package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
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
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;


public class SortApplicationTest {
    private SortApplication sortApplication;
    private ByteArrayInputStream testInputStream;
    private ByteArrayOutputStream testOutputStream;
    private String inputString;
    private String inputStringOfNums;
    private List<String> inputArray;
    private String[] expectedFileNames;
    private String[] argsFiles;
    private String[] argsStdin;

    @BeforeEach
    public void init() {
        sortApplication = new SortApplication();

        // Constants
        inputString = "This is the first line." + System.lineSeparator() + "This is the 2nd line." + System.lineSeparator();
        inputStringOfNums = "-50" + System.lineSeparator() + "10" + System.lineSeparator()
                + "-20" + System.lineSeparator() + "100" + System.lineSeparator()
                + "Hello" + System.lineSeparator();
        inputArray = Arrays.asList(inputString.split(System.lineSeparator()));

        expectedFileNames = new String[]{"file.txt"};
        argsFiles = new String[]{"-f", "file.txt"};
        argsStdin = new String[]{"-f"};

        testInputStream = new ByteArrayInputStream(inputString.getBytes());
        testOutputStream = new ByteArrayOutputStream();
    }

    @Test
    public void run_emptyStdout_throwsException() {
        SortException exception = assertThrows(SortException.class,
                () -> sortApplication.run(null, null, null)
        );

        String actual = exception.getMessage();

        assertTrue(actual.contains(E_NULL_POINTER));
    }

    @Test
    public void run_noFileNamesSpecified_usesStdinAndWritesToStdoutSuccessfully() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        SortApplication sortAppSpy = spy(sortApplication);
        SortArgsParser mockSortParser = mock(SortArgsParser.class);

        doReturn(mockSortParser)
                .when(sortAppSpy)
                .parseArgs(argsStdin);

        when(mockSortParser.isFirstWordNumber()).thenReturn(false);
        when(mockSortParser.isReverseOrder()).thenReturn(false);
        when(mockSortParser.isCaseIndependent()).thenReturn(true);
        when(mockSortParser.isReadingFromStdin()).thenReturn(true);

        doReturn(inputString)
                .when(sortAppSpy)
                .sortFromStdin(eq(false), eq(false), eq(true), eq(testInputStream));

        assertDoesNotThrow(
                () -> sortAppSpy.run(argsStdin, testInputStream, outputStream)
        );

        verify(sortAppSpy).parseArgs(eq(argsStdin));
        verify(mockSortParser).isFirstWordNumber();
        verify(mockSortParser).isReverseOrder();
        verify(mockSortParser).isCaseIndependent();
        verify(mockSortParser).isReadingFromStdin();
        verify(sortAppSpy).sortFromStdin(eq(false), eq(false), eq(true), eq(testInputStream));

