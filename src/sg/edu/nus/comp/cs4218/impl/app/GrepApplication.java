package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.GrepInterface;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.ErrorConstants;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static sg.edu.nus.comp.cs4218.impl.app.helper.GrepHelper.checkRunExceptions;
import static sg.edu.nus.comp.cs4218.impl.app.helper.GrepHelper.grepResultsFromFiles;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_EMPTY_PATTERN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class GrepApplication implements GrepInterface {

    /**
     * Returns result after searching for lines containing match to pattern present in file, depending on boolean options;
     *
     * @param pattern           String specifying a regular expression in JAVA format
     * @param isCaseInsensitive Boolean option to perform case insensitive matching
     * @param isCountLines      Boolean option to only write out a count of matched lines
     * @param isPrefixFileName  Boolean option to print file name with output lines
     * @param fileNames         Array of file names (not including "-" for reading from stdin)
     * @return
     * @throws Exception
     */
    @Override
    public String grepFromFiles(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, String... fileNames) throws Exception {
        if (fileNames == null || pattern == null) {
            throw new GrepException(E_NULL_POINTER);
        }

        if (StringUtils.isBlank(pattern)) {
            throw new GrepException(ErrorConstants.E_EMPTY_PATTERN);
        }

        ArrayList<String> stringJoinerRes = grepResultsFromFiles(pattern, isPrefixFileName, isCaseInsensitive, fileNames);

        String lineResults = stringJoinerRes.get(0);
        String countResults = stringJoinerRes.get(1);


        String results = "";
        if (isCountLines) {
            results = countResults + STRING_NEWLINE;
        } else {
            if (!lineResults.isEmpty()) {
                results = lineResults + STRING_NEWLINE;
            }
        }
        return results;
    }

    /**
     * Returns result after searching for lines containing match to pattern present in stdin,
     * based on boolean options.
     *
     * @param pattern           String specifying a regular expression in JAVA format
     * @param isCaseInsensitive Boolean option to perform case insensitive matching
     * @param isCountLines      Boolean option to only write out a count of matched lines
     * @param isPrefixFileName  Boolean option to print file name with output lines
     * @param stdin             InputStream containing arguments from Stdin
     * @return
     * @throws Exception if regex is invalid or file doesn't exist
     */
    @Override
    public String grepFromStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin) throws Exception {
        if (StringUtils.isBlank(pattern)) {
            throw new GrepException(ErrorConstants.E_EMPTY_PATTERN);
        }
        int count = 0;
        StringJoiner stringJoiner = new StringJoiner(STRING_NEWLINE);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            String line;
            Pattern compiledPattern;
            if (isCaseInsensitive) {
                compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } else {
                compiledPattern = Pattern.compile(pattern);
            }
            while ((line = reader.readLine()) != null) {
                Matcher matcher = compiledPattern.matcher(line);
                if (matcher.find()) {
                    if (isPrefixFileName) {
                        stringJoiner.add("(standard input): " + line);
                    } else {
                        stringJoiner.add(line);
                    }
                    count++;
                }
            }
            reader.close();
        } catch (PatternSyntaxException pse) {
            throw new GrepException(E_INVALID_REGEX, pse);
        } catch (NullPointerException npe) {
            throw new GrepException(E_FILE_NOT_FOUND, npe);
        }

        String results = "";
        if (isCountLines) {
            if (isPrefixFileName) {
                results += "(standard input): ";
            }
            results += count + STRING_NEWLINE;
        } else {
            if (!stringJoiner.toString().isEmpty()) {
                results = stringJoiner.toString() + STRING_NEWLINE;
            }
        }
        return results;
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @throws GrepException
     */
    protected GrepArgsParser parseArgs(String... args) throws GrepException {
        GrepArgsParser parser = new GrepArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new GrepException(E_SYNTAX, e);
        }
        return parser;
    }

    /**
     * Runs the grep application with the specified arguments.
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws GrepException If an I/O exception occurs or streams and/or arguments are not provided.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws GrepException {
        try {
            ArrayList<String> inputFiles;
            String result;

            GrepArgsParser grepArgsParser = parseArgs(args);

            String pattern = grepArgsParser.getPattern();
            inputFiles = new ArrayList<>(Arrays.asList(grepArgsParser.getFileNames()));

            checkRunExceptions(pattern, inputFiles, stdin, stdout);

            if (pattern.isEmpty()) {
                throw new GrepException(E_EMPTY_PATTERN);
            } else {
                if (inputFiles.isEmpty()) {
                    result = grepFromStdin(pattern, grepArgsParser.isCaseInsensitive(), grepArgsParser.isCount(), grepArgsParser.isPrintFileName(), stdin);
                } else {
                    String[] inputFilesArray = new String[inputFiles.size()];
                    inputFilesArray = inputFiles.toArray(inputFilesArray);
                    result = grepFromFileAndStdin(pattern, grepArgsParser.isCaseInsensitive(), grepArgsParser.isCount(), grepArgsParser.isPrintFileName(), stdin, inputFilesArray);
                }
            }
            stdout.write(result.getBytes());
        } catch (GrepException grepException) {
            throw grepException;
        } catch (IOException e) {
            throw new GrepException(E_IO_EXCEPTION, e);
        } catch (Exception e) {
            throw new GrepException(e.getMessage(), e);
        }
    }

    /**
     * Returns result after searching for lines containing match to pattern present in stdin and/or files,
     * based on boolean options.
     *
     * @param pattern           String specifying a regular expression in JAVA format
     * @param isCaseInsensitive Boolean option to perform case insensitive matching
     * @param isCountLines      Boolean option to only write out a count of matched lines
     * @param isPrefixFileName  Boolean option to print file name with output lines
     * @param stdin             InputStream containing arguments from Stdin
     * @param fileNames         Array of file names (including "-" for reading from stdin)
     * @return
     * @throws Exception if exception encountered in grepFromStdin or grepFromFiles
     */
    @Override
    public String grepFromFileAndStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin, String... fileNames) throws Exception {

        StringJoiner stringJoiner = new StringJoiner("");
        Boolean usePrefixFileName = (isPrefixFileName || fileNames.length > 1);

        try {
            for (String f : fileNames) {
                if (("-").equals(f)) {
                    stringJoiner.add(grepFromStdin(pattern, isCaseInsensitive, isCountLines, usePrefixFileName, stdin));
                } else {
                    stringJoiner.add(grepFromFiles(pattern, isCaseInsensitive, isCountLines, usePrefixFileName, f));
                }
            }
        } catch (GrepException e) {
            throw e;
        }

        String results = "";
        if (!stringJoiner.toString().isEmpty()) {
            results = stringJoiner.toString();
        }
        return results;
    }
}
