package sg.edu.nus.comp.cs4218.impl.app.helper;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_CURR_DIR;

public final class LsHelper {

    private final static String PATH_CURR_DIR = STRING_CURR_DIR + CHAR_FILE_SEP;

    private LsHelper() {
    }

    /**
     * Builds the resulting string to be written into the output stream.
     * <p>
     * NOTE: This is recursively called if user wants recursive mode.
     *
     * @param paths       - list of java.nio.Path objects to list
     * @param isRecursive - recursive mode, repeatedly ls the child directories
     * @param isSortByExt - sorts folder contents alphabetically by file extension (characters after the last ‘.’ (without quotes)). Files with no extension are sorted first.
     * @return String to be written to output stream.
     */
    public static String buildResult(List<Path> paths, Boolean isRecursive, Boolean isSortByExt) {
        StringBuilder result = new StringBuilder();
        for (Path path : paths) {
            try {
                List<Path> contents = getContents(path);
                String formatted = formatContents(contents, isSortByExt);
                String relativePath = getRelativeToCwd(path).toString();
                result.append(StringUtils.isBlank(relativePath) ? PATH_CURR_DIR : relativePath);
                String line = ":" + StringUtils.STRING_NEWLINE;
                result.append(line);
                result.append(formatted);

                if (!formatted.isEmpty()) {
                    // Empty directories should not have an additional new line
                    result.append(StringUtils.STRING_NEWLINE);
                }
                result.append(StringUtils.STRING_NEWLINE);

                // RECURSE!
                if (isRecursive) {
                    result.append(buildResult(contents, isRecursive, isSortByExt));
                }
            } catch (InvalidDirectoryException e) {
                if (!isRecursive) {
                    result.append(e.getMessage());
                    result.append(StringUtils.STRING_NEWLINE);
                }
            }
        }

        return result.toString();
    }

    /**
     * Formats the contents of a directory into a single string.
     *
     * @param contents    - list of items in a directory
     * @param isSortByExt - sorts folder contents alphabetically by file extension (characters after the last ‘.’ (without quotes)). Files with no extension are sorted first.
     * @return
     */
    public static String formatContents(List<Path> contents, Boolean isSortByExt) {
        ArrayList<String> fileNames = new ArrayList<>();
        ArrayList<String> fileExtensions = new ArrayList<>();
        ArrayList<String> sortedFileNames = new ArrayList<>();

        for (Path path : contents) {
            String fileName = path.getFileName().toString();
            fileNames.add(fileName);

            if (fileName.contains(".")) {
                String[] splat = fileName.split("\\.");
                String fileExtension = splat[splat.length - 1]; // assuming fileName does not contain "."
                if (!fileExtensions.contains(fileExtension)) {
                    fileExtensions.add("." + fileExtension);
                }
                // add files with no extension into sortedFileNames
            } else {
                sortedFileNames.add(fileName);
            }
        }

        if (isSortByExt) {
            Collections.sort(fileNames);
            Collections.sort(fileExtensions);
            for (String extension : fileExtensions) {
                for (String fileName : fileNames) {
                    if (!sortedFileNames.contains(fileName) && fileName.contains(extension)) {
                        sortedFileNames.add(fileName);
                    }
                }
            }
            fileNames = sortedFileNames;
        }

        StringBuilder result = new StringBuilder();
        for (String fileName : fileNames) {
            result.append(fileName);
            result.append(StringUtils.STRING_NEWLINE);
        }

        return result.toString().trim();
    }

    /**
     * Gets the contents in a single specified directory.
     *
     * @param directory
     * @return List of files + directories in the passed directory.
     */
    public static List<Path> getContents(Path directory)
            throws InvalidDirectoryException {
        if (!Files.exists(directory)) {
            throw new InvalidDirectoryException(getRelativeToCwd(directory).toString());
        }

        if (!Files.isDirectory(directory)) {
            throw new InvalidDirectoryException(getRelativeToCwd(directory).toString());
        }

        List<Path> result = new ArrayList<>();
        File pwd = directory.toFile();
        for (File f : pwd.listFiles()) {
            if (!f.isHidden()) {
                result.add(f.toPath());
            }
        }

        Collections.sort(result);

        return result;
    }

    /**
     * Resolve all paths given as arguments into a list of Path objects for easy path management.
     *
     * @param directories
     * @return List of java.nio.Path objects
     */
    public static List<Path> resolvePaths(String... directories) throws InvalidDirectoryException {
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < directories.length; i++) {
            paths.add(resolvePath(directories[i]));
        }

        return paths;
    }

    /**
     * Converts a String into a java.nio.Path objects. Also resolves if the current path provided
     * is an absolute path.
     *
     * @param directory
     * @return
     */
    public static Path resolvePath(String directory) throws InvalidDirectoryException {
        Path path;
        if (Paths.get(directory).isAbsolute()) {
            // This is an absolute path
            path = Paths.get(directory).normalize();
        } else {
            path = Paths.get(Environment.getCurrentDirectory(), directory).normalize();
        }
        if (!path.toFile().exists()) {
            throw new InvalidDirectoryException(getRelativeToCwd(path).toString());
        }
        return path;
    }

    /**
     * Converts a path to a relative path to the current directory.
     *
     * @param path
     * @return
     */
    public static Path getRelativeToCwd(Path path) {
        return Paths.get(Environment.getCurrentDirectory()).relativize(path);
    }
}
