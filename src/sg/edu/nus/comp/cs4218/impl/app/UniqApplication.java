package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.app.helper.UniqHelper.getDuplicateLines;
import static sg.edu.nus.comp.cs4218.impl.app.helper.UniqHelper.removeDuplicateLines;
import static sg.edu.nus.comp.cs4218.impl.app.helper.UniqHelper.validatePath;
import static sg.edu.nus.comp.cs4218.impl.app.helper.UniqHelper.writeToFile;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;

public class UniqApplication implements UniqInterface {
    /**
     * Runs the uniq application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws UniqException If the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws UniqException {
        // Format: uniq [-Options] [INPUT_FILE [OUTPUT_FILE]]
        if (args == null) {
            throw new UniqException(E_NULL_POINTER);
        }
        if (stdin == null) {
            throw new UniqException(E_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new UniqException(E_NO_OSTREAM);
        }

        UniqArgsParser uniqArgs = parseArgs(args);
        String output = "";
        List<String> files = uniqArgs.getFiles();
        String inputFileName = getInputOutputFiles(files)[0];
        String outputFileName = getInputOutputFiles(files)[1];

        try {
            if (uniqArgs.hasInputFile() && !"-".equals(inputFileName)) {
                output = uniqFromFile(uniqArgs.isCount(), uniqArgs.isRepeated(), uniqArgs.isAllRepeated(),
                        inputFileName, outputFileName);
            } else {
                output = uniqFromStdin(uniqArgs.isCount(), uniqArgs.isRepeated(), uniqArgs.isAllRepeated(),
                        stdin, outputFileName);
            }
        } catch (UniqException e) {
            throw e;
        }

        try {
            if (!output.isEmpty()) {
                if (outputFileName == null) {
                    stdout.write(output.getBytes());
                } else {
                    writeToFile(output, outputFileName);
                }
            }
        } catch (IOException e) {
            throw new UniqException(E_WRITE_STREAM, e);
        }
    }

    /**
     * Get input filename and output filename
     *
     * @param files List of String of filenames
     * @throws UniqException
     */
    protected String[] getInputOutputFiles(List<String> files) throws UniqException {
        try {
            String inputFileName = null;
            String outputFileName = null;

            if (files.size() == 1) {
                inputFileName = files.get(0);
            }
            if (files.size() == 2) {
                inputFileName = files.get(0);
                outputFileName = files.get(1);
            }

            return new String[]{inputFileName, outputFileName};

        } catch (Exception e) {
            throw new UniqException(e.getMessage(), e);
        }
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @throws UniqException
     */
    protected UniqArgsParser parseArgs(String... args) throws UniqException {
        UniqArgsParser parser = new UniqArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new UniqException(e.getMessage(), e);
        }
        return parser;
    }

    /**
     * Filters adjacent matching lines from INPUT_FILE or standard input and writes to an OUTPUT_FILE or to standard output.
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated     Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param inputFileName  of path to input file
     * @param outputFileName of path to output file (if any)
     * @throws Exception
     */
    public String uniqFromFile(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, String inputFileName, String outputFileName) throws UniqException {
        try {
            Path path = IOUtils.resolveFilePath(inputFileName);

            validatePath(path);

            InputStream input = IOUtils.openInputStream(inputFileName);

            if (input.available() <= 0) {
                IOUtils.closeInputStream(input);
                return StringUtils.STRING_NEWLINE;
            }

            String output = uniqFromStdin(isCount, isRepeated, isAllRepeated, input, outputFileName);
            IOUtils.closeInputStream(input);

            return output;

        } catch (UniqException e) {
            throw e;
        } catch (IOException | ShellException e) {
            throw new UniqException(e.getMessage(), e);
        }
    }


    /**
     * Filters adjacent matching lines from INPUT_FILE or standard input and writes to an OUTPUT_FILE or to standard output.
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated     Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param stdin          InputStream containing arguments from Stdin
     * @param outputFileName of path to output file (if any)
     * @throws Exception
     */
    public String uniqFromStdin(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, InputStream stdin, String outputFileName) throws UniqException {
        try {
            List<String> result;
            StringBuilder stringBuilder = new StringBuilder();
            result = removeDuplicateLines(stdin, true);

            if (isRepeated || isAllRepeated) {
                result = getDuplicateLines(result, isRepeated, isAllRepeated);
            }

            for (String line : result) {
                if (isCount) {
                    stringBuilder.append('\t');
                    stringBuilder.append(line);
                } else {
                    stringBuilder.append(line.split("\\s+", 2)[1]);
                }
                stringBuilder.append(StringUtils.STRING_NEWLINE);
            }

            return stringBuilder.toString();

        } catch (UniqException e) {
            throw e;
        } catch (Exception e) {
            throw new UniqException(e.getMessage(), e);
        }
    }


}
