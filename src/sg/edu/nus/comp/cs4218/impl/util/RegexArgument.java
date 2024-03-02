package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_ASTERISK;

public final class RegexArgument {
    private List<String> plaintext;
    private List<String> regex;
    private boolean hasWildcard;

    public RegexArgument() {
        this.plaintext = new LinkedList<>();
        this.regex = new LinkedList<>();
        this.hasWildcard = false;
    }

    public RegexArgument(String str) {
        this();
        merge(str);
    }

    // Used for `find` command.
    // `text` here corresponds to the folder that we want to look in.
    public RegexArgument(String str, String text, boolean hasWildcard) {
        this();
        this.plaintext.add(text);
        this.hasWildcard = hasWildcard;
        this.regex.add(".*"); // We want to match filenames
        for (char c : str.toCharArray()) {
            if (c == CHAR_ASTERISK) {
                this.regex.add("[^" + StringUtils.fileSeparator() + "]*");
            } else {
                this.regex.add(Pattern.quote(String.valueOf(c)));
            }
        }
    }

    public void append(char chr) {
        plaintext.add(Character.toString(chr));
        regex.add(Pattern.quote(String.valueOf(chr)));
    }

    public void appendAsterisk() {
        plaintext.add(Character.toString(CHAR_ASTERISK));
        regex.add(String.format("[^%s]*", StringUtils.fileSeparator()));
        hasWildcard = true;
    }

    public void merge(RegexArgument other) {
        plaintext.addAll(other.plaintext);
        regex.addAll(other.regex);
        hasWildcard = hasWildcard || other.hasWildcard;
    }

    public void merge(String str) {
        plaintext.add(str);
        regex.add(Pattern.quote(str));
    }

    public List<String> globFiles() {
        List<String> globbedFiles = new LinkedList<>();

        if (isRegex()) {
            Pattern regexPattern = Pattern.compile(String.join("", regex));
            String dir = "";
            List<String> tokens = Arrays.stream(String.join("", plaintext).replaceAll("\\\\", "/").split("/"))
                    .filter(x -> !StringUtils.isBlank(x)).collect(Collectors.toList());
            if (tokens.size() > 1) {
                for (int i = 0; i < tokens.size(); i++) {
                    if (tokens.get(i).contains("*")) {
                        break;
                    }
                    dir += tokens.get(i) + StringUtils.fileSeparator();
                }
            }
            File currentDir;
            if (Paths.get(dir).isAbsolute()) {
                currentDir = Paths.get(dir).toFile();
            } else {
                currentDir = Paths.get(Environment.getCurrentDirectory() + StringUtils.fileSeparator() + dir).toFile();
            }

            if (currentDir.isDirectory()) {
                for (String candidate : currentDir.list()) {
                    if (regexPattern.matcher(dir + candidate).matches()) {
                        globbedFiles.add(dir + candidate);
                    }
                }
            }

            Collections.sort(globbedFiles);
        }

        if (globbedFiles.isEmpty()) {
            globbedFiles.add(String.join("", plaintext));
        }

        return globbedFiles;
    }


    /**
     * Traverses a given File node and returns a list of absolute path that match the given regexPattern.
     * <p>
     * Assumptions:
     * - ignores files and folders that we do not have access to (insufficient read permissions)
     * - regexPattern should not be null
     *
     * @param regexPattern    Pattern object
     * @param node            File object
     * @param isAbsolute      Boolean option to indicate that the regexPattern refers to an absolute path
     * @param onlyDirectories Boolean option to list only the directories
     */
    private List<String> traverseAndFilter(Pattern regexPattern, File node, boolean isAbsolute, boolean onlyDirectories) {
        List<String> matches = new ArrayList<>();
        if (regexPattern == null || !node.canRead() || !node.isDirectory()) {
            return matches;
        }
        for (String current : node.list()) {
            File nextNode = new File(node, current);
            String match = isAbsolute
                    ? nextNode.getPath()
                    : nextNode.getPath().substring(Environment.getCurrentDirectory().length() + 1);
            // TODO: Find a better way to handle this.
            if (onlyDirectories && nextNode.isDirectory()) {
                match += File.separator;
            }
            if (!nextNode.isHidden() && regexPattern.matcher(match).matches()) {
                matches.add(nextNode.getAbsolutePath());
            }
            matches.addAll(traverseAndFilter(regexPattern, nextNode, isAbsolute, onlyDirectories));
        }
        return matches;
    }

    public boolean isRegex() {
        return hasWildcard;
    }

    public boolean isEmpty() {
        return plaintext.isEmpty();
    }

    public String toString() {
        return String.join("", plaintext);
    }
}
