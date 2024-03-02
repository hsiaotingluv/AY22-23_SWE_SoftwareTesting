package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.app.helper.GrepHelper;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.ErrorConstants;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
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
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_EMPTY_PATTERN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class GrepApplicationTest {

    private static final String HELLO1 = "hello";
    private static final String HELLO2 = "HELLO";
    private static final String HELLO3 = "heLLo";
    private static final String HEY = "hey";
    private static final String DASH = "-";
    private static final String C_FLAG = "-c";
    private static final String I_FLAG = "-i";
    private static final String H_FLAG = "-H";
    private static final String FILE_NAME1 = "file1.txt";
    private static final String STANDARD_INPUT = "(standard input)";
    private GrepApplication grepApplication;

    @BeforeEach
    void setUp() {
        this.grepApplication = new GrepApplication();
    }

    @Test
    void checkRunException_withValidPatternFilesInputOutputStream_runsSuccessfully() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(
                () -> GrepHelper.checkRunExceptions(HELLO1,
                        new ArrayList<>(Arrays.asList(FILE_NAME1)),
                        System.in, outputStream)
        );
    }

    @Test
    void checkRunExceptions_withNoInputNoFilesStream_throwsNoInputException() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Throwable exception = assertThrows(GrepException.class,
                () -> GrepHelper.checkRunExceptions(HELLO1,
                        new ArrayList<>(),
                        null, outputStream)
        );
        assertEquals(new GrepException(E_NO_INPUT).getMessage(), exception.getMessage());
    }

    @Test
    void checkRunExceptions_withNoPattern_throwsInvalidSyntaxException() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Throwable exception = assertThrows(GrepException.class,
                () -> GrepHelper.checkRunExceptions(null,
                        new ArrayList<>(Arrays.asList(FILE_NAME1)),
                        System.in, outputStream)
        );
        assertEquals(new GrepException(E_SYNTAX).getMessage(), exception.getMessage());
    }

    @Test
    void checkRunExceptions_withNoOutputStream_throwsNoOutputStreamException() {
        Throwable exception = assertThrows(GrepException.class,
                () -> GrepHelper.checkRunExceptions(HELLO1,
                        new ArrayList<>(Arrays.asList(FILE_NAME1)),
                        System.in, null)
        );
        assertEquals(new GrepException(E_NO_OSTREAM).getMessage(), exception.getMessage());
    }

    @Test
    void checkRunExceptions_withNoInputStream_throwsNoInputStreamException() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Throwable exception = assertThrows(GrepException.class,
                () -> GrepHelper.checkRunExceptions(HELLO1,
                        new ArrayList<>(Arrays.asList(FILE_NAME1)),
                        null, outputStream)
        );
        assertEquals(new GrepException(E_NO_ISTREAM).getMessage(), exception.getMessage());
    }

    @Test
    void run_withEmptyPattern_throwsEmptyPatternException() throws Exception {
        String pattern = "";
        String inputString = HELLO1;

        String[] inputArgs = new String[]{C_FLAG, pattern};

        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        GrepApplication grepAppSpy = spy(grepApplication);
        GrepArgsParser mockGrepParser = mock(GrepArgsParser.class);

        doReturn(mockGrepParser)
                .when(grepAppSpy)
                .parseArgs(inputArgs);

        when(mockGrepParser.isCaseInsensitive()).thenReturn(false);
        when(mockGrepParser.isCount()).thenReturn(true);
        when(mockGrepParser.isPrintFileName()).thenReturn(false);
        when(mockGrepParser.getPattern()).thenReturn("");
        when(mockGrepParser.getFileNames()).thenReturn(new String[0]);

        Throwable exception = assertThrows(GrepException.class,
                () -> grepAppSpy.run(inputArgs, inputStream, outputStream)
        );
        assertEquals(new GrepException(E_EMPTY_PATTERN).getMessage(), exception.getMessage());

    }

    @Test
    void run_withValidFlagValidPatternValidFile_usesGrepFromFilesWritesToStdOutSuccessfully(@TempDir Path tempDir) throws Exception {

        String pattern = HELLO1;
        String fileName = FILE_NAME1;
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, Arrays.asList(pattern));

        String[] inputArgs = new String[]{C_FLAG, pattern, fileName};

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        OutputStream outputStream = new ByteArrayOutputStream();

        GrepApplication grepAppSpy = spy(grepApplication);
        GrepArgsParser mockGrepParser = mock(GrepArgsParser.class);

        doReturn(mockGrepParser)
                .when(grepAppSpy)
                .parseArgs(inputArgs);

        when(mockGrepParser.isCaseInsensitive()).thenReturn(false);
        when(mockGrepParser.isCount()).thenReturn(true);
        when(mockGrepParser.isPrintFileName()).thenReturn(false);
        when(mockGrepParser.getPattern()).thenReturn(pattern);
        when(mockGrepParser.getFileNames()).thenReturn(new String[]{fileName});

        doReturn("1").when(grepAppSpy).grepFromFiles(eq(pattern), eq(false), eq(true), eq(false), eq(fileName));

        assertDoesNotThrow(
                () -> grepAppSpy.run(inputArgs, inputStream, outputStream)
        );

        verify(grepAppSpy).parseArgs(eq(inputArgs));
        verify(mockGrepParser).isPrintFileName();
        verify(mockGrepParser).isCaseInsensitive();
        verify(mockGrepParser).isCount();
        verify(mockGrepParser).getFileNames();
        verify(mockGrepParser).getPattern();
        verify(grepAppSpy).grepFromFiles(eq(pattern), eq(false), eq(true), eq(false), eq(fileName));

        String expected = "1";
        assertEquals(expected, outputStream.toString().trim());
    }

    @Test
    void run_withValidFlagValidPatternNoFileNoDash_usesGrepFromStdInWritesToStdOutSuccessfully() throws Exception {
        String pattern = HELLO1;
        String inputString = HELLO1 + STRING_NEWLINE + HELLO1;

        String[] inputArgs = new String[]{C_FLAG, pattern};

        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        GrepApplication grepAppSpy = spy(grepApplication);
        GrepArgsParser mockGrepParser = mock(GrepArgsParser.class);

        doReturn(mockGrepParser)
                .when(grepAppSpy)
                .parseArgs(inputArgs);

        when(mockGrepParser.isCaseInsensitive()).thenReturn(false);
        when(mockGrepParser.isCount()).thenReturn(true);
        when(mockGrepParser.isPrintFileName()).thenReturn(false);
        when(mockGrepParser.getPattern()).thenReturn(pattern);
        when(mockGrepParser.getFileNames()).thenReturn(new String[0]);

        doReturn("2").when(grepAppSpy).grepFromStdin(eq(pattern), eq(false), eq(true), eq(false), eq(inputStream));

        assertDoesNotThrow(
                () -> grepAppSpy.run(inputArgs, inputStream, outputStream)
        );

        verify(grepAppSpy).parseArgs(eq(inputArgs));
        verify(mockGrepParser).isPrintFileName();
        verify(mockGrepParser).isCaseInsensitive();
        verify(mockGrepParser).isCount();
        verify(mockGrepParser).getFileNames();
        verify(mockGrepParser).getPattern();
        verify(grepAppSpy).grepFromStdin(eq(pattern), eq(false), eq(true), eq(false), eq(inputStream));

        String expected = "2";
        assertEquals(expected, outputStream.toString().trim());
    }

    @Test
    void run_withValidFlagsValidPatternValidFileWithStdin_usesGrepFromFileAndStdInWritesToStdOutSuccessfully(@TempDir Path tempDir) throws Exception {

        String pattern = HELLO3;
        String fileName = FILE_NAME1;
        String inputString = HELLO1 + STRING_NEWLINE + HELLO1;
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, Arrays.asList(HELLO1, HELLO2, HEY));

        String[] inputArgs = new String[]{I_FLAG, C_FLAG, H_FLAG, pattern, fileName, DASH};

        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        GrepApplication grepAppSpy = spy(grepApplication);
        GrepArgsParser mockGrepParser = mock(GrepArgsParser.class);

        doReturn(mockGrepParser)
                .when(grepAppSpy)
                .parseArgs(inputArgs);

        when(mockGrepParser.isCaseInsensitive()).thenReturn(true);
        when(mockGrepParser.isCount()).thenReturn(true);
        when(mockGrepParser.isPrintFileName()).thenReturn(true);
        when(mockGrepParser.getPattern()).thenReturn(pattern);
        when(mockGrepParser.getFileNames()).thenReturn(new String[]{fileName, DASH});

        String expected = fileName + ": " + 2 + STRING_NEWLINE + STANDARD_INPUT + ": " + 2;
        doReturn(expected)
                .when(grepAppSpy)
                .grepFromFileAndStdin(eq(pattern), eq(true), eq(true), eq(true), eq(inputStream), eq(new String[]{fileName, DASH}));

        assertDoesNotThrow(
                () -> grepAppSpy.run(inputArgs, inputStream, outputStream)
        );

        verify(grepAppSpy).parseArgs(eq(inputArgs));
        verify(mockGrepParser).isPrintFileName();
        verify(mockGrepParser).isCaseInsensitive();
        verify(mockGrepParser).isCount();
        verify(mockGrepParser).getFileNames();
        verify(mockGrepParser).getPattern();
        verify(grepAppSpy).grepFromFileAndStdin(eq(pattern), eq(true), eq(true), eq(true), eq(inputStream), eq(new String[]{fileName, DASH}));

        assertEquals(expected, outputStream.toString().trim());
    }

    @Test
    void run_withGrepFilesMethodThrowsException_throwsGrepException(@TempDir Path tempDir) throws Exception {
        String pattern = HELLO1;
        String fileName = FILE_NAME1;
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, Arrays.asList(pattern));

        String[] inputArgs = new String[]{C_FLAG, pattern, fileName};
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        OutputStream outputStream = new ByteArrayOutputStream();

        GrepApplication grepAppSpy = spy(grepApplication);
        GrepArgsParser mockGrepParser = mock(GrepArgsParser.class);

        doReturn(mockGrepParser)
                .when(grepAppSpy)
                .parseArgs(inputArgs);

        when(mockGrepParser.isCaseInsensitive()).thenReturn(false);
        when(mockGrepParser.isCount()).thenReturn(true);
        when(mockGrepParser.isPrintFileName()).thenReturn(false);
        when(mockGrepParser.getPattern()).thenReturn(pattern);
        when(mockGrepParser.getFileNames()).thenReturn(new String[]{fileName});

        doThrow(Exception.class)
                .when(grepAppSpy)
                .grepFromFiles(eq(pattern), eq(false), eq(true), eq(false), eq(fileName));

        assertThrows(GrepException.class,
                () -> grepAppSpy.run(inputArgs, inputStream, outputStream)
        );
    }

    @Test
    void run_withStdOutThrowsIOException_throwsIOGrepException(@TempDir Path tempDir) throws Exception {
        String pattern = HELLO1;
        String fileName = FILE_NAME1;
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, Arrays.asList(pattern));

        String[] inputArgs = new String[]{C_FLAG, pattern, fileName};
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int bytes) throws IOException {
                throw new IOException();
            }
        };

        GrepApplication grepAppSpy = spy(grepApplication);
        GrepArgsParser mockGrepParser = mock(GrepArgsParser.class);

        doReturn(mockGrepParser)
                .when(grepAppSpy)
                .parseArgs(inputArgs);

        when(mockGrepParser.isCaseInsensitive()).thenReturn(false);
        when(mockGrepParser.isCount()).thenReturn(true);
        when(mockGrepParser.isPrintFileName()).thenReturn(false);
        when(mockGrepParser.getPattern()).thenReturn(pattern);
        when(mockGrepParser.getFileNames()).thenReturn(new String[]{fileName});

        doReturn("1").when(grepAppSpy).grepFromFiles(eq(pattern), eq(false), eq(true), eq(false), eq(fileName));

        GrepException exception = assertThrows(GrepException.class,
                () -> grepAppSpy.run(inputArgs, inputStream, outputStream)
        );

        assertEquals(new GrepException(E_IO_EXCEPTION).getMessage(), exception.getMessage());
    }

    @Test
    void grepFromFileAndStdin_withValidFlagValidFileAndStdIn_returnsFilesAndStdInConcatenated(@TempDir Path tempDir) throws Exception {

        String pattern = HELLO3;
        String fileName = FILE_NAME1;
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, Arrays.asList(HELLO1, HELLO2, HEY));
        String count = "0";

        GrepApplication grepAppSpy = spy(grepApplication);
        String inputString = HELLO1 + STRING_NEWLINE + HELLO1;
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        doReturn(STANDARD_INPUT + ": " + count)
                .when(grepAppSpy)
                .grepFromStdin(eq(pattern), eq(false), eq(true), eq(true), eq(inputStream));

        doReturn(fileName + ": " + count + STRING_NEWLINE)
                .when(grepAppSpy)
                .grepFromFiles(eq(pattern), eq(false), eq(true), eq(true), eq(fileName));


        String actual = assertDoesNotThrow(
                () -> grepAppSpy.grepFromFileAndStdin(pattern, false, true, false, inputStream, FILE_NAME1, DASH)
        );

        verify(grepAppSpy).grepFromStdin(eq(pattern), eq(false), eq(true), eq(true), eq(inputStream));
        verify(grepAppSpy).grepFromFiles(eq(pattern), eq(false), eq(true), eq(true), eq(fileName));

        String expected = fileName + ": " + count + System.lineSeparator() + STANDARD_INPUT + ": " + count;
        assertEquals(expected, actual);
    }

    @Test
    void grepFromFileAndStdin_withEmptyPattern_throwsEmptyRegexGrepException(@TempDir Path tempDir) throws Exception {
        String fileName = FILE_NAME1;
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, Arrays.asList(HELLO1, HELLO2, HEY));

        GrepApplication grepAppSpy = spy(grepApplication);
        String inputString = HELLO1 + STRING_NEWLINE + HELLO1;
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        Throwable exception = assertThrows(GrepException.class,
                () -> grepAppSpy.grepFromFileAndStdin("", false, false, false, inputStream, fileName, DASH));

        assertEquals(new GrepException(ErrorConstants.E_EMPTY_PATTERN).getMessage(), exception.getMessage());
    }

    @Test
    void grepFromStdin_withEmptyPatternAn_throwsEmptyRegexGrepException() {

        GrepApplication grepAppSpy = spy(grepApplication);
        String inputString = HELLO1 + STRING_NEWLINE + HELLO1;
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        Throwable exception = assertThrows(GrepException.class,
                () -> grepAppSpy.grepFromStdin("", true, false, false, inputStream)
        );

        assertEquals(new GrepException(ErrorConstants.E_EMPTY_PATTERN).getMessage(), exception.getMessage());

    }

    @Test
    void grepFromStdin_withInvalidPattern_throwsrInvalidRegexGrepException() {

        GrepApplication grepAppSpy = spy(grepApplication);
        String inputString = HELLO1 + STRING_NEWLINE + HELLO1;
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        Throwable exception = assertThrows(GrepException.class,
                () -> grepAppSpy.grepFromStdin("[", false, false, false, inputStream));

        assertEquals(new GrepException(E_INVALID_REGEX).getMessage(), exception.getMessage());
    }

    @Test
    void grepFromStdin_withCAndHFlag_returnsFileNameWithPatternCount() {
        String pattern = HELLO3;

        GrepApplication grepAppSpy = spy(grepApplication);
        String inputString = HELLO1 + STRING_NEWLINE + HELLO1;
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());


        String actual = assertDoesNotThrow(
                () -> grepAppSpy.grepFromStdin(pattern, false, true, true, inputStream)
        );

        String expected = STANDARD_INPUT + ": " + 0 + STRING_NEWLINE;
        assertEquals(expected, actual);
    }

    @Test
    void grepFromStdin_withCAndIFlag_returnsCaseInsensitivePatternCount() {
        String pattern = HELLO3;

        GrepApplication grepAppSpy = spy(grepApplication);
        String inputString = HELLO1 + STRING_NEWLINE + HELLO1;
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());


        String actual = assertDoesNotThrow(
                () -> grepAppSpy.grepFromStdin(pattern, true, true, false, inputStream)
        );

        String expected = 2 + STRING_NEWLINE;
        assertEquals(expected, actual);
    }

    @Test
    void grepFromStdin_withHAndIFlag_returnsCaseInsensitivePatternCount() {
        String pattern = HELLO3;

        GrepApplication grepAppSpy = spy(grepApplication);
        String inputString = HELLO1 + STRING_NEWLINE + HELLO2;
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());


        String actual = assertDoesNotThrow(
                () -> grepAppSpy.grepFromStdin(pattern, true, false, true, inputStream)
        );

        String expected = STANDARD_INPUT + ": " + HELLO1 + STRING_NEWLINE + STANDARD_INPUT + ": " + HELLO2 + STRING_NEWLINE;
        assertEquals(expected, actual);
    }

    @Test
    void grepFromStdin_withAllFlagsAndStdIn_returnsFileNameWithCaseInsensitivePatternCount() {
        String pattern = HELLO3;

        GrepApplication grepAppSpy = spy(grepApplication);
        String inputString = HELLO1 + STRING_NEWLINE + HELLO1;
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());


        String actual = assertDoesNotThrow(
                () -> grepAppSpy.grepFromStdin(pattern, true, true, true, inputStream)
        );

        String expected = STANDARD_INPUT + ": " + 2 + STRING_NEWLINE;
        assertEquals(expected, actual);
    }

    @Test
    void grepFromFiles_withNullFilesNullPattern_throwsNullPointerGrepException() {

        GrepApplication grepAppSpy = spy(grepApplication);

        Throwable exception = assertThrows(GrepException.class,
                () -> grepAppSpy.grepFromFiles(null, true, false, false, null)
        );

        assertEquals(new GrepException(E_NULL_POINTER).getMessage(), exception.getMessage());
    }

    @Test
    void grepFromFiles_withEmptyPattern_throwsEmptyRegexGrepException() {

        GrepApplication grepAppSpy = spy(grepApplication);

        Throwable exception = assertThrows(GrepException.class,
                () -> grepAppSpy.grepFromFiles("", true, false, false, FILE_NAME1)
        );

        assertEquals(new GrepException(ErrorConstants.E_EMPTY_PATTERN).getMessage(), exception.getMessage());
    }

    @Test
    void grepFromFiles_withCFlagValidPatternValidFile_returnsPatternCount() {

        ArrayList<String> stringJoinerRes = new ArrayList<>(Arrays.asList("", "0"));

        try (MockedStatic<GrepHelper> mGrepHelper = mockStatic(GrepHelper.class, CALLS_REAL_METHODS)) {
            mGrepHelper.when(() -> GrepHelper.grepResultsFromFiles(HELLO3,
                    false, false,
                    FILE_NAME1)).thenReturn(stringJoinerRes);
            String expected = "0" + STRING_NEWLINE;

            String actual = assertDoesNotThrow(
                    () -> grepApplication.grepFromFiles(HELLO3, false, true, false, FILE_NAME1)
            );

            assertEquals(expected, actual);
        }
    }

    @Test
    void grepFromFiles_withHFlagValidPatternValidFile_returnsFileNameWithPatternMatching() {

        ArrayList<String> stringJoinerRes = new ArrayList<>(Arrays.asList("file1.txt: hello", "1"));


        try (MockedStatic<GrepHelper> mGrepHelper = mockStatic(GrepHelper.class, CALLS_REAL_METHODS)) {
            mGrepHelper.when(() -> GrepHelper.grepResultsFromFiles(HELLO3,
                    true, false,
                    FILE_NAME1)).thenReturn(stringJoinerRes);
            String expected = "file1.txt: hello" + STRING_NEWLINE;

            String actual = assertDoesNotThrow(
                    () -> grepApplication.grepFromFiles(HELLO3, false, false, true, FILE_NAME1)
            );

            assertEquals(expected, actual);
        }
    }

    @Test
    void grepFromFiles_withIFlagValidPatternValidFile_returnsCaseInsensitiveMatching() {

        ArrayList<String> stringJoinerRes = new ArrayList<>(Arrays.asList("hello", "1"));


        try (MockedStatic<GrepHelper> mGrepHelper = mockStatic(GrepHelper.class, CALLS_REAL_METHODS)) {
            mGrepHelper.when(() -> GrepHelper.grepResultsFromFiles(HELLO3,
                    false, true,
                    FILE_NAME1)).thenReturn(stringJoinerRes);
            String expected = "hello" + STRING_NEWLINE;

            String actual = assertDoesNotThrow(
                    () -> grepApplication.grepFromFiles(HELLO3, true, false, false, FILE_NAME1)
            );

            assertEquals(expected, actual);
        }
    }

    @Test
    void grepResultsFromFiles_withHAndIFlagValidPatternValidFile_returnsFileNameWithCaseInsensitivePatternMatching(@TempDir Path tempDir) throws Exception {

        String fileName = FILE_NAME1;
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, Arrays.asList(HELLO1, HELLO2));

        GrepApplication grepAppSpy = spy(grepApplication);
        ArrayList<String> expected = new ArrayList<>(
                Arrays.asList(
                        FILE_NAME1 + ": " + HELLO1 + STRING_NEWLINE + FILE_NAME1 + ": " + HELLO2,
                        FILE_NAME1 + ": " + 2
                )
        );

        StringJoiner linesJoiner = new StringJoiner(STRING_NEWLINE);
        StringJoiner countJoiner = new StringJoiner(STRING_NEWLINE);

        ArrayList<StringJoiner> expectedJoiner = new ArrayList<>(
                Arrays.asList(
                        linesJoiner.add(FILE_NAME1 + ": " + HELLO1).add(FILE_NAME1 + ": " + HELLO2),
                        countJoiner.add(FILE_NAME1 + ": " + 2)
                )
        );

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(FILE_NAME1))
                    .thenReturn(filePath);

            try (MockedStatic<GrepHelper> mGrepHelper = mockStatic(GrepHelper.class, CALLS_REAL_METHODS)) {
                mGrepHelper.when(() -> GrepHelper.buildResults(
                        any(BufferedReader.class),
                        anyString(),
                        any(Pattern.class),
                        anyBoolean(), anyBoolean(), any(StringJoiner.class), any(StringJoiner.class))).thenReturn(expectedJoiner);

                ArrayList<String> actual = assertDoesNotThrow(
                        () -> GrepHelper.grepResultsFromFiles(HELLO3, true, true, FILE_NAME1)
                );

                ioUtils.verify(() -> IOUtils.resolveFilePath(FILE_NAME1), times(1));
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    void grepResultsFromFiles_withHFlagValidPatternValidFile_returnsFileNameWithPatternMatching(@TempDir Path tempDir) throws Exception {

        String fileName = FILE_NAME1;
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, Arrays.asList(HELLO1, HELLO2));

        ArrayList<String> expected = new ArrayList<>(
                Arrays.asList(
                        "",
                        FILE_NAME1 + ": " + 2
                )
        );

        StringJoiner linesJoiner = new StringJoiner(STRING_NEWLINE);
        StringJoiner countJoiner = new StringJoiner(STRING_NEWLINE);

        ArrayList<StringJoiner> expectedJoiner = new ArrayList<>(
                Arrays.asList(
                        linesJoiner,
                        countJoiner.add(FILE_NAME1 + ": " + 2)
                )
        );

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(FILE_NAME1))
                    .thenReturn(filePath);

            try (MockedStatic<GrepHelper> mGrepHelper = mockStatic(GrepHelper.class, CALLS_REAL_METHODS)) {
                mGrepHelper.when(() -> GrepHelper.buildResults(
                        any(BufferedReader.class),
                        anyString(),
                        any(Pattern.class),
                        anyBoolean(), anyBoolean(), any(StringJoiner.class), any(StringJoiner.class))).thenReturn(expectedJoiner);

                ArrayList<String> actual = assertDoesNotThrow(
                        () -> GrepHelper.grepResultsFromFiles(HELLO3, true, false, FILE_NAME1)
                );

                ioUtils.verify(() -> IOUtils.resolveFilePath(FILE_NAME1), times(1));

                assertEquals(expected, actual);
            }
        }
    }

    @Test
    void grepResultsFromFiles_withInvalidFile_returnsFileNotFoundResult() {

        ArrayList<String> expected = new ArrayList<>(
                Arrays.asList(
                        FILE_NAME1 + ": " + E_FILE_NOT_FOUND,
                        FILE_NAME1 + ": " + E_FILE_NOT_FOUND
                )
        );

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(FILE_NAME1))
                    .thenReturn(Path.of("nonexistent"));

            ArrayList<String> actual = assertDoesNotThrow(
                    () -> GrepHelper.grepResultsFromFiles(HELLO3, true, true, FILE_NAME1)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(FILE_NAME1), times(1));

            assertEquals(expected, actual);
        }
    }

    @Test
    void grepResultsFromFiles_withValidFileAsDirectory_returnsFileIsDirectoryResult(@TempDir Path tempDir) throws Exception {

        String fileName = FILE_NAME1;
        Path filePath = tempDir.resolve(fileName);
        Files.createDirectory(filePath);

        ArrayList<String> expected = new ArrayList<>(
                Arrays.asList(
                        FILE_NAME1 + ": " + E_IS_DIR,
                        FILE_NAME1 + ": " + E_IS_DIR
                )
        );

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(FILE_NAME1))
                    .thenReturn(filePath);

            ArrayList<String> actual = assertDoesNotThrow(
                    () -> GrepHelper.grepResultsFromFiles(HELLO3, true, true, FILE_NAME1)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(FILE_NAME1), times(1));

            assertEquals(expected, actual);
        }
    }

    @Test
    void buildResults_withValidFilePattern_returnsLinesAndCountStringJoiners(@TempDir Path tempDir) throws Exception {

        String fileName = FILE_NAME1;
        Path filePath = tempDir.resolve(fileName);
        Files.createFile(filePath);
        Files.write(filePath, Arrays.asList(HELLO1, HELLO2));

        StringJoiner linesJoiner = new StringJoiner(STRING_NEWLINE);
        StringJoiner countJoiner = new StringJoiner(STRING_NEWLINE);

        ArrayList<StringJoiner> expected = new ArrayList<>(
                Arrays.asList(
                        linesJoiner,
                        countJoiner.add(FILE_NAME1 + ": " + 0)
                )
        );

        ArrayList<StringJoiner> actual = assertDoesNotThrow(
                () -> {
                    BufferedReader input = new BufferedReader(new FileReader(filePath.toString()));
                    ArrayList<StringJoiner> results = GrepHelper.buildResults(
                            input,
                            FILE_NAME1, Pattern.compile(HELLO3),
                            true, true,
                            new StringJoiner(STRING_NEWLINE), new StringJoiner(STRING_NEWLINE));
                    input.close();
                    return results;
                });

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            StringJoiner sj1 = expected.get(i);
            StringJoiner sj2 = actual.get(i);
            assertEquals(sj1.toString(), sj2.toString());
        }
    }
}
