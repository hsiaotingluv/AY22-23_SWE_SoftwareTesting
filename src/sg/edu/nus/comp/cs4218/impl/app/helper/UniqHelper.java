package sg.edu.nus.comp.cs4218.impl.app.helper;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;

public final class UniqHelper {

    private UniqHelper() {
    }

    public static List<String> removeDuplicateLines(InputStream input, Boolean isCount) throws UniqException {
        try {
            List<String> tokens = IOUtils.getLinesFromInputStream(input);
            List<String> result = new ArrayList<>();

            if (tokens.isEmpty()) {
                result.add(StringUtils.STRING_NEWLINE);
                return result;
            }

            String prevLine = tokens.get(0);
            int countDuplicate = 1;

            for (int i = 1; i < tokens.size(); i++) {
                String currLine = tokens.get(i);
                if (currLine.equals(prevLine)) {
                    countDuplicate++;
                } else {
                    String line = "";
                    if (isCount) {
                        line = countDuplicate + " " + prevLine;
                    } else {
                        line = prevLine;
                    }
                    result.add(line);

                    // reset values
                    prevLine = currLine;
                    countDuplicate = 1;
                }
            }

            // add last unique line
            String line = "";
            if (isCount) {
                line = countDuplicate + " " + prevLine;
            } else {
                line = prevLine;
            }
            result.add(line);

            return result;

        } catch (Exception e) {
            throw new UniqException(e.getMessage(), e);
        }
    }

    public static List<String> getDuplicateLines(List<String> tokens, Boolean isRepeated, Boolean isAllRepeated) throws UniqException {
        try {
            List<String> result = new ArrayList<>();
            for (String str : tokens) {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
                int count = Integer.parseInt(str.split("\\s+", 2)[0]);

                if (isAllRepeated && count > 1) {
                    for (int i = 0; i < count; i++) {
                        result.add(str);
                    }
                } else if (isRepeated && count > 1) {
                    result.add(str);
                }
            }
            return result;

        } catch (Exception e) {
            throw new UniqException(e.getMessage(), e);
        }
    }

    public static boolean writeToFile(String lines, String outputFileName) throws UniqException {
        try {
            File node = IOUtils.resolveFilePath(outputFileName).toFile();
            if (node.exists()) {
                if (node.isDirectory()) {
                    throw new UniqException(node.getName() + ": " + E_IS_DIR);
                }
                if (!node.canWrite()) {
                    throw new UniqException(node.getName() + ": " + E_NO_PERM);
                }
            }
            byte[] strToBytes = lines.getBytes();
            Files.write(node.toPath(), strToBytes);
            return true;

        } catch (UniqException e) {
            throw e;
        } catch (IOException | ShellException e) {
            throw new UniqException(e.getMessage(), e);
        }
    }

    public static void validatePath(Path path) throws UniqException {
        if (Files.exists(path) && !Files.isReadable(path)) {
            throw new UniqException(E_NO_PERM);
        }

        if (!Files.exists(path)) {
            throw new UniqException(path.getFileName() + ": " + E_FILE_NOT_FOUND);
        }

        if (Files.isDirectory(path)) {
            throw new UniqException(path.getFileName() + ": " + E_IS_DIR);
        }
    }
}
