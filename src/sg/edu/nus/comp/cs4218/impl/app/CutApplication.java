package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class CutApplication implements CutInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        // Format: cut [-c] LIST FILES
        //         cut [-b] LIST FILES
        if (stdout == null) {
            throw new CutException(E_NULL_POINTER);
        }
        CutArgsParser cutArgs = parseArgs(args);
        StringBuilder output = new StringBuilder();
        try {
            if (cutArgs.isReadingFromStdin()) {
                output.append(cutFromStdin(cutArgs.isCharPo(), cutArgs.isBytePo(), cutArgs.getRanges(), stdin));
            } else {
                output.append(cutFromFiles(cutArgs.isCharPo(), cutArgs.isBytePo(), cutArgs.getRanges(), cutArgs.getFiles()));
            }
        } catch (CutException e) {
            throw e;
        }
        try {
            if (!output.toString().isEmpty()) {
                stdout.write(output.toString().getBytes());
            }
        } catch (IOException e) {
            throw new CutException(E_WRITE_STREAM, e);
        }
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @throws CutException
     */
    protected CutArgsParser parseArgs(String... args) throws CutException {
        CutArgsParser parser = new CutArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CutException(e.getMessage(), e);
        }
        return parser;
    }

    /**
     * Cuts out selected portions of each line
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param fileName Array of String of file names
     * @return
     * @throws CutException
     */
    @Override
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, String... fileName) throws CutException {
        if (fileName == null) {
            throw new CutException(E_NULL_ARGS);
        }
        List<String> output = new ArrayList<>();
        List<String> errorOutput = new ArrayList<>();
        for (String file : fileName) {
            try {
                try {
                    File node = IOUtils.resolveFilePath(file).toFile();
                    if (!node.exists()) {
                        throw new CutException(node.getName() + ": " + E_FILE_NOT_FOUND);
                    }
                    if (node.isDirectory()) {
                        throw new CutException(node.getName() + ": " + E_IS_DIR);
                    }
                    if (!node.canRead()) {
                        throw new CutException(node.getName() + ": " + E_NO_PERM);
                    }
                    InputStream input = IOUtils.openInputStream(file);
                    output.addAll(cutLines(isCharPo, isBytePo, ranges, input));
                    IOUtils.closeInputStream(input);
                } catch (ShellException e) {
                    throw new CutException(e.getMessage(), e);
                }
            } catch (CutException e) {
                errorOutput.add(e.getMessage());
            }
        }
        output.addAll(errorOutput);
        return String.join(STRING_NEWLINE, output) + STRING_NEWLINE;
    }

    /**
     * Cuts out selected portions of each line
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param stdin    InputStream containing arguments from Stdin
     * @return
     * @throws CutException
     */
    @Override
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin) throws CutException {
        if (stdin == null) {
            throw new CutException(E_NULL_POINTER);
        }
        List<String> output;
        try {
            output = cutLines(isCharPo, isBytePo, ranges, stdin);
        } catch (ShellException e) {
            throw new CutException(e.getMessage(), e);
        }
        return String.join(STRING_NEWLINE, output) + STRING_NEWLINE;
    }

    /**
     * Helper function to cut out selected portions of each line using either char or byte position.
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param input    InputStream containing arguments from Stdin or Files
     * @return
     * @throws ShellException
     */
    protected List<String> cutLines(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream input) throws ShellException {
        List<String> lines = IOUtils.getLinesFromInputStream(input);

        return lines.stream()
                .map(line -> { // do this for each line
                    return ranges.stream()
                            .map(range -> {
                                int start = range[0] - 1;
                                int end = range[1];

                                if (isCharPo) {
                                    if (line.length() < end) {
                                        if (range[0] == range[1]) {
                                            return "";
                                        } else {
                                            end = line.length();
                                        }
                                    }
                                    return line.substring(start, end);
                                } else {
                                    byte[] bytes = line.getBytes();
                                    if (bytes.length < end) {
                                        if (range[0] == range[1]) {
                                            return "";
                                        } else {
                                            end = bytes.length;
                                        }
                                    }
                                    return new String(Arrays.copyOfRange(bytes, start, end));
                                }
                            })
                            .reduce("", (x, y) -> x + y);
                }).collect(Collectors.toList());
    }
}
