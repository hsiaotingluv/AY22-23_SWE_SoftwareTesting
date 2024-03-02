package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class RmApplication implements RmInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        // Format: rm [-rd] FILES...
        if (stdout == null) {
            throw new RmException(E_NULL_POINTER);
        }
        RmArgsParser rmArgs = parseArgs(args);
        StringBuilder output = new StringBuilder();
        for (String file : rmArgs.getFiles().toArray(new String[0])) {
            try {
                remove(rmArgs.isEmptyFolder(), rmArgs.isRecursive(), file);
            } catch (RmException e) {
                output.append(e.getMessage());
            }
        }
        try {
            if (!output.toString().isEmpty()) {
                stdout.write(output.toString().getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new RmException(E_WRITE_STREAM, e);
        }
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @throws RmException
     */
    protected RmArgsParser parseArgs(String... args) throws RmException {
        RmArgsParser parser = new RmArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new RmException(e.getMessage(), e);
        }
        return parser;
    }

    /**
     * Remove the file. (It does not remove folder by default)
     *
     * @param isEmptyFolder Boolean option to delete a folder only if it is empty
     * @param isRecursive   Boolean option to recursively delete the folder contents (traversing
     *                      through all folders inside the specified folder)
     * @param fileName      Array of String of file names
     * @throws Exception
     */
    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileName) throws RmException {
        if (fileName == null) {
            throw new RmException(E_NULL_ARGS);
        }

        for (String file : fileName) {
            File node;
            try {
                node = IOUtils.resolveFilePath(file).toFile();
            } catch (ShellException e) {
                throw new RmException(e.getMessage(), e);
            }
            if (!node.exists()) {
                throw new RmException(node.getName() + ": " + E_FILE_NOT_FOUND);
            }
            if (node.isDirectory()) {
                boolean folderIsEmpty = checkEmptyFolder(node);
                if (isEmptyFolder && folderIsEmpty) {
                    node.delete();
                } else if (isRecursive) {
                    if (!deleteDirectory(node)) {
                        throw new RmException(E_NULL_POINTER);
                    }
                } else {
                    throw new RmException(node.getName() + ": " + E_IS_DIR);
                }
            } else {
                node.delete();
            }
        }
    }

    /**
     * Checks if a directory is empty.
     *
     * @param directory File directory to be checked if empty
     */
    protected boolean checkEmptyFolder(File directory) {
        String[] contents = directory.list();
        return (contents == null || contents.length == 0);
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory File directory to be deleted
     */
    protected boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteDirectory(file);
            }
        }
        return directory.delete();
    }
}
