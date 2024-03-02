package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_READING_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CatApplicationTest {
    private CatApplication catApplication;
    private ByteArrayInputStream testInputStream;
    private ByteArrayOutputStream testOutputStream;
    private List<String> inputArray;
    private String[] testFileNames;
    private String[] testFilesWithDash;
    private String[] argsFilesAndStdin;

    @BeforeEach
    public void init() {
        catApplication = new CatApplication();

        // Constants
        String inputString = "This is the first line" + System.lineSeparator() + "This is the second line." + System.lineSeparator();
        inputArray = Arrays.asList(inputString.split(System.lineSeparator()));

        testFileNames = new String[]{"file.txt", "file2.txt"};
        testFilesWithDash = new String[]{"file.txt", "-"};
        argsFilesAndStdin = new String[]{"-n", "-", "file.txt"};

        testInputStream = new ByteArrayInputStream(inputString.getBytes());
        testOutputStream = new ByteArrayOutputStream();
    }

    private String createExpectedResultForPrefixedOutputs(List<String> lines) {
        int lineNumber = 1;
        List<String> expectedList = new ArrayList<>();
        for (String line : lines) {
            expectedList.add("\t" + lineNumber + " " + line);
            lineNumber++;
        }
        return String.join(STRING_NEWLINE, expectedList);
    }

    private String createExpectedResultForNonPrefixedOutputs(List<String> lines) {
        List<String> expectedList = new ArrayList<>(lines);
        return String.join(STRING_NEWLINE, expectedList);
    }

    @Test
    public void run_emptyStdout_throwsCatException() {
        CatException exception = assertThrows(CatException.class,
                () -> catApplication.run(null, null, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void run_validInputsWithPrefixFlag_usesFilesAndOrStdinAndWritesToStdoutSuccessfully() throws Exception {
        System.setOut(new PrintStream(testOutputStream));

        CatApplication catAppSpy = spy(catApplication);
        CatArgsParser mockCatParser = mock(CatArgsParser.class);

        doReturn(mockCatParser)
                .when(catAppSpy)
                .parseArgs(argsFilesAndStdin);

        when(mockCatParser.isLineNumber()).thenReturn(true);
        when(mockCatParser.getFiles()).thenReturn(testFileNames);

        doReturn(createExpectedResultForPrefixedOutputs(inputArray))
                .when(catAppSpy)
                .catFileAndStdin(eq(true), eq(testInputStream), eq(testFileNames));

        assertDoesNotThrow(
                () -> catAppSpy.run(argsFilesAndStdin, testInputStream, testOutputStream)
        );

        verify(catAppSpy).parseArgs(eq(argsFilesAndStdin));
        verify(mockCatParser).isLineNumber();
        verify(mockCatParser).getFiles();
        verify(catAppSpy).catFileAndStdin(eq(true), eq(testInputStream), eq(testFileNames));

        String expected = createExpectedResultForPrefixedOutputs(inputArray);

        assertEquals(expected, testOutputStream.toString().stripTrailing());
    }

    @Test
    public void run_validInputsWithoutPrefixFlag_usesFilesAndOrStdinAndWritesToStdoutSuccessfully() throws Exception {
        System.setOut(new PrintStream(testOutputStream));

        CatApplication catAppSpy = spy(catApplication);
        CatArgsParser mockCatParser = mock(CatArgsParser.class);

        doReturn(mockCatParser)
                .when(catAppSpy)
                .parseArgs(argsFilesAndStdin);

        when(mockCatParser.isLineNumber()).thenReturn(false);
        when(mockCatParser.getFiles()).thenReturn(testFileNames);

        doReturn(createExpectedResultForPrefixedOutputs(inputArray))
                .when(catAppSpy)
                .catFileAndStdin(eq(false), eq(testInputStream), eq(testFileNames));

        assertDoesNotThrow(
                () -> catAppSpy.run(argsFilesAndStdin, testInputStream, testOutputStream)
        );

        verify(catAppSpy).parseArgs(eq(argsFilesAndStdin));
        verify(mockCatParser).isLineNumber();
        verify(mockCatParser).getFiles();
        verify(catAppSpy).catFileAndStdin(eq(false), eq(testInputStream), eq(testFileNames));

        String expected = createExpectedResultForPrefixedOutputs(inputArray);

        assertEquals(expected, testOutputStream.toString().stripTrailing());
    }

    @Test
    public void run_catFromFileAndStdinError_throwsCatException() throws Exception {
        CatApplication catAppSpy = spy(catApplication);
        CatArgsParser mockCatParser = mock(CatArgsParser.class);

        doReturn(mockCatParser)
                .when(catAppSpy)
                .parseArgs(argsFilesAndStdin);

        when(mockCatParser.isLineNumber()).thenReturn(true);
        when(mockCatParser.getFiles()).thenReturn(testFileNames);

        doThrow(Exception.class)
                .when(catAppSpy)
                .catFileAndStdin(eq(true), eq(testInputStream), eq(testFileNames));

        assertThrows(Exception.class,
                () -> catAppSpy.run(argsFilesAndStdin, testInputStream, testOutputStream)
        );

        verify(catAppSpy).parseArgs(eq(argsFilesAndStdin));
        verify(mockCatParser).isLineNumber();
        verify(mockCatParser).getFiles();
        verify(catAppSpy).catFileAndStdin(eq(true), eq(testInputStream), eq(testFileNames));
    }

    @Test
    public void run_stdOutWritesThrowsIOException_throwsCatException() throws Exception {
        CatApplication catAppSpy = spy(catApplication);
        CatArgsParser mockCatParser = mock(CatArgsParser.class);

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

        doReturn(mockCatParser)
                .when(catAppSpy)
                .parseArgs(argsFilesAndStdin);

        when(mockCatParser.isLineNumber()).thenReturn(true);
        when(mockCatParser.getFiles()).thenReturn(new String[]{"-"});

        doReturn(createExpectedResultForPrefixedOutputs(inputArray))
                .when(catAppSpy)
                .catFileAndStdin(eq(true), eq(testInputStream), eq(new String[]{"-"}));

        CatException exception = assertThrows(CatException.class,
                () -> catAppSpy.run(argsFilesAndStdin, testInputStream, exOutputStream)
        );

        verify(catAppSpy).parseArgs(eq(argsFilesAndStdin));
        verify(mockCatParser).isLineNumber();
        verify(mockCatParser).getFiles();
        verify(catAppSpy).catFileAndStdin(eq(true), eq(testInputStream), eq(new String[]{"-"}));
        assertTrue(exception.getMessage().contains(E_WRITE_STREAM));
    }

    @Test
    public void catFiles_validFileNameWithoutLineNumber_catSuccessfully(@TempDir Path tempDir) throws Exception {
        CatApplication catAppSpy = spy(catApplication);

        String fileName = "catFiles_validFileNameWithoutLineNumber_catSuccessfully.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, inputArray);
        FileInputStream exFileInputStream = new FileInputStream(filePath.toString());

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            ioUtils.when(() -> IOUtils.openInputStream(fileName))
                    .thenReturn(exFileInputStream);

            doReturn(Collections.singletonList(createExpectedResultForNonPrefixedOutputs(inputArray)))
                    .when(catAppSpy)
                    .getAndPrefixLinesFromInputStream(eq(exFileInputStream), eq(false));

            String actual = assertDoesNotThrow(
                    () -> catAppSpy.catFiles(false, fileName)
            );

            verify(catAppSpy).getAndPrefixLinesFromInputStream(eq(exFileInputStream), eq(false));

            String expected = createExpectedResultForNonPrefixedOutputs(inputArray);

            assertTrue(Files.exists(filePath));
            assertEquals(expected + STRING_NEWLINE, actual);
        }

        exFileInputStream.close();
    }

    @Test
    public void catFiles_nullFileName_throwsCatException() {
        CatException exception = assertThrows(CatException.class,
                () -> catApplication.catFiles(false, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_ARGS));
    }

    @Test
    public void catFiles_resolveFilePathThrowsError_throwsCatException() {
        CatApplication catAppSpy = spy(catApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenThrow(ShellException.class);

            assertThrows(CatException.class, () ->
                    catAppSpy.catFiles(false, "file.txt")
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(anyString()));
        }
    }

    @Test
    public void catFiles_invalidFileName_throwsCatException(@TempDir Path tempDir) {
        CatApplication catAppSpy = spy(catApplication);
        String fileName = "catFiles_invalidFileName_throwsCatException.txt";
        Path filePath = tempDir.resolve(fileName);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            CatException exception = assertThrows(CatException.class, () ->
                    catAppSpy.catFiles(false, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));

            assertTrue(exception.getMessage().contains(E_FILE_NOT_FOUND));
        }
    }

    @Test
    public void catFiles_fileIsADirectory_throwsCatException(@TempDir Path tempDir) {
        CatApplication catAppSpy = spy(catApplication);
        String fileName = "catFiles_fileIsADirectory_throwsCatException.txt";

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(tempDir);

            CatException exception = assertThrows(CatException.class, () ->
                    catAppSpy.catFiles(false, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));

            assertEquals("cat: " + tempDir.getFileName().toString() + ": " + E_IS_DIR, exception.getMessage());
        }
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    public void catFiles_fileCannotBeRead_throwsCatException(@TempDir Path tempDir) throws IOException {
        CatApplication catAppSpy = spy(catApplication);
        String fileName = "catFiles_fileCannotBeRead_throwsException.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.createFile(filePath);
        Files.setPosixFilePermissions(filePath, new HashSet<>(Collections.singletonList(PosixFilePermission.OWNER_WRITE)));

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            CatException exception = assertThrows(CatException.class, () ->
                    catAppSpy.catFiles(false, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));

            assertEquals("cat: " + fileName + ": " + E_NO_PERM, exception.getMessage());
        }
    }

    @Test
    public void catFiles_errorWithTryOpenInputStream_throwsCatException(@TempDir Path tempDir) throws IOException {
        CatApplication catAppSpy = spy(catApplication);
        String fileName = "catFiles_errorWithTryOpenInputStream_throwsCatException.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.createFile(filePath);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenReturn(filePath);

            ioUtils.when(() -> IOUtils.openInputStream(anyString()))
                    .thenThrow(ShellException.class);

            CatException exception = assertThrows(CatException.class, () ->
                    catAppSpy.catFiles(false, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(anyString()));
            ioUtils.verify(() -> IOUtils.openInputStream(anyString()));

            assertTrue(exception.getMessage().contains(E_READING_FILE));
        }
    }

    @Test
    public void catStdin_validStdinWithoutLineNumber_catSuccessfully() throws Exception {
        CatApplication catAppSpy = spy(catApplication);

        doReturn(Collections.singletonList(createExpectedResultForNonPrefixedOutputs(inputArray)))
                .when(catAppSpy)
                .getAndPrefixLinesFromInputStream(eq(testInputStream), eq(false));

        String output = assertDoesNotThrow(
                () -> catAppSpy.catStdin(false, testInputStream)
        );

        verify(catAppSpy).getAndPrefixLinesFromInputStream(eq(testInputStream), eq(false));

        String expected = createExpectedResultForNonPrefixedOutputs(inputArray);
        assertEquals(expected + STRING_NEWLINE, output);
    }

    @Test
    public void catStdin_getLinesFromInputStreamError_throwsCatException() throws Exception {
        CatApplication catAppSpy = spy(catApplication);

        doThrow(ShellException.class)
                .when(catAppSpy)
                .getAndPrefixLinesFromInputStream(eq(testInputStream), eq(false));

        CatException exception = assertThrows(CatException.class,
                () -> catAppSpy.catStdin(false, testInputStream)
        );

        verify(catAppSpy).getAndPrefixLinesFromInputStream(eq(testInputStream), eq(false));

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void catStdin_nullStdin_throwsCatException() {
        CatException exception = assertThrows(CatException.class,
                () -> catApplication.catStdin(false, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void catFileAndStdin_fileNameAndStdinSpecified_returnsFilesAndStdinConcatenated() throws Exception {
        CatApplication catAppSpy = spy(catApplication);

        doReturn(createExpectedResultForNonPrefixedOutputs(inputArray))
                .when(catAppSpy)
                .catStdin(eq(true), eq(testInputStream));

        doReturn(createExpectedResultForNonPrefixedOutputs(inputArray))
                .when(catAppSpy)
                .catFiles(eq(false), eq(testFileNames[0]));

        String actual = assertDoesNotThrow(
                () -> catAppSpy.catFileAndStdin(false, testInputStream, testFilesWithDash)
        );

        verify(catAppSpy).catStdin(eq(false), eq(testInputStream));
        verify(catAppSpy).catFiles(eq(false), eq(testFileNames[0]));

        String expected = createExpectedResultForNonPrefixedOutputs(inputArray)
                + System.lineSeparator()
                + createExpectedResultForNonPrefixedOutputs(inputArray);

        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    public void catFileAndStdin_fileNameSpecifiedOnly_returnsFilesConcatenated() throws Exception {
        CatApplication catAppSpy = spy(catApplication);

        doReturn(createExpectedResultForNonPrefixedOutputs(inputArray))
                .when(catAppSpy)
                .catFiles(eq(false), eq(testFileNames[0]));

        doReturn(createExpectedResultForNonPrefixedOutputs(inputArray))
                .when(catAppSpy)
                .catFiles(eq(false), eq(testFileNames[1]));

        String actual = assertDoesNotThrow(
                () -> catAppSpy.catFileAndStdin(false, testInputStream, testFileNames)
        );

        verify(catAppSpy).catFiles(eq(false), eq(testFileNames[0]));
        verify(catAppSpy).catFiles(eq(false), eq(testFileNames[1]));

        String expected = createExpectedResultForNonPrefixedOutputs(inputArray)
                + System.lineSeparator()
                + createExpectedResultForNonPrefixedOutputs(inputArray);

        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    public void catFileAndStdin_stdinSpecifiedOnly_returnsStdinConcatenated() throws Exception {
        CatApplication catAppSpy = spy(catApplication);

        doReturn(createExpectedResultForNonPrefixedOutputs(inputArray))
                .when(catAppSpy)
                .catStdin(eq(false), eq(testInputStream));

        String actual = assertDoesNotThrow(
                () -> catAppSpy.catFileAndStdin(false, testInputStream, "-")
        );

        verify(catAppSpy).catStdin(eq(false), eq(testInputStream));

        String expected = createExpectedResultForNonPrefixedOutputs(inputArray);

        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    public void getLinesFromInputStream_validInputStreamWithPrefix_returnsCorrectOutput() {
        CatApplication catAppSpy = spy(catApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArray);

            List<String> actual = assertDoesNotThrow(
                    () -> catAppSpy.getAndPrefixLinesFromInputStream(testInputStream, true)
            );

            String expected = createExpectedResultForPrefixedOutputs(inputArray);
            List<String> expectedList = Arrays.asList(expected.split(System.lineSeparator()));
            assertEquals(expectedList, actual);
        }
    }

    @Test
    public void getLinesFromInputStream_validInputStreamWithoutPrefix_returnsCorrectOutput() {
        CatApplication catAppSpy = spy(catApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArray);

            List<String> actual = assertDoesNotThrow(
                    () -> catAppSpy.getAndPrefixLinesFromInputStream(testInputStream, false)
            );

            String expected = createExpectedResultForNonPrefixedOutputs(inputArray);
            List<String> expectedList = Arrays.asList(expected.split(System.lineSeparator()));
            assertEquals(expectedList, actual);
        }
    }

    @Test
    public void getLinesFromInputStream_invalidInputStream_throwsShellException() {
        assertThrowsExactly(ShellException.class, () -> {
            InputStream inputStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }
            };
            catApplication.getAndPrefixLinesFromInputStream(inputStream, false);
        });
    }
}
