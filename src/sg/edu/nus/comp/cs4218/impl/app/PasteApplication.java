package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.app.helper.PasteHelper.concatenateLines;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


public class PasteApplication implements PasteInterface {
    /**
     * Runs the paste application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws PasteException If the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws PasteException {

        if (stdout == null) {
            throw new PasteException(E_NULL_POINTER);
        }

        PasteArgsParser pasteArgsParser = parseArgs(args);
        StringBuilder output = new StringBuilder();
        try {
            output.append(mergeFileAndStdin(
                    pasteArgsParser.isSerial(),
                    stdin,
                    pasteArgsParser.getFiles().toArray(new String[0])));
        } catch (PasteException e) {
            throw e;
        }
        try {
            if (output.toString().isEmpty()) {
                stdout.write(STRING_NEWLINE.getBytes());
            } else {
                stdout.write(output.toString().getBytes());
            }
        } catch (Exception e) {
            throw new PasteException(E_WRITE_STREAM, e);
        }
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @throws PasteException
     */
    protected PasteArgsParser parseArgs(String... args) throws PasteException {
        PasteArgsParser parser = new PasteArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new PasteException(e.getMessage(), e);
        }
        return parser;
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) Stdin arguments. If only one Stdin
     * arg is specified, echo back the Stdin.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param stdin    InputStream containing arguments from Stdin
     * @throws PasteException
     */
    public String mergeStdin(Boolean isSerial, InputStream stdin) throws PasteException {

        if (stdin == null) {
            throw new PasteException(E_NULL_POINTER);
        }
        List<String> output;
        try {
            output = IOUtils.getLinesFromInputStream(stdin);
        } catch (Exception e) {
            throw new PasteException(E_NULL_POINTER, e);
        }
        if (output.isEmpty()) {
            return "";
        }

        return String.join(STRING_NEWLINE, output);
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) files. If only one file is
     * specified, echo back the file content.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param fileName Array of file names to be read and merged (not including "-" for reading from stdin)
     * @throws PasteException
     */
    public String mergeFile(Boolean isSerial, String... fileName) throws PasteException {

        if (fileName == null) {
            throw new PasteException(E_NULL_POINTER);
        }
        List<String> output = new ArrayList<>();
        File node;

        try {
            node = IOUtils.resolveFilePath(fileName[0]).toFile();
        } catch (ShellException e) {
            throw new PasteException(e.getMessage(), e);
        }
        if (!node.exists()) {
            throw new PasteException(node.getName() + ": " + E_FILE_NOT_FOUND);
        }
        if (node.isDirectory()) {
            throw new PasteException(node.getName() + ": " + E_IS_DIR);
        }
        if (!node.canRead()) {
            throw new PasteException(node.getName() + ": " + E_NO_PERM);
        }

        try {
            output.addAll(Files.readAllLines(node.toPath()));
        } catch (IOException e) {
            throw new PasteException(E_IO_EXCEPTION, e);
        }

        return String.join(STRING_NEWLINE, output);
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) files and Stdin arguments.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of file names to be read and merged (including "-" for reading from stdin)
     * @throws PasteException
     */
    public String mergeFileAndStdin(Boolean isSerial, InputStream stdin, String... fileName) throws PasteException {
        List<String> output = new ArrayList<>();
        List<String> exceptionOutput = new ArrayList<>();

        boolean isStdinHandled = false;

        if (fileName.length == 0) {
            output.add(mergeStdin(isSerial, stdin).trim());
        }

        for (String str : fileName) {
            try {
                if ("-".equals(str) && !isStdinHandled) {
                    output.add(mergeStdin(isSerial, stdin).trim());
                    isStdinHandled = true;
                } else if ("-".equals(str)){
                    continue;
                } else {
                    output.add(mergeFile(isSerial, str).trim());
                }
            } catch (PasteException e) {
                if (isSerial) {
                    exceptionOutput.add(e.getMessage());
                } else {
                    throw e;
                }
            }
        }

        List<List<String>> totalLines = output.stream().filter(s -> !s.isEmpty())
                .map(s -> Arrays.asList((s.split(STRING_NEWLINE))))
                .collect(Collectors.toList());

        String result = concatenateLines(isSerial, totalLines);
        String exceptions = String.join(STRING_NEWLINE, exceptionOutput);
        if (!result.isEmpty()) {
            result += STRING_NEWLINE;
        }
        return result + (exceptions.length() > 0 ? exceptions + STRING_NEWLINE : "");

    }

}
