package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.SortInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class SortApplication implements SortInterface {

    /**
     * Runs the sort application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws SortException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        // Format: sort [-nrf] [FILES]
        if (stdout == null) {
            throw new SortException(E_NULL_POINTER);
        }
        SortArgsParser sortArgs = parseArgs(args);
        StringBuilder output = new StringBuilder();
        try {
            if (sortArgs.isReadingFromStdin()) {
                output.append(sortFromStdin(sortArgs.isFirstWordNumber(), sortArgs.isReverseOrder(), sortArgs.isCaseIndependent(), stdin));
            } else {
                output.append(sortFromFiles(sortArgs.isFirstWordNumber(), sortArgs.isReverseOrder(), sortArgs.isCaseIndependent(), sortArgs.getFiles().toArray(new String[0])));
            }
        } catch (SortException e) {
            throw e;
        }
        try {
            if (!output.toString().isEmpty()) {
                stdout.write(output.toString().getBytes());
            }
        } catch (IOException e) {
            throw new SortException(E_WRITE_STREAM, e);
        }
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @throws SortException
     */
    protected SortArgsParser parseArgs(String... args) throws SortException {
        SortArgsParser parser = new SortArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new SortException(e.getMessage(), e);
        }
        return parser;
    }

    /**
     * Returns string containing the orders of the lines of the specified file
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param fileNames         Array of String of file names
     * @throws Exception
     */
    @Override
    public String sortFromFiles(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                String... fileNames) throws SortException {
        if (fileNames == null) {
            throw new SortException(E_NULL_ARGS);
        }
        List<String> lines = new ArrayList<>();
        try {
            for (String file : fileNames) {
                File node = IOUtils.resolveFilePath(file).toFile();
                if (!node.exists()) {
                    throw new SortException(node.getName() + ": " + E_FILE_NOT_FOUND);
                }
                if (node.isDirectory()) {
                    throw new SortException(node.getName() + ": " + E_IS_DIR);
                }
                if (!node.canRead()) {
                    throw new SortException(node.getName() + ": " + E_NO_PERM);
                }
                InputStream input = IOUtils.openInputStream(file);
                lines.addAll(IOUtils.getLinesFromInputStream(input));
                IOUtils.closeInputStream(input);
            }
            sortInputString(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
            return String.join(STRING_NEWLINE, lines) + STRING_NEWLINE;
        } catch (ShellException e) {
            throw new SortException(e.getMessage(), e);
        }
    }

    /**
     * Returns string containing the orders of the lines from the standard input
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param stdin             InputStream containing arguments from Stdin
     * @throws Exception
     */
    @Override
    public String sortFromStdin(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                InputStream stdin) throws SortException {
        if (stdin == null) {
            throw new SortException(E_NULL_POINTER);
        }
        try {
            List<String> lines = IOUtils.getLinesFromInputStream(stdin);
            sortInputString(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
            return String.join(STRING_NEWLINE, lines) + STRING_NEWLINE;
        } catch (ShellException e) {
            throw new SortException(e.getMessage(), e);
        }
    }

    /**
     * Sorts the input ArrayList based on the given conditions. Invoking this function will mutate the ArrayList.
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param input             ArrayList of Strings of lines
     */
    protected void sortInputString(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                   List<String> input) {
        Collections.sort(input, new Comparator<String>() {
            @Override
            public int compare(String str1, String str2) {
                String temp1 = isCaseIndependent ? str1.toLowerCase(Locale.ENGLISH) : str1;
                String temp2 = isCaseIndependent ? str2.toLowerCase(Locale.ENGLISH) : str2;

                // Extract the first group of numbers if possible.
                if (isFirstWordNumber && !temp1.isEmpty() && !temp2.isEmpty()) {
                    String chunk1 = getChunk(temp1);
                    String chunk2 = getChunk(temp2);

                    char chunk1FirstChar = chunk1.length() == 1
                            ? chunk1.charAt(0)
                            : chunk1.charAt(0) == '-' ? chunk1.charAt(1) : chunk1.charAt(0);
                    char chunk2FirstChar = chunk2.length() == 1
                            ? chunk2.charAt(0)
                            : chunk2.charAt(0) == '-' ? chunk2.charAt(1) : chunk2.charAt(0);

                    // If both chunks can be represented as numbers, sort them numerically.
                    int result = 0;

                    if (Character.isDigit(chunk1FirstChar) && Character.isDigit(chunk2FirstChar)) {
                        result = new BigInteger(chunk1).compareTo(new BigInteger(chunk2));
                    } else {
                        result = chunk1.compareTo(chunk2);
                    }
                    if (result != 0) {
                        return result;
                    }
                    return temp1.substring(chunk1.length()).compareTo(temp2.substring(chunk2.length()));
                }

                return temp1.compareTo(temp2);
            }
        });
        if (isReverseOrder) {
            Collections.reverse(input);
        }
    }

    /**
     * Extracts a chunk of numbers or non-numbers from str starting from index 0.
     *
     * @param str Input string to read from
     */
    protected String getChunk(String str) {
        int startIndexLocal = 0;
        StringBuilder chunk = new StringBuilder();
        final int strLen = str.length();
        char chr = str.charAt(startIndexLocal++);
        chunk.append(chr);
        final boolean extractDigit = Character.isDigit(chr) || chr == '-';
        while (startIndexLocal < strLen) {
            char nextChr = str.charAt(startIndexLocal++);
            if ((extractDigit && !Character.isDigit(nextChr)) || (!extractDigit && Character.isDigit(nextChr))) {
                break;
            }
            chunk.append(nextChr);
        }
        return chunk.toString();
    }
}