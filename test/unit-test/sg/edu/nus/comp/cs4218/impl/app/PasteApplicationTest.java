package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.app.helper.PasteHelper;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class PasteApplicationTest {
    private PasteApplication pasteApplication;
    private ByteArrayInputStream testInputStreamC;
    private ByteArrayOutputStream testOutputStream;
    private List<String> inputArrayFileA;
    private List<String> inputArrayFileB;
    private List<String> inputArrayStdinC;

    private String[] testFileNames;
    private String[] testFilesWithDash;

    private String[] argsFiles;
    private String[] argsFilesAndStdin;
    private String[] argsStdin;

    private String expectedOutAB;
    private String expectedOutC;
    private String expectedACB;
    private String expectedACBNoFlag;
    private String expectedCAB;

    @BeforeEach
    public void init() {
        pasteApplication = new PasteApplication();

        // Constants
        String fileA = "A" + System.lineSeparator() + "B" + System.lineSeparator() + "C" + System.lineSeparator() + "D";
        String fileB = "1" + System.lineSeparator() + "2" + System.lineSeparator() + "3" + System.lineSeparator() + "4";
        String stdinC = "E" + System.lineSeparator() + "F" + System.lineSeparator() + "G" + System.lineSeparator() + "H";
        inputArrayFileA = Arrays.asList(fileA.split(System.lineSeparator()));
        inputArrayFileB = Arrays.asList(fileB.split(System.lineSeparator()));
        inputArrayStdinC = Arrays.asList(stdinC.split(System.lineSeparator()));

        testFileNames = new String[]{"A.txt", "B.txt"};
        testFilesWithDash = new String[]{"A.txt", "-", "B.txt"};

        argsStdin = new String[]{"-s", "-"};
        argsFiles = new String[]{"-s", "A.txt", "B.txt"};
        argsFilesAndStdin = new String[]{"-s", "-", "A.txt", "B.txt"};

        testInputStreamC = new ByteArrayInputStream(stdinC.getBytes());
        testOutputStream = new ByteArrayOutputStream();

        expectedOutC = "E\tF\tG\tH";
        expectedOutAB = "A\tB\tC\tD" + System.lineSeparator() + "1\t2\t3\t4";
        expectedACB = "A\tB\tC\tD\nE\tF\tG\tH\n1\t2\t3\t4";
        expectedACBNoFlag = "A\t1\tE\nB\t2\tF\nC\t3\tG\nD\t4\tH";
        expectedCAB = "E\tF\tG\tH\nA\tB\tC\tD\n1\t2\t3\t4";
    }

    // Run test cases - under the assumption everything goes through mergeFileAndStdin first
    @Test
    public void run_emptyStdout_throwsException() {
        PasteException exception = assertThrows(PasteException.class,
                () -> pasteApplication.run(null, null, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void run_fileNameAndStdinSpecified_usesFilesAndStdinAndWritesToStdoutSuccessfully() throws Exception {
        System.setOut(new PrintStream(testOutputStream));

        PasteApplication pasteAppSpy = spy(pasteApplication);
        PasteArgsParser mockPasteParser = mock(PasteArgsParser.class);

        doReturn(mockPasteParser)
                .when(pasteAppSpy)
                .parseArgs(argsFilesAndStdin);

        when(mockPasteParser.isSerial()).thenReturn(true);
        when(mockPasteParser.getFiles()).thenReturn(Arrays.asList(testFilesWithDash));

        doReturn(expectedCAB)
                .when(pasteAppSpy)
                .mergeFileAndStdin(eq(true), eq(testInputStreamC), eq(testFilesWithDash));

        assertDoesNotThrow(
                () -> pasteAppSpy.run(argsFilesAndStdin, testInputStreamC, testOutputStream)
        );

        verify(pasteAppSpy).parseArgs(eq(argsFilesAndStdin));
        verify(mockPasteParser).isSerial();
        verify(mockPasteParser).getFiles();
        verify(pasteAppSpy).mergeFileAndStdin(eq(true), eq(testInputStreamC), eq(testFilesWithDash));

        assertEquals(expectedCAB, testOutputStream.toString().trim());
    }

    @Test
    public void run_noFileNameSpecified_usesStdinAndWritesToStdoutSuccessfully() throws Exception {
        System.setOut(new PrintStream(testOutputStream));

        PasteApplication pasteAppSpy = spy(pasteApplication);
        PasteArgsParser mockPasteParser = mock(PasteArgsParser.class);

        doReturn(mockPasteParser)
                .when(pasteAppSpy)
                .parseArgs(argsStdin);

        when(mockPasteParser.isSerial()).thenReturn(true);

        doReturn(expectedOutC)
                .when(pasteAppSpy)
                .mergeFileAndStdin(eq(true), eq(testInputStreamC));

        assertDoesNotThrow(
                () -> pasteAppSpy.run(argsStdin, testInputStreamC, testOutputStream)
        );

        verify(pasteAppSpy).parseArgs(eq(argsStdin));
        verify(mockPasteParser).isSerial();
        verify(mockPasteParser).getFiles();
        verify(pasteAppSpy).mergeFileAndStdin(eq(true), eq(testInputStreamC));

        assertEquals(expectedOutC, testOutputStream.toString().trim());
    }

    @Test
    public void run_fileNameSpecified_usesFilesAndWritesToStdoutSuccessfully() throws Exception {
        System.setOut(new PrintStream(testOutputStream));

        PasteApplication pasteAppSpy = spy(pasteApplication);
        PasteArgsParser mockPasteParser = mock(PasteArgsParser.class);

        doReturn(mockPasteParser)
                .when(pasteAppSpy)
                .parseArgs(argsFiles);

        when(mockPasteParser.isSerial()).thenReturn(true);
        when(mockPasteParser.getFiles()).thenReturn(Arrays.asList(testFileNames));

        doReturn(expectedOutAB)
                .when(pasteAppSpy)
                .mergeFileAndStdin(eq(true), eq(System.in), eq(testFileNames));

        assertDoesNotThrow(
                () -> pasteAppSpy.run(argsFiles, System.in, testOutputStream)
        );

        verify(pasteAppSpy).parseArgs(eq(argsFiles));
        verify(mockPasteParser).isSerial();
        verify(mockPasteParser).getFiles();
        verify(pasteAppSpy).mergeFileAndStdin(eq(true), eq(System.in), eq(testFileNames));

        assertEquals(expectedOutAB, testOutputStream.toString().trim());
    }

    @Test
    public void run_mergeFromFileAndStdinThrowsException_throwsPasteException() throws Exception {
        PasteApplication pasteAppSpy = spy(pasteApplication);
        PasteArgsParser mockPasteParser = mock(PasteArgsParser.class);

        doReturn(mockPasteParser)
                .when(pasteAppSpy)
                .parseArgs(argsStdin);

        when(mockPasteParser.isSerial()).thenReturn(true);

        doThrow(PasteException.class)
                .when(pasteAppSpy)
                .mergeFileAndStdin(eq(true), eq(testInputStreamC));

        assertThrows(PasteException.class,
                () -> pasteAppSpy.run(argsStdin, testInputStreamC, testOutputStream)
        );

        verify(pasteAppSpy).parseArgs(eq(argsStdin));
        verify(mockPasteParser).isSerial();
        verify(pasteAppSpy).mergeFileAndStdin(eq(true), eq(testInputStreamC));
    }

    @Test
    public void run_stdOutWritesThrowsIOException_throwsPasteException() throws Exception {
        PasteApplication pasteAppSpy = spy(pasteApplication);
        PasteArgsParser mockPasteParser = mock(PasteArgsParser.class);

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

        doReturn(mockPasteParser)
                .when(pasteAppSpy)
                .parseArgs(argsStdin);

        when(mockPasteParser.isSerial()).thenReturn(true);

        doReturn(expectedOutC)
                .when(pasteAppSpy)
                .mergeFileAndStdin(eq(true), eq(testInputStreamC));

        PasteException exception = assertThrows(PasteException.class,
                () -> pasteAppSpy.run(argsStdin, testInputStreamC, exOutputStream)
        );

        verify(pasteAppSpy).parseArgs(eq(argsStdin));
        verify(mockPasteParser).isSerial();
        verify(pasteAppSpy).mergeFileAndStdin(eq(true), eq(testInputStreamC));
        assertTrue(exception.getMessage().contains(E_WRITE_STREAM));
    }


    // Merge stdin cases - under the assumption that  app can only take in a single stdin input, and no merging done here

    @Test
    public void mergeStdin_nullStdin_throwsPasteException() {
        PasteApplication pasteAppSpy = spy(pasteApplication);
        Throwable exception = assertThrows(PasteException.class, () -> pasteAppSpy.mergeStdin(true, null));
        assertEquals(new PasteException(E_NULL_POINTER).getMessage(), exception.getMessage());
    }

    @Test
    public void mergeStdin_validStdin_concatenateStdinSuccessfully() {
        PasteApplication pasteAppSpy = spy(pasteApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStreamC))
                    .thenReturn(inputArrayStdinC);

            String output = assertDoesNotThrow(
                    () -> pasteAppSpy.mergeStdin(true, testInputStreamC)
            );

            ioUtils.verify(() -> IOUtils.getLinesFromInputStream(testInputStreamC));

            assertEquals(String.join(STRING_NEWLINE, inputArrayStdinC), output);
        }
    }

    @Test
    public void mergeStdin_emptyStdin_concatenateStdinSuccessfully() {
        PasteApplication pasteAppSpy = spy(pasteApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(System.in))
                    .thenReturn(new ArrayList<String>());

            String output = assertDoesNotThrow(
                    () -> pasteAppSpy.mergeStdin(true, System.in)
            );

            ioUtils.verify(() -> IOUtils.getLinesFromInputStream(System.in));

            assertEquals("", output);
        }
    }

    @Test
    public void mergeStdin_stdinThrowsException_throwsPasteException() throws Exception {
        PasteApplication pasteAppSpy = spy(pasteApplication);
        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.getLinesFromInputStream(System.in))
                    .thenThrow(ShellException.class);

            Throwable exception = assertThrows(PasteException.class,
                    () -> pasteAppSpy.mergeStdin(true, System.in)
            );

            ioUtils.verify(() -> IOUtils.getLinesFromInputStream(System.in));

            assertEquals(new PasteException(E_NULL_POINTER).getMessage(), exception.getMessage());
        }
    }

    // Merge file cases - under the assumption that it only takes in one file, and no merging done here

    @Test
    public void mergeFile_invalidFileName_throwsPasteException(@TempDir Path tempDir) throws Exception {
        String fileNameA = "mergeFileA_validFileNames_concatenatesSuccessfully.txt";
        Path filePathA = tempDir.resolve(fileNameA);

        PasteApplication pasteAppSpy = spy(pasteApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileNameA))
                    .thenReturn(filePathA);

            Throwable exception = assertThrows(PasteException.class,
                    () -> pasteAppSpy.mergeFile(true, fileNameA));

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileNameA));

            assertEquals(new PasteException(fileNameA + ": " + E_FILE_NOT_FOUND).getMessage(),
                    exception.getMessage());
        }
    }

    @Test
    public void mergeFile_fileIsDirectory_throwsPastException(@TempDir Path tempDir) throws Exception {
        String fileNameA = "mergeFileA_validFileNames_concatenatesSuccessfully.txt";
        Path filePathA = tempDir.resolve(fileNameA);
        Files.createDirectory(filePathA);

        PasteApplication pasteAppSpy = spy(pasteApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileNameA))
                    .thenReturn(filePathA);

            Throwable exception = assertThrows(PasteException.class,
                    () -> pasteAppSpy.mergeFile(true, fileNameA));


            ioUtils.verify(() -> IOUtils.resolveFilePath(fileNameA));

            assertEquals(new PasteException(fileNameA + ": " + E_IS_DIR).getMessage(),
                    exception.getMessage());
        }
    }

    @Test
    public void mergeFile_nullFileNames_throwsPasteException() {
        PasteException exception = assertThrows(PasteException.class,
                () -> pasteApplication.mergeFile(true, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    public void mergeFile_fileCannotBeRead_throwsPasteException(@TempDir Path tempDir) throws IOException {
        PasteApplication pasteAppSpy = spy(pasteApplication);
        String fileName = "mergeFile_fileCannotBeRead_throwsPasteException.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.createFile(filePath);
        Files.setPosixFilePermissions(filePath, new HashSet<>(Collections.singletonList(PosixFilePermission.OWNER_WRITE)));

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            PasteException exception = assertThrows(PasteException.class, () ->
                    pasteAppSpy.mergeFile(true, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));

            assertTrue(exception.getMessage().contains(E_NO_PERM));
        }
    }

    @Test
    public void mergeFile_validFileNames_concatenatesSuccessfully(@TempDir Path tempDir) throws Exception {
        String fileNameA = "mergeFileA_validFileNames_concatenatesSuccessfully.txt";
        Path filePathA = tempDir.resolve(fileNameA);
        Files.createFile(filePathA);
        Files.write(filePathA, inputArrayFileA);

        PasteApplication pasteAppSpy = spy(pasteApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileNameA))
                    .thenReturn(filePathA);


            String actual = assertDoesNotThrow(
                    () -> pasteAppSpy.mergeFile(true, fileNameA)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileNameA));

            assertEquals(String.join(STRING_NEWLINE, inputArrayFileA), actual);
        }
    }

    //Merge file and stdin cases - this is where stdin and files are sent for merging

    @Test
    public void mergeFileAndStdin_fileNameSpecifiedWithStdinFlag_returnsFilesAndStdinConcatenated() throws Exception {
        PasteApplication pasteAppSpy = spy(pasteApplication);

        doReturn(String.join(STRING_NEWLINE, inputArrayStdinC))
                .when(pasteAppSpy)
                .mergeStdin(eq(true), eq(testInputStreamC));

        doReturn(String.join(STRING_NEWLINE, inputArrayFileA))
                .when(pasteAppSpy)
                .mergeFile(eq(true), eq(testFileNames[0]));

        doReturn(String.join(STRING_NEWLINE, inputArrayFileB))
                .when(pasteAppSpy)
                .mergeFile(eq(true), eq(testFileNames[1]));

        List<List<String>> inp = Arrays.asList(inputArrayFileA, inputArrayStdinC, inputArrayFileB);


        try (MockedStatic<PasteHelper> mPasteHelper = mockStatic(PasteHelper.class)) {
            mPasteHelper.when(() -> PasteHelper.concatenateLines(eq(true), eq(inp))).thenReturn(expectedACB);
            String actual = assertDoesNotThrow(
                    () -> pasteAppSpy.mergeFileAndStdin(true, testInputStreamC, testFilesWithDash)
            );

            verify(pasteAppSpy).mergeStdin(eq(true), eq(testInputStreamC));
            verify(pasteAppSpy).mergeFile(eq(true), eq(testFileNames[0]));
            verify(pasteAppSpy).mergeFile(eq(true), eq(testFileNames[1]));


            mPasteHelper.verify(() -> PasteHelper.concatenateLines(eq(true), eq(inp)));

            assertEquals(expectedACB + STRING_NEWLINE, actual);
        }


    }

    @Test
    public void mergeFileAndStdin_fileNameSpecifiedWithStdinNoFlag_returnsFilesAndStdinConcatenated() throws Exception {
        PasteApplication pasteAppSpy = spy(pasteApplication);

        doReturn(String.join(STRING_NEWLINE, inputArrayStdinC))
                .when(pasteAppSpy)
                .mergeStdin(eq(false), eq(testInputStreamC));

        doReturn(String.join(STRING_NEWLINE, inputArrayFileA))
                .when(pasteAppSpy)
                .mergeFile(eq(false), eq(testFileNames[0]));

        doReturn(String.join(STRING_NEWLINE, inputArrayFileB))
                .when(pasteAppSpy)
                .mergeFile(eq(false), eq(testFileNames[1]));

        List<List<String>> inp = Arrays.asList(inputArrayFileA, inputArrayStdinC, inputArrayFileB);

        try (MockedStatic<PasteHelper> mPasteHelper = mockStatic(PasteHelper.class)) {
            mPasteHelper.when(() -> PasteHelper.concatenateLines(eq(false), eq(inp))).thenReturn(expectedACBNoFlag);


            String actual = assertDoesNotThrow(
                    () -> pasteAppSpy.mergeFileAndStdin(false, testInputStreamC, testFilesWithDash)
            );

            verify(pasteAppSpy).mergeStdin(eq(false), eq(testInputStreamC));
            verify(pasteAppSpy).mergeFile(eq(false), eq(testFileNames[0]));
            verify(pasteAppSpy).mergeFile(eq(false), eq(testFileNames[1]));


            mPasteHelper.verify(() -> PasteHelper.concatenateLines(eq(false), eq(inp)));

            assertEquals(expectedACBNoFlag + STRING_NEWLINE, actual);
        }
    }

    //Concatenate line cases

    @Test
    public void concatenateLines_withIsSerial_returnsCorrectOutput() {
        String actual = assertDoesNotThrow(
                () -> PasteHelper.concatenateLines(true, Arrays.asList(inputArrayFileA,
                        inputArrayFileB))
        );

        String expected = expectedOutAB;
        assertEquals(expected, actual);
    }

    @Test
    public void concatenateLines_withoutSerial_returnsCorrectOutput() {
        String expectedOutABNoS = "A\t1" + System.lineSeparator()
                + "B\t2" + System.lineSeparator()
                + "C\t3" + System.lineSeparator()
                + "D\t4";

        String actual = assertDoesNotThrow(
                () -> PasteHelper.concatenateLines(false, Arrays.asList(inputArrayFileA,
                        inputArrayFileB))
        );

        assertEquals(expectedOutABNoS, actual);
    }

}

