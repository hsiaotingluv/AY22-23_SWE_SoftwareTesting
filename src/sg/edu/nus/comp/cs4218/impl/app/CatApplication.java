package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CatInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class CatApplication implements CatInterface {
    public static final String E_IS_DIR = "This is a directory";
    public static final String E_READING_FILE = "Could not read file";
    public static final String E_WRITE_STREAM = "Could not write to output stream";
    public static final String E_NULL_POINTER = "Null Pointer Exception";

    /**
     * Runs the cat application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws CatException If the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        // Format: cat [-n] [FILES]...
        if (stdout == null) {
            throw new CatException(E_NULL_POINTER);
        }
        CatArgsParser catArgs = parseArgs(args);
        StringBuilder output = new StringBuilder();
        try {
            output.append(catFileAndStdin(catArgs.isLineNumber(), stdin, catArgs.getFiles()));
        } catch (CatException e) {
            throw e;
        }
        try {
            if (output.toString().isEmpty()) {
                stdout.write(STRING_NEWLINE.getBytes());
            } else {
                stdout.write(output.toString().getBytes());
            }
        } catch (IOException e) {
            throw new CatException(E_WRITE_STREAM, e);
        }
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @throws CatException
     */
    protected CatArgsParser parseArgs(String... args) throws CatException {
        CatArgsParser parser = new CatArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CatException(e.getMessage(), e);
        }
        return parser;
    }

    /**
     * Returns string containing the content of the specified file
     *
     * @param isLineNumber Prefix lines with their corresponding line number starting from 1
     * @param fileName     Array of String of file names (not including "-" for reading from stdin)
     * @return String containing content of specified file(s)
     * @throws CatException
     */
    @Override
    public String catFiles(Boolean isLineNumber, String... fileName) throws CatException {
        if (fileName == null) {
            throw new CatException(E_NULL_ARGS);
        }
        List<String> output = new ArrayList<>();
        File node;
        try {
            node = IOUtils.resolveFilePath(fileName[0]).toFile();
        } catch (ShellException e) {
            throw new CatException(e.getMessage(), e);
        }
        if (!node.exists()) {
            throw new CatException(node.getName() + ": " + E_FILE_NOT_FOUND);
        }
        if (node.isDirectory()) {
            throw new CatException(node.getName() + ": " + E_IS_DIR);
        }
        if (!node.canRead()) {
            throw new CatException(node.getName() + ": " + E_NO_PERM);
        }
        try {
            InputStream input = IOUtils.openInputStream(fileName[0]);
            output.addAll(getAndPrefixLinesFromInputStream(input, isLineNumber));
            IOUtils.closeInputStream(input);
        } catch (ShellException e) {
            throw new CatException(E_READING_FILE, e);
        }
        return String.join(STRING_NEWLINE, output) + STRING_NEWLINE;
    }

    /**
     * Returns string containing the content of the standard input
     *
     * @param isLineNumber Prefix lines with their corresponding line number starting from 1
     * @param stdin        InputStream containing arguments from Stdin
     * @return String containing content of standard input
     * @throws CatException
     */
    @Override
    public String catStdin(Boolean isLineNumber, InputStream stdin) throws CatException {
        if (stdin == null) {
            throw new CatException(E_NULL_POINTER);
        }
        List<String> output;
        try {
            output = getAndPrefixLinesFromInputStream(stdin, isLineNumber);
        } catch (Exception e) {
            throw new CatException(E_NULL_POINTER, e);
        }
        if (output.isEmpty()) {
            return String.join(STRING_NEWLINE, output);
        }
        return String.join(STRING_NEWLINE, output) + STRING_NEWLINE;
    }

    /**
     * Returns string containing the content of the standard input and specified file
     *
     * @param isLineNumber Prefix lines with their corresponding line number starting from 1
     * @param stdin        InputStream containing arguments from Stdin
     * @param fileName     Array of String of file names (including "-" for reading from stdin)
     * @return
     * @throws CatException
     */
    @Override
    public String catFileAndStdin(Boolean isLineNumber, InputStream stdin, String... fileName) throws CatException {
        List<String> output = new ArrayList<>();

        for (String str : fileName) {
            try {
                if ("-".equals(str)) {
                    output.add(catStdin(isLineNumber, stdin).stripTrailing());
                } else {
                    output.add(catFiles(isLineNumber, str).stripTrailing());
                }
            } catch (CatException e) {
                output.add(e.getMessage());
            }
        }
        return String.join(STRING_NEWLINE, output).stripTrailing() + STRING_NEWLINE;
    }

    /**
     * Returns string containing the content of the standard input or specified file and prefixes them if specified.
     *
     * @param input        InputStream containing arguments from Stdin or FileInputStream
     * @param isLineNumber Prefix lines with their corresponding line number starting from 1
     * @return
     * @throws ShellException
     */
    protected List<String> getAndPrefixLinesFromInputStream(InputStream input, Boolean isLineNumber) throws ShellException {
        List<String> lines = IOUtils.getLinesFromInputStream(input);

        if (isLineNumber) {
            return IntStream.rangeClosed(1,
                            lines.size())
                    .boxed()
                    .map(x -> "\t" + x + " " + lines.get(x - 1))
                    .collect(Collectors.toList());
        }

        return lines;
    }
}
