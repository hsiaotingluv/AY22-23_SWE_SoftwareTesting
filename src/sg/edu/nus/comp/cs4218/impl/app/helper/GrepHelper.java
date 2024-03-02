package sg.edu.nus.comp.cs4218.impl.app.helper;

import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.util.ErrorConstants;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public final class GrepHelper {

    private GrepHelper() {
    }

    /**
     * @param reader           reader that reads lines from file
     * @param file             file from which lines are being read
     * @param compiledPattern  pattern that is being matched for with lines in the file
     * @param isSingleFile     boolean operation that determines if file name should be added to result
     * @param isPrefixFileName boolean operation that determines if file name should be added to result
     * @param lineResults      results containing line that is matched with and file name
     * @param countResults     results containing count of lines that are matched with and file name
     * @throws IOException if there is an error while reading lines from file
     */
    public static ArrayList<StringJoiner> buildResults(BufferedReader reader, String file, Pattern compiledPattern, boolean isSingleFile, boolean isPrefixFileName,
                                                       StringJoiner lineResults, StringJoiner countResults) throws IOException {
        int count = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = compiledPattern.matcher(line);
            if (matcher.find()) { // match
                if (isSingleFile) {
                    if (isPrefixFileName) {
                        lineResults.add(file + ": " + line);
                    } else {
                        lineResults.add(line);
                    }
                } else {
                    lineResults.add(file + ": " + line);
                }
                count++;
            }
        }
        if (!isPrefixFileName && isSingleFile) {
            countResults.add("" + count);
        } else {
            countResults.add(file + ": " + count);
        }
        return new ArrayList<>(Arrays.asList(lineResults, countResults));
    }

    /**
     * Returns an ArrayList of lineResults and countResults. The lines and count number of lines for grep from files and insert them into
     * lineResults and countResults respectively.
     *
     * @param pattern           supplied by user
     * @param isCaseInsensitive supplied by user
     * @param fileNames         a String Array of file names supplied by user
     */
    public static ArrayList<String> grepResultsFromFiles(String pattern, Boolean isPrefixFileName, Boolean isCaseInsensitive, String... fileNames) throws Exception {
        if (StringUtils.isBlank(pattern)) {
            throw new GrepException(ErrorConstants.E_EMPTY_PATTERN);
        }
        boolean isSingleFile = (fileNames.length == 1);
        StringJoiner lineResults = new StringJoiner(STRING_NEWLINE);
        StringJoiner countResults = new StringJoiner(STRING_NEWLINE);
        for (String f : fileNames) {
            BufferedReader reader = null;
            try {
                File file = IOUtils.resolveFilePath(f).toFile();
                if (!file.exists()) {
                    lineResults.add(f + ": " + E_FILE_NOT_FOUND);
                    countResults.add(f + ": " + E_FILE_NOT_FOUND);
                    continue;
                }
                if (file.isDirectory()) { // ignore if it's a directory
                    lineResults.add(f + ": " + E_IS_DIR);
                    countResults.add(f + ": " + E_IS_DIR);
                    continue;
                }
                reader = new BufferedReader(new FileReader(file));
                Pattern compiledPattern;
                if (isCaseInsensitive) {
                    compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                } else {
                    compiledPattern = Pattern.compile(pattern);
                }
                ArrayList<StringJoiner> stringJoinRes = buildResults(reader, f, compiledPattern, isSingleFile, isPrefixFileName, lineResults, countResults);
                lineResults = stringJoinRes.get(0);
                countResults = stringJoinRes.get(1);
                reader.close();
            } catch (PatternSyntaxException pse) {
                throw new GrepException(E_INVALID_REGEX, pse);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        return new ArrayList<>(Arrays.asList(lineResults.toString(), countResults.toString()));
    }

    /**
     * Checks for exceptions as a result of pattern, files, or input and output streams
     *
     * @param pattern    The pattern with which lines on the file will be matched with.
     * @param inputFiles A list of files that will be read from.
     * @param stdin      An InputStream. The input for the command is read from this InputStream if no files are specified.
     * @param stdout     An OutputStream. The output of the command is written to this OutputStream.
     * @throws Exception if streams are null, files are empty or pattern is null
     */
    public static void checkRunExceptions(String pattern, ArrayList<String> inputFiles, InputStream stdin, OutputStream stdout) throws GrepException {
        if (stdin == null && inputFiles.isEmpty()) {
            throw new GrepException(E_NO_INPUT);
        }
        if (pattern == null) {
            throw new GrepException(E_SYNTAX);
        }

        if (stdin == null) {
            throw new GrepException(E_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new GrepException(E_NO_OSTREAM);
        }
    }
}
