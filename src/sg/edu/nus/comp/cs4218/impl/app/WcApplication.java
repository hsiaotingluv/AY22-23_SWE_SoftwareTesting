package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.WcInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.app.helper.WcHelper.getCountLine;
import static sg.edu.nus.comp.cs4218.impl.app.helper.WcHelper.getCountReport;
import static sg.edu.nus.comp.cs4218.impl.app.helper.WcHelper.invalidNode;
import static sg.edu.nus.comp.cs4218.impl.app.helper.WcHelper.updateCountTotal;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_GENERAL;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class WcApplication implements WcInterface {


    /**
     * Runs the wc application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws WcException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws WcException {
        // Format: wc [-clw] [FILES]
        if (args == null) {
            throw new WcException(E_NULL_POINTER);
        }
        if (stdin == null) {
            throw new WcException(E_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new WcException(E_NO_OSTREAM);
        }

        WcArgsParser wcArgs = parseArgs(args);
        StringBuilder output = new StringBuilder();

        try {
            if (wcArgs.isReadingFromStdinOnly()) {
                output.append(countFromStdin(wcArgs.isBytes(), wcArgs.isLines(), wcArgs.isWords(), stdin));
            } else {
                output.append(countFromFileAndStdin(wcArgs.isBytes(), wcArgs.isLines(), wcArgs.isWords(),
                        stdin, wcArgs.getFiles().toArray(new String[0])));
            }
        } catch (WcException e) {
            throw e;
        }
        try {
            if (!output.toString().isEmpty()) {
                stdout.write(output.toString().getBytes());
            }
        } catch (IOException e) {
            throw new WcException(E_WRITE_STREAM, e);
        }
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @throws WcException
     */
    protected WcArgsParser parseArgs(String... args) throws WcException {
        WcArgsParser parser = new WcArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage(), e);
        }
        return parser;
    }

    /**
     * Returns string containing the number of lines, words, and bytes in input files
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param fileName Array of String of file names (not including "-" for reading from stdin)
     * @throws WcException
     */
    @Override
    public String countFromFiles(Boolean isBytes, Boolean isLines, Boolean isWords,
                                 String... fileName) throws WcException {
        if (fileName == null) {
            throw new WcException(E_GENERAL);
        }

        try {
            List<String> result = new ArrayList<>();
            long[] total = {0, 0, 0}; // lines words bytes

            for (String file : fileName) {
                File node = IOUtils.resolveFilePath(file).toFile();
                if (invalidNode(node, result)) {
                    continue;
                }

                InputStream input = IOUtils.openInputStream(file);
                long[] count = getCountReport(input); // lines words bytes
                IOUtils.closeInputStream(input);

                total[0] += count[0];
                total[1] += count[1];
                total[2] += count[2];

                String countLine = getCountLine(isLines, isWords, isBytes, count, file);
                result.add(countLine);
            }

            // get cumulative counts for all the files
            if (fileName.length > 1) {
                String totalLine = getCountLine(isLines, isWords, isBytes, total, "total");
                result.add(totalLine);
            }

            return String.join(STRING_NEWLINE, result) + STRING_NEWLINE;

        } catch (WcException e) {
            throw e;
        } catch (Exception e) {
            throw new WcException(e.getMessage(), e);
        }
    }

    /**
     * Returns string containing the number of lines, words, and bytes in standard input
     *
     * @param isBytes Boolean option to count the number of Bytes
     * @param isLines Boolean option to count the number of lines
     * @param isWords Boolean option to count the number of words
     * @param stdin   InputStream containing arguments from Stdin
     * @throws WcException
     */
    @Override
    public String countFromStdin(Boolean isBytes, Boolean isLines, Boolean isWords,
                                 InputStream stdin) throws WcException {
        if (stdin == null) {
            throw new WcException(E_NULL_POINTER);
        }

        try {
            long[] count = getCountReport(stdin); // lines words bytes;
            String countLine = getCountLine(isLines, isWords, isBytes, count);
            return countLine + STRING_NEWLINE;

        } catch (WcException e) {
            throw e;
        } catch (Exception e) {
            throw new WcException(e.getMessage(), e);
        }
    }

    /**
     * Returns string containing the number of lines, words, and bytes in standard input and files
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names (not including "-" for reading from stdin)
     * @throws WcException
     */
    @Override
    public String countFromFileAndStdin(Boolean isBytes, Boolean isLines, Boolean isWords, InputStream stdin, String... fileName) throws WcException {
        try {
            List<String> output = new ArrayList<>();
            long[] total = {0, 0, 0}; // lines words bytes

            for (String str : fileName) {
                if ("-".equals(str)) {
                    output.add(countFromStdin(isBytes, isLines, isWords, stdin).replaceAll(STRING_NEWLINE + "$", " -"));
                } else {
                    output.add(countFromFiles(isBytes, isLines, isWords, str).replaceAll(STRING_NEWLINE + "$", ""));
                }
            }

            if (fileName.length >= 2) {
                for (String out : output) {
                    if (out.contains(":")) {
                        continue;
                    }
                    List<String> splitList = Arrays.asList(out.trim().split("\\s+"));
                    int len = splitList.size();
                    // check if last element is not an integer, removes filename
                    if (len > 1 && !StringUtils.isNumber(splitList.get(len - 1))) {
                        splitList = splitList.subList(0, len - 1);
                    }

                    if (splitList.isEmpty()) {
                        break;
                    } else {
                        total = updateCountTotal(splitList, isLines, isWords, isBytes, total);
                    }
                }

                String totalLine = getCountLine(isLines, isWords, isBytes, total, "total");
                output.add(totalLine);
            }
            return String.join(STRING_NEWLINE, output) + STRING_NEWLINE;

        } catch (WcException e) {
            throw e;
        } catch (Exception e) {
            throw new WcException(e.getMessage(), e);
        }
    }
}