        assertEquals(inputString, outputStream.toString());
    }

    @Test
    public void run_fileNameSpecified_usesFilesAndWritesToStdoutSuccessfully() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        SortApplication sortAppSpy = spy(sortApplication);
        SortArgsParser mockSortParser = mock(SortArgsParser.class);

        doReturn(mockSortParser)
                .when(sortAppSpy)
                .parseArgs(argsFiles);

        when(mockSortParser.isFirstWordNumber()).thenReturn(false);
        when(mockSortParser.isReverseOrder()).thenReturn(false);
        when(mockSortParser.isCaseIndependent()).thenReturn(true);
        when(mockSortParser.getFiles()).thenReturn(Arrays.asList(expectedFileNames));

        doReturn(inputString)
                .when(sortAppSpy)
                .sortFromFiles(eq(false), eq(false), eq(true), eq(expectedFileNames));

        assertDoesNotThrow(
                () -> sortAppSpy.run(argsFiles, testInputStream, outputStream)
        );

        verify(sortAppSpy).parseArgs(eq(argsFiles));
        verify(mockSortParser).isFirstWordNumber();
        verify(mockSortParser).isReverseOrder();
        verify(mockSortParser).isCaseIndependent();
        verify(mockSortParser).getFiles();
        verify(sortAppSpy).sortFromFiles(eq(false), eq(false), eq(true), eq(expectedFileNames));

        assertEquals(inputString, outputStream.toString());
    }

    @Test
    public void run_sortFromStdinThrowsException_throwsSortException() throws Exception {
        SortApplication sortAppSpy = spy(sortApplication);
        SortArgsParser mockSortParser = mock(SortArgsParser.class);

        doReturn(mockSortParser)
                .when(sortAppSpy)
                .parseArgs(argsStdin);

        when(mockSortParser.isFirstWordNumber()).thenReturn(false);
        when(mockSortParser.isReverseOrder()).thenReturn(false);
        when(mockSortParser.isCaseIndependent()).thenReturn(true);
        when(mockSortParser.isReadingFromStdin()).thenReturn(true);

        doThrow(SortException.class)
                .when(sortAppSpy)
                .sortFromStdin(false, false, true, testInputStream);

        assertThrows(SortException.class,
                () -> sortAppSpy.run(argsStdin, testInputStream, testOutputStream)
        );

        verify(sortAppSpy).parseArgs(eq(argsStdin));
        verify(mockSortParser).isFirstWordNumber();
        verify(mockSortParser).isReverseOrder();
        verify(mockSortParser).isCaseIndependent();
        verify(mockSortParser).isReadingFromStdin();
        verify(sortAppSpy).sortFromStdin(eq(false), eq(false), eq(true), eq(testInputStream));
    }

    @Test
    public void run_stdOutWritesThrowsIOException_throwsSortException() throws Exception {
        SortApplication sortAppSpy = spy(sortApplication);
        SortArgsParser mockSortParser = mock(SortArgsParser.class);

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

        doReturn(mockSortParser)
                .when(sortAppSpy)
                .parseArgs(argsStdin);

        when(mockSortParser.isFirstWordNumber()).thenReturn(false);
        when(mockSortParser.isReverseOrder()).thenReturn(false);
        when(mockSortParser.isCaseIndependent()).thenReturn(true);
        when(mockSortParser.isReadingFromStdin()).thenReturn(true);

        doReturn(inputString)
                .when(sortAppSpy)
                .sortFromStdin(eq(false), eq(false), eq(true), eq(testInputStream));

        SortException exception = assertThrows(SortException.class,
                () -> sortAppSpy.run(argsStdin, testInputStream, exOutputStream)
        );

        assertTrue(exception.getMessage().contains(E_WRITE_STREAM));

        verify(sortAppSpy).parseArgs(eq(argsStdin));
        verify(mockSortParser).isFirstWordNumber();
        verify(mockSortParser).isReverseOrder();
        verify(mockSortParser).isCaseIndependent();
        verify(mockSortParser).isReadingFromStdin();
        verify(sortAppSpy).sortFromStdin(eq(false), eq(false), eq(true), eq(testInputStream));
        assertTrue(exception.getMessage().contains(E_WRITE_STREAM));
    }

    @Test
    public void sortFromFiles_validInputs_returnsSortedLines(@TempDir Path tempDir) throws IOException {
        SortApplication sortAppSpy = spy(sortApplication);
        String fileName = "sortFromFiles_validInputs_returnsSortedLines.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, inputArray);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);
            ioUtils.when(() -> IOUtils.openInputStream(anyString()))
                    .thenReturn(testInputStream);
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArray);
            ioUtils.when(() -> IOUtils.closeInputStream(testInputStream))
                    .thenAnswer(i -> null);

            doNothing()
                    .when(sortAppSpy)
                    .sortInputString(false, false, false, inputArray);

            String actual = assertDoesNotThrow(
                    () -> sortAppSpy.sortFromFiles(false, false, false, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));
            ioUtils.verify(() -> IOUtils.openInputStream(anyString()));
            ioUtils.verify(() -> IOUtils.getLinesFromInputStream(testInputStream));
            ioUtils.verify(() -> IOUtils.closeInputStream(testInputStream));
            verify(sortAppSpy).sortInputString(eq(false), eq(false), eq(false), eq(inputArray));

            assertEquals(inputString, actual);
        }
    }

    @Test
    public void sortFromFiles_nullFileNames_throwsSortException() {
        SortException exception = assertThrows(SortException.class,
                () -> sortApplication.sortFromFiles(false, false, false, null)
        );

        String actual = exception.getMessage();

        assertTrue(actual.contains(E_NULL_ARGS));
    }

    @Test
    public void sortFromFiles_fileDoesNotExist_throwsSortException(@TempDir Path tempDir) {
        SortApplication sortAppSpy = spy(sortApplication);
        String fileName = "sortFromFiles_fileDoesNotExist_throwsSortException.txt";
        Path filePath = tempDir.resolve(fileName);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            SortException exception = assertThrows(SortException.class,
                    () -> sortAppSpy.sortFromFiles(false, false, false, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));

            assertEquals("sort: " + fileName + ": " + E_FILE_NOT_FOUND, exception.getMessage());
        }
    }

    @Test
    public void sortFromFiles_fileIsADirectory_throwsSortException(@TempDir Path tempDir) {
        SortApplication sortAppSpy = spy(sortApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenReturn(tempDir);

            SortException exception = assertThrows(SortException.class,
                    () -> sortAppSpy.sortFromFiles(false, false, false, tempDir.getFileName().toString())
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(anyString()));

            assertEquals("sort: " + tempDir.getFileName().toString() + ": " + E_IS_DIR, exception.getMessage());
        }
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    public void sortFromFiles_fileCannotBeRead_throwsSortException(@TempDir Path tempDir) throws IOException {
        SortApplication sortAppSpy = spy(sortApplication);
        String fileName = "sortFromFiles_fileCannotBeRead_throwsSortException.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.createFile(filePath);
        Files.setPosixFilePermissions(filePath, new HashSet<>(Collections.singletonList(PosixFilePermission.OWNER_WRITE)));

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenReturn(filePath);

            SortException exception = assertThrows(SortException.class,
                    () -> sortAppSpy.sortFromFiles(false, false, false, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(anyString()));

            assertEquals("sort: " + fileName + ": " + E_NO_PERM, exception.getMessage());
        }
    }

    @Test
    public void sortFromStdin_validInputs_returnsLinesSorted() {
        SortApplication sortAppSpy = spy(sortApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArray);

            doNothing()
                    .when(sortAppSpy)
                    .sortInputString(false, false, false, inputArray);

            String actual = assertDoesNotThrow(
                    () -> sortAppSpy.sortFromStdin(false, false, false, testInputStream)
            );

            verify(sortAppSpy).sortInputString(eq(false), eq(false), eq(false), eq(inputArray));

            assertEquals(inputString, actual);
        }
    }

    @Test
    public void sortFromStdin_nullStdin_throwsSortException() {
        SortException exception = assertThrows(SortException.class,
                () -> sortApplication.sortFromStdin(false, false, false, null)
        );

        String actual = exception.getMessage();

        assertTrue(actual.contains(E_NULL_POINTER));
    }

    @Test
    public void sortInputString_noFlags_returnsSortedList() {
        String input = "a\nc\nb";
        List<String> inputArray = Arrays.asList(input.split("\n"));

        assertDoesNotThrow(
                () -> sortApplication.sortInputString(false, false, false, inputArray)
        );

        assertEquals(Arrays.asList("a\nb\nc".split("\n")), inputArray);
    }

    @Test
    public void sortInputString_sortInFirstWordNumber_returnsSortedList() {
        String input = "5 d\n8 b\n1 c\na";
        List<String> inputArray = Arrays.asList(input.split("\n"));

        assertDoesNotThrow(
                () -> sortApplication.sortInputString(true, false, false, inputArray)
        );

        assertEquals(Arrays.asList("1 c\n5 d\n8 b\na".split("\n")), inputArray);
    }

    @Test
    public void sortInputString_sortInReverseOrder_returnsSortedList() {
        String input = "a\nc\nb";
        List<String> inputArray = Arrays.asList(input.split("\n"));

        assertDoesNotThrow(
                () -> sortApplication.sortInputString(false, true, false, inputArray)
        );

        assertEquals(Arrays.asList("c\nb\na".split("\n")), inputArray);
    }

    @Test
    public void sortInputString_sortInCaseIndependent_returnsSortedList() {
        String input = "A\nC\nb";
        List<String> inputArray = Arrays.asList(input.split("\n"));

        assertDoesNotThrow(
                () -> sortApplication.sortInputString(false, false, true, inputArray)
        );

        assertEquals(Arrays.asList("A\nb\nC".split("\n")), inputArray);
    }

    @Test
    public void getChunk_validInput_returnsLinesSuccessfully() {
        String actual = assertDoesNotThrow(
                () -> sortApplication.getChunk(inputString)
        );

        String expected = "This is the first line." + System.lineSeparator() + "This is the ";
        assertEquals(expected, actual);
    }

    @Test
    public void getChunk_validInputWithNumbers_returnsLinesSuccessfully() {
        String actual = assertDoesNotThrow(
                () -> sortApplication.getChunk(inputStringOfNums)
        );

        String expected = "-50";
        assertEquals(expected, actual);
    }
}
