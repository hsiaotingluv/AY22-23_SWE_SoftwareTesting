package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.app.helper.WcHelper;
import sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_GENERAL;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

/**
 * Provides unit tests of WcApplication
 * <p>
 * Positive test cases:
 * - wc with separated prefix -c -lw and filename
 * - wc with separated prefix -lw and filename
 * - wc with separated prefix -cl and filename
 * - wc with separated prefix -cw and filename
 * - wc with all prefix -clw and no filename for stdin
 * - wc with no prefix and filenames - and filename
 * <p>
 * - wc with valid filename for countFromFiles
 * - wc with valid stdin for countFromStdin
 * - wc with valid stdin and filename for countFromFileAndStdin
 * - wc with -cl valid stdin and filename for countFromFileAndStdin
 * - wc with -cw valid stdin and filename for countFromFileAndStdin
 * - wc with -lw valid stdin and filename for countFromFileAndStdin
 * - wc with valid stdin for getCountReport
 * <p>
 * Negative test cases:
 * - wc with null arguments (throws null argument exception)
 * - wc with exception from countFromStdin (throws wc exception)
 * - wc with exception from stdout (throws output stream exception)
 * <p>
 * - wc with null filename for countFromFiles (throws exception)
 * - wc with invalid filename for countFromFiles (throws file not found exception)
 * - wc with directory as filename for countFromFiles (throws file not found exception)
 * - wc with filename with no read permission for countFromFiles (throws no permission exception)
 * - wc with null stdin for countFromStdin (throws exception)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class WcApplicationTest {
    private WcApplication wcApplication;
    private ByteArrayInputStream testInputStream;
    private ByteArrayOutputStream testOutputStream;
    private List<String> inputArray;
    private String[] testFileNames;
    private String[] testFilesWithDash;
    private String[] argsBLWFiles;
    private String[] argsBLFiles;
    private String[] argsBWFiles;
    private String[] argsLWFiles;
    private String[] argsBLWStdin;
    private String[] argsStdinFiles;

    @BeforeEach
    void init() {
        wcApplication = new WcApplication();

        // Constants
        String inputString = "This is the first line" + System.lineSeparator() + "This is the second line." + System.lineSeparator();
        inputArray = Arrays.asList(inputString.split(System.lineSeparator()));

        testFileNames = new String[]{"file.txt"};
        testFilesWithDash = new String[]{"-", "file.txt"};
        argsBLWFiles = new String[]{"-c -lw", "file.txt"};
        argsBLFiles = new String[]{"-cl", "file.txt"};
        argsBWFiles = new String[]{"-cw", "file.txt"};
        argsLWFiles = new String[]{"-lw", "file.txt"};
        argsBLWStdin = new String[]{"-clw"};
        argsStdinFiles = new String[]{"", "-", "file.txt"};

        testInputStream = new ByteArrayInputStream(inputString.getBytes());
        testOutputStream = new ByteArrayOutputStream();
    }

    /**
     * Returns string containing the number of lines, words, and bytes based on data in input.
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param fileName A String of filename
     * @param hasStdin Boolean option to count stdin
     * @param input    A list of String
     * @throws Exception
     */
    private String createExpectedResult(Boolean isBytes, Boolean isLines, Boolean isWords, String fileName, Boolean hasStdin, List<String> input) throws Exception {
        long[] result = new long[3]; // lines, words, bytes
        long[] totalResult = new long[3]; // total of lines, words, bytes
        String[] fileNames = fileName.split(" ");

        for (int i = 0; i < input.size(); i++) {
            ++result[0];
            ++result[1];
            result[2] += input.get(i).getBytes("UTF-8").length;

            char[] charac = input.get(i).toCharArray();
            for (int j = 0; j < charac.length; j++) {
                if (Character.isWhitespace(charac[j])) {
                    result[1] += 1;
                }
            }
        }
        ++result[2];

        totalResult[0] = result[0] * 2;
        totalResult[1] = result[1] * 2;
        totalResult[2] = result[2] * 2;

        StringBuilder output = new StringBuilder();
        String resultString = updateResult(isBytes, isLines, isWords, result, totalResult)[0];
        String totalSb = updateResult(isBytes, isLines, isWords, result, totalResult)[1];

        for (String file : fileNames) {
            if ("".equals(file)) {
                if (hasStdin) {
                    String line = resultString + STRING_NEWLINE;
                    output.append(line);
                }
            } else {
                String line = resultString + " " + file + STRING_NEWLINE;
                output.append(line);
            }
        }
        if (hasStdin && !(fileNames.length == 1 && (fileNames[0].equals("-") || fileNames[0].equals("")))) {
            String line = totalSb + " total" + STRING_NEWLINE;
            output.append(line);
        }

        return output.toString();
    }

    private String[] updateResult(Boolean isBytes, Boolean isLines, Boolean isWords, long[] result, long... totalResult) {
        StringBuilder resultString = new StringBuilder();
        StringBuilder totalSb = new StringBuilder();

        if (isLines) {
            resultString.append(CHAR_TAB);
            resultString.append(result[0]);
            totalSb.append(CHAR_TAB);
            totalSb.append(totalResult[0]);
        }
        if (isWords) {
            resultString.append(CHAR_TAB);
            resultString.append(result[1]);
            totalSb.append(CHAR_TAB);
            totalSb.append(totalResult[1]);
        }
        if (isBytes) {
            resultString.append(CHAR_TAB);
            resultString.append(result[2]);
            totalSb.append(CHAR_TAB);
            totalSb.append(totalResult[2]);
        }

        return new String[]{resultString.toString(), totalSb.toString()};
    }


    /**
     * Returns array containing the number of lines, words, and bytes based on data in input.
     *
     * @param input A list of String
     * @throws Exception
     */
    private long[] getExpectedCountReport(List<String> input) throws Exception {
        long[] result = new long[3]; // lines, words, bytes

        for (int i = 0; i < input.size(); i++) {
            ++result[0];
            ++result[1];
            result[2] += input.get(i).getBytes("UTF-8").length;

            char[] charac = input.get(i).toCharArray();
            for (int j = 0; j < charac.length; j++) {
                if (Character.isWhitespace(charac[j])) {
                    result[1] += 1;
                }
            }
        }

        ++result[2];

        return result;
    }

    /**
     * wc with null arguments (throws null argument exception)
     */
    @Test
    public void run_emptyStdout_throwsException() {
        WcApplication wcAppSpy = spy(wcApplication);

        WcException exception = assertThrows(WcException.class,
                () -> wcAppSpy.run(null, null, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    /**
     * wc with separated prefix -c -lw and filename
     */
    @Test
    public void run_allPrefixAndFilenameSpecified_usesFilenameAndWritesToStdoutSuccessfully() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);
        WcArgsParser mockWcParser = mock(WcArgsParser.class);

        doReturn(mockWcParser)
                .when(wcAppSpy)
                .parseArgs(argsBLWFiles);

        when(mockWcParser.isReadingFromStdinOnly()).thenReturn(false);
        when(mockWcParser.isLines()).thenReturn(true);
        when(mockWcParser.isWords()).thenReturn(true);
        when(mockWcParser.isBytes()).thenReturn(true);
        when(mockWcParser.getFiles()).thenReturn(List.of(testFileNames));

        doReturn(createExpectedResult(true, true, true, String.join("", testFileNames), false, inputArray))
                .when(wcAppSpy)
                .countFromFiles(eq(true), eq(true), eq(true), eq(testFileNames));

        assertDoesNotThrow(
                () -> wcAppSpy.run(argsBLWFiles, testInputStream, testOutputStream)
        );

        verify(wcAppSpy).parseArgs(eq(argsBLWFiles));
        verify(mockWcParser).isReadingFromStdinOnly();
        verify(mockWcParser).isLines();
        verify(mockWcParser).isWords();
        verify(mockWcParser).isBytes();
        verify(wcAppSpy).countFromFiles(eq(true), eq(true), eq(true), eq(testFileNames));

        String expected = createExpectedResult(true, true, true, String.join("", testFileNames), false, inputArray);

        assertEquals(expected, testOutputStream.toString());
    }

    /**
     * wc with separated prefix -lw and filename
     */
    @Test
    public void run_twoPrefixAndFilenameSpecified_returnLinesAndWords() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);
        WcArgsParser mockWcParser = mock(WcArgsParser.class);

        doReturn(mockWcParser)
                .when(wcAppSpy)
                .parseArgs(argsLWFiles);

        when(mockWcParser.isReadingFromStdinOnly()).thenReturn(false);
        when(mockWcParser.isLines()).thenReturn(true);
        when(mockWcParser.isWords()).thenReturn(true);
        when(mockWcParser.isBytes()).thenReturn(false);
        when(mockWcParser.getFiles()).thenReturn(List.of(testFileNames));

        doReturn(createExpectedResult(false, true, true, String.join("", testFileNames), false, inputArray))
                .when(wcAppSpy)
                .countFromFiles(eq(false), eq(true), eq(true), eq(testFileNames));

        assertDoesNotThrow(
                () -> wcAppSpy.run(argsLWFiles, testInputStream, testOutputStream)
        );

        verify(wcAppSpy).parseArgs(eq(argsLWFiles));
        verify(mockWcParser).isReadingFromStdinOnly();
        verify(mockWcParser).isLines();
        verify(mockWcParser).isWords();
        verify(mockWcParser).isBytes();
        verify(wcAppSpy).countFromFiles(eq(false), eq(true), eq(true), eq(testFileNames));

        String expected = createExpectedResult(false, true, true, String.join("", testFileNames), false, inputArray);

        assertEquals(expected, testOutputStream.toString());
    }

    /**
     * wc with separated prefix -cl and filename
     */
    @Test
    public void run_twoPrefixAndFilenameSpecified_returnBytesAndLines() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);
        WcArgsParser mockWcParser = mock(WcArgsParser.class);

        doReturn(mockWcParser)
                .when(wcAppSpy)
                .parseArgs(argsBLFiles);

        when(mockWcParser.isReadingFromStdinOnly()).thenReturn(false);
        when(mockWcParser.isLines()).thenReturn(true);
        when(mockWcParser.isWords()).thenReturn(false);
        when(mockWcParser.isBytes()).thenReturn(true);
        when(mockWcParser.getFiles()).thenReturn(List.of(testFileNames));

        doReturn(createExpectedResult(true, true, false, String.join("", testFileNames), false, inputArray))
                .when(wcAppSpy)
                .countFromFiles(eq(true), eq(true), eq(false), eq(testFileNames));

        assertDoesNotThrow(
                () -> wcAppSpy.run(argsBLFiles, testInputStream, testOutputStream)
        );

        verify(wcAppSpy).parseArgs(eq(argsBLFiles));
        verify(mockWcParser).isReadingFromStdinOnly();
        verify(mockWcParser).isLines();
        verify(mockWcParser).isWords();
        verify(mockWcParser).isBytes();
        verify(wcAppSpy).countFromFiles(eq(true), eq(true), eq(false), eq(testFileNames));

        String expected = createExpectedResult(true, true, false, String.join("", testFileNames), false, inputArray);

        assertEquals(expected, testOutputStream.toString());
    }

    /**
     * wc with separated prefix -cw and filename
     */
    @Test
    public void run_twoPrefixAndFilenameSpecified_returnBytesAndWords() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);
        WcArgsParser mockWcParser = mock(WcArgsParser.class);

        doReturn(mockWcParser)
                .when(wcAppSpy)
                .parseArgs(argsBWFiles);

        when(mockWcParser.isReadingFromStdinOnly()).thenReturn(false);
        when(mockWcParser.isLines()).thenReturn(false);
        when(mockWcParser.isWords()).thenReturn(true);
        when(mockWcParser.isBytes()).thenReturn(true);
        when(mockWcParser.getFiles()).thenReturn(List.of(testFileNames));

        doReturn(createExpectedResult(true, false, true, String.join("", testFileNames), false, inputArray))
                .when(wcAppSpy)
                .countFromFiles(eq(true), eq(false), eq(true), eq(testFileNames));

        assertDoesNotThrow(
                () -> wcAppSpy.run(argsBWFiles, testInputStream, testOutputStream)
        );

        verify(wcAppSpy).parseArgs(eq(argsBWFiles));
        verify(mockWcParser).isReadingFromStdinOnly();
        verify(mockWcParser).isLines();
        verify(mockWcParser).isWords();
        verify(mockWcParser).isBytes();
        verify(wcAppSpy).countFromFiles(eq(true), eq(false), eq(true), eq(testFileNames));

        String expected = createExpectedResult(true, false, true, String.join("", testFileNames), false, inputArray);

        assertEquals(expected, testOutputStream.toString());
    }

    /**
     * wc with all prefix -clw and no filename for stdin
     */
    @Test
    public void run_allPrefixAndStdinSpecified_usesStdinAndWritesToStdoutSuccessfully() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);
        WcArgsParser mockWcParser = mock(WcArgsParser.class);

        doReturn(mockWcParser)
                .when(wcAppSpy)
                .parseArgs(argsBLWStdin);

        when(mockWcParser.isReadingFromStdinOnly()).thenReturn(true);
        when(mockWcParser.isLines()).thenReturn(true);
        when(mockWcParser.isWords()).thenReturn(true);
        when(mockWcParser.isBytes()).thenReturn(true);
        when(mockWcParser.getFiles()).thenReturn(List.of());

        doReturn(createExpectedResult(true, true, true, "", true, inputArray))
                .when(wcAppSpy)
                .countFromStdin(true, true, true, testInputStream);

        assertDoesNotThrow(
                () -> wcAppSpy.run(argsBLWStdin, testInputStream, testOutputStream)
        );

        verify(wcAppSpy).parseArgs(eq(argsBLWStdin));
        verify(mockWcParser).isReadingFromStdinOnly();
        verify(mockWcParser).isLines();
        verify(mockWcParser).isWords();
        verify(mockWcParser).isBytes();
        verify(wcAppSpy).countFromStdin(true, true, true, testInputStream);

        String expected = createExpectedResult(true, true, true, "", true, inputArray);

        assertEquals(expected, testOutputStream.toString());
    }

    /**
     * wc with no prefix and filenames - and filename
     */
    @Test
    public void run_noPrefixFileNameAndStdinSpecified_usesFilesAndStdinAndWritesToStdoutSuccessfully() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);
        WcArgsParser mockWcParser = mock(WcArgsParser.class);

        doReturn(mockWcParser)
                .when(wcAppSpy)
                .parseArgs(argsStdinFiles);

        when(mockWcParser.isReadingFromStdinOnly()).thenReturn(false);
        when(mockWcParser.isLines()).thenReturn(true);
        when(mockWcParser.isWords()).thenReturn(true);
        when(mockWcParser.isBytes()).thenReturn(true);
        when(mockWcParser.getFiles()).thenReturn(List.of(testFileNames));

        doReturn(createExpectedResult(true, true, true, String.join("", testFileNames), true, inputArray))
                .when(wcAppSpy)
                .countFromFileAndStdin(eq(true), eq(true), eq(true), eq(testInputStream), eq(testFileNames));

        assertDoesNotThrow(
                () -> wcAppSpy.run(argsStdinFiles, testInputStream, testOutputStream)
        );

        verify(wcAppSpy).parseArgs(eq(argsStdinFiles));
        verify(mockWcParser).isReadingFromStdinOnly();
        verify(mockWcParser).isLines();
        verify(mockWcParser).isWords();
        verify(mockWcParser).isBytes();
        verify(wcAppSpy).countFromFileAndStdin(eq(true), eq(true), eq(true), eq(testInputStream), eq(testFileNames));

        String expected = createExpectedResult(true, true, true, String.join("", testFileNames), true, inputArray);

        assertEquals(expected, testOutputStream.toString());
    }

    /**
     * wc with exception from countFromStdin (throws wc exception)
     */
    @Test
    public void run_countFromStdinThrowsException_throwsWcException() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);
        WcArgsParser mockWcParser = mock(WcArgsParser.class);

        doReturn(mockWcParser)
                .when(wcAppSpy)
                .parseArgs(argsBLWStdin);

        when(mockWcParser.isReadingFromStdinOnly()).thenReturn(true);
        when(mockWcParser.isLines()).thenReturn(true);
        when(mockWcParser.isWords()).thenReturn(true);
        when(mockWcParser.isBytes()).thenReturn(true);

        doThrow(WcException.class)
                .when(wcAppSpy)
                .countFromStdin(eq(true), eq(true), eq(true), eq(testInputStream));

        assertThrows(WcException.class,
                () -> wcAppSpy.run(argsBLWStdin, testInputStream, testOutputStream)
        );

        verify(wcAppSpy).parseArgs(eq(argsBLWStdin));
        verify(mockWcParser).isReadingFromStdinOnly();
        verify(mockWcParser).isLines();
        verify(mockWcParser).isWords();
        verify(mockWcParser).isBytes();
        verify(wcAppSpy).countFromStdin(eq(true), eq(true), eq(true), eq(testInputStream));
    }

    /**
     * wc with exception from stdout (throws output stream exception)
     */
    @Test
    public void run_stdOutWritesThrowsIOException_throwsWcException() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);
        WcArgsParser mockWcParser = mock(WcArgsParser.class);

        OutputStream exOutputStream = new OutputStream() {
            @Override
            public void write(int bytes) throws IOException {
                throw new IOException();
            }

            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };

        doReturn(mockWcParser)
                .when(wcAppSpy)
                .parseArgs(argsBLWStdin);

        when(mockWcParser.isReadingFromStdinOnly()).thenReturn(true);
        when(mockWcParser.isLines()).thenReturn(true);
        when(mockWcParser.isWords()).thenReturn(true);
        when(mockWcParser.isBytes()).thenReturn(true);

        doReturn(createExpectedResult(true, true, true, "", true, inputArray))
                .when(wcAppSpy)
                .countFromStdin(eq(true), eq(true), eq(true), eq(testInputStream));

        WcException exception = assertThrows(WcException.class,
                () -> wcAppSpy.run(argsBLWStdin, testInputStream, exOutputStream)
        );

        verify(wcAppSpy).parseArgs(eq(argsBLWStdin));
        verify(mockWcParser).isReadingFromStdinOnly();
        verify(mockWcParser).isLines();
        verify(mockWcParser).isWords();
        verify(mockWcParser).isBytes();
        verify(wcAppSpy).countFromStdin(eq(true), eq(true), eq(true), eq(testInputStream));
        assertTrue(exception.getMessage().contains(E_WRITE_STREAM));
    }

    /**
     * wc with valid filename for countFromFiles
     */
    @Test
    public void countFromFiles_validFileName_wcSuccessfully(@TempDir Path tempDir) throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);
        String fileName = "countFromFiles_validFileName_wcSuccessfully.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, inputArray);
        FileInputStream exFileInputStream = new FileInputStream(filePath.toString());

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            ioUtils.when(() -> IOUtils.openInputStream(fileName))
                    .thenReturn(exFileInputStream);

            try (MockedStatic<WcHelper> mWcHelper = mockStatic(WcHelper.class, CALLS_REAL_METHODS)) {
                mWcHelper.when(() -> WcHelper.getCountReport(eq(exFileInputStream))).thenReturn(getExpectedCountReport(inputArray));

                String actual = assertDoesNotThrow(
                        () -> wcAppSpy.countFromFiles(true, true, true, fileName)
                );

                mWcHelper.verify(() -> WcHelper.getCountReport(eq(exFileInputStream)));

                String expected = createExpectedResult(true, true, true, fileName, false, inputArray);

                assertTrue(Files.exists(filePath));
                assertEquals(expected, actual);
            }
        }

        exFileInputStream.close();
    }

    /**
     * wc with null filename for countFromFiles (throws exception)
     */
    @Test
    public void countFromFiles_nullFileName_throwsException() {
        Exception exception = assertThrows(Exception.class,
                () -> wcApplication.countFromFiles(true, true, true, null)
        );

        assertTrue(exception.getMessage().contains(E_GENERAL));
    }

    /**
     * wc with invalid filename for countFromFiles (throws file not found exception)
     */
    @Test
    public void countFromFiles_invalidFileName_returnExceptionMessage() {
        WcApplication wcAppSpy = spy(wcApplication);
        File mockFile = mock(File.class);
        Path mockPath = mock(Path.class);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenReturn(mockPath);
            when(mockPath.toFile()).thenReturn(mockFile);
            when(mockFile.exists()).thenReturn(false);

            String actual = assertDoesNotThrow(() ->
                    wcAppSpy.countFromFiles(true, true, true, testFileNames)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(anyString()));
            verify(mockPath, times(1)).toFile();
            verify(mockFile, times(1)).exists();

            assertTrue(actual.contains(E_FILE_NOT_FOUND));
        }
    }

    /**
     * wc with directory as filename for countFromFiles (throws file not found exception)
     */
    @Test
    public void countFromFiles_fileIsADirectory_throwsException() {
        WcApplication wcAppSpy = spy(wcApplication);
        File mockFile = mock(File.class);
        Path mockPath = mock(Path.class);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenReturn(mockPath);
            when(mockPath.toFile()).thenReturn(mockFile);

            when(mockFile.exists()).thenReturn(true);
            when(mockFile.isDirectory()).thenReturn(true);

            String actual = assertDoesNotThrow(() ->
                    wcAppSpy.countFromFiles(true, true, true, testFileNames)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(anyString()));
            verify(mockPath, times(1)).toFile();
            verify(mockFile, times(1)).exists();
            verify(mockFile, times(1)).isDirectory();

            assertTrue(actual.contains(E_IS_DIR));
        }
    }

    /**
     * wc with filename with no read permission for countFromFiles (throws no permission
     */
    @Test
    public void countFromFiles_fileCannotBeRead_throwsException() {
        File mockFile = mock(File.class);
        Path mockPath = mock(Path.class);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenReturn(mockPath);
            when(mockPath.toFile()).thenReturn(mockFile);

            when(mockFile.exists()).thenReturn(true);
            when(mockFile.isDirectory()).thenReturn(false);
            when(mockFile.canRead()).thenReturn(false);

            String actual = assertDoesNotThrow(() ->
                    wcApplication.countFromFiles(true, true, true, testFileNames)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(anyString()));
            verify(mockPath, times(1)).toFile();
            verify(mockFile, times(1)).exists();
            verify(mockFile, times(1)).isDirectory();
            verify(mockFile, times(1)).canRead();

            assertTrue(actual.contains(E_NO_PERM));
        }
    }

    /**
     * wc with valid stdin for countFromStdin
     */
    @Test
    public void countFromStdin_validStdin_wcSuccessfully() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);

        try (MockedStatic<WcHelper> mWcHelper = mockStatic(WcHelper.class, CALLS_REAL_METHODS)) {
            mWcHelper.when(() -> WcHelper.getCountReport(eq(testInputStream))).thenReturn(getExpectedCountReport(inputArray));

            String output = assertDoesNotThrow(
                    () -> wcAppSpy.countFromStdin(true, true, true, testInputStream)
            );

            mWcHelper.verify(() -> WcHelper.getCountReport(eq(testInputStream)));

            String expected = createExpectedResult(true, true, true, "", true, inputArray);
            assertEquals(expected, output);
        }
    }

    /**
     * wc with null stdin for countFromStdin (throws exception)
     */
    @Test
    public void countFromStdin_nullStdin_throwsException() {
        Exception exception = assertThrows(Exception.class,
                () -> wcApplication.countFromStdin(true, true, true, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    /**
     * wc with valid stdin and filename for countFromFileAndStdin
     */
    @Test
    public void countFromFileAndStdin_fileNameSpecifiedWithStdin_returnsConcatenated() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);

        doReturn(createExpectedResult(true, true, true, "", true, inputArray))
                .when(wcAppSpy)
                .countFromStdin(eq(true), eq(true), eq(true), eq(testInputStream));

        doReturn(createExpectedResult(true, true, true, String.join("", testFileNames), false, inputArray))
                .when(wcAppSpy)
                .countFromFiles(eq(true), eq(true), eq(true), eq(testFileNames));

        String actual = assertDoesNotThrow(
                () -> wcAppSpy.countFromFileAndStdin(true, true, true, testInputStream, testFilesWithDash)
        );

        verify(wcAppSpy).countFromStdin(eq(true), eq(true), eq(true), eq(testInputStream));
        verify(wcAppSpy).countFromFiles(eq(true), eq(true), eq(true), eq(testFileNames));

        String expected = createExpectedResult(true, true, true, String.join(" ", testFilesWithDash), true, inputArray);

        assertEquals(expected, actual);
    }

    /**
     * wc with -cl valid stdin and filename for countFromFileAndStdin
     */
    @Test
    public void countFromFileAndStdin_fileNameSpecifiedWithStdin_returnsBytesAndLines() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);

        doReturn(createExpectedResult(true, true, false, "", true, inputArray))
                .when(wcAppSpy)
                .countFromStdin(eq(true), eq(true), eq(false), eq(testInputStream));

        doReturn(createExpectedResult(true, true, false, String.join("", testFileNames), false, inputArray))
                .when(wcAppSpy)
                .countFromFiles(eq(true), eq(true), eq(false), eq(testFileNames));

        String actual = assertDoesNotThrow(
                () -> wcAppSpy.countFromFileAndStdin(true, true, false, testInputStream, testFilesWithDash)
        );

        verify(wcAppSpy).countFromStdin(eq(true), eq(true), eq(false), eq(testInputStream));
        verify(wcAppSpy).countFromFiles(eq(true), eq(true), eq(false), eq(testFileNames));

        String expected = createExpectedResult(true, true, false, String.join(" ", testFilesWithDash), true, inputArray);

        assertEquals(expected, actual);
    }

    /**
     * wc with -cw valid stdin and filename for countFromFileAndStdin
     */
    @Test
    public void countFromFileAndStdin_fileNameSpecifiedWithStdin_returnsBytesAndWords() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);

        doReturn(createExpectedResult(true, false, true, "", true, inputArray))
                .when(wcAppSpy)
                .countFromStdin(eq(true), eq(false), eq(true), eq(testInputStream));

        doReturn(createExpectedResult(true, false, true, String.join("", testFileNames), false, inputArray))
                .when(wcAppSpy)
                .countFromFiles(eq(true), eq(false), eq(true), eq(testFileNames));

        String actual = assertDoesNotThrow(
                () -> wcAppSpy.countFromFileAndStdin(true, false, true, testInputStream, testFilesWithDash)
        );

        verify(wcAppSpy).countFromStdin(eq(true), eq(false), eq(true), eq(testInputStream));
        verify(wcAppSpy).countFromFiles(eq(true), eq(false), eq(true), eq(testFileNames));

        String expected = createExpectedResult(true, false, true, String.join(" ", testFilesWithDash), true, inputArray);

        assertEquals(expected, actual);
    }

    /**
     * wc with -lw valid stdin and filename for countFromFileAndStdin
     */
    @Test
    public void countFromFileAndStdin_fileNameSpecifiedWithStdin_returnsLinesAndWords() throws Exception {
        WcApplication wcAppSpy = spy(wcApplication);

        doReturn(createExpectedResult(false, true, true, "", true, inputArray))
                .when(wcAppSpy)
                .countFromStdin(eq(false), eq(true), eq(true), eq(testInputStream));

        doReturn(createExpectedResult(false, true, true, String.join("", testFileNames), false, inputArray))
                .when(wcAppSpy)
                .countFromFiles(eq(false), eq(true), eq(true), eq(testFileNames));

        String actual = assertDoesNotThrow(
                () -> wcAppSpy.countFromFileAndStdin(false, true, true, testInputStream, testFilesWithDash)
        );

        verify(wcAppSpy).countFromStdin(eq(false), eq(true), eq(true), eq(testInputStream));
        verify(wcAppSpy).countFromFiles(eq(false), eq(true), eq(true), eq(testFileNames));

        String expected = createExpectedResult(false, true, true, String.join(" ", testFilesWithDash), true, inputArray);

        assertEquals(expected, actual);
    }

    /**
     * wc with valid stdin for getCountReport
     */
    @Test
    public void getCountReport_validInputStreamWithPrefix_returnsCorrectOutput() throws Exception {
        long[] actual = assertDoesNotThrow(
                () -> WcHelper.getCountReport(testInputStream)
        );
        long[] expected = getExpectedCountReport(inputArray);

        StringBuilder actualStr = new StringBuilder();
        StringBuilder expectedStr = new StringBuilder();

        for (int i = 0; i < actual.length; i++) {
            actualStr.append(String.format(" ", actual[i]));
            expectedStr.append(String.format(" ", expected[i]));
        }

        assertEquals(expectedStr.toString(), actualStr.toString());
    }

}
