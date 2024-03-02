package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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


public class CutApplicationTest {
    private final List<int[]> testRanges = new ArrayList<>();
    private CutApplication cutApplication;
    private ByteArrayInputStream testInputStream;
    private ByteArrayOutputStream testOutputStream;
    private List<String> inputArray;
    private String[] testFileNames;
    private String[] argsFiles;
    private String[] argsStdin;
    private String expectedOutCharPo;
    private String expectedOutBytePo;

    @BeforeEach
    public void init() {
        cutApplication = new CutApplication();

        // Constants
        String inputString = "That biscuit cost me 2£!!!"
                + System.lineSeparator()
                + "Hello this is";
        inputArray = Arrays.asList(inputString.split(System.lineSeparator()));

        expectedOutCharPo = "T£That biscuit cost me 2£"
                + System.lineSeparator()
                + "HHello this is"
                + System.lineSeparator();
        expectedOutBytePo = "T�That biscuit cost me 2�"
                + System.lineSeparator()
                + "HHello this is"
                + System.lineSeparator();

        testFileNames = new String[]{"file.txt"};
        argsFiles = new String[]{"-c", "1-23", testFileNames[0]};
        argsStdin = new String[]{"-b", "1-23", "-"};

        // only used to test cutLines method
        testRanges.add(0, new int[]{1, 1}); // only char/byte at 1
        testRanges.add(1, new int[]{23, 23}); // only char/byte at 23
        testRanges.add(2, new int[]{1, 23}); // range from 1-23
        testRanges.add(3, new int[]{50, 50}); // out of range

        testInputStream = new ByteArrayInputStream(inputString.getBytes());
        testOutputStream = new ByteArrayOutputStream();
    }

    @Test
    public void run_emptyStdout_throwsCutException() {
        CutException exception = assertThrows(CutException.class,
                () -> cutApplication.run(null, null, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void run_fileSpecified_usesFilesAndWritesToStdoutSuccessfully() throws Exception {
        System.setOut(new PrintStream(testOutputStream));

        CutApplication cutAppSpy = spy(cutApplication);
        CutArgsParser mockCutParser = mock(CutArgsParser.class);

        doReturn(mockCutParser)
                .when(cutAppSpy)
                .parseArgs(argsFiles);

        when(mockCutParser.isReadingFromStdin()).thenReturn(false);
        when(mockCutParser.isCharPo()).thenReturn(true);
        when(mockCutParser.isBytePo()).thenReturn(false);
        when(mockCutParser.getRanges()).thenReturn(testRanges);
        when(mockCutParser.getFiles()).thenReturn(testFileNames);

        doReturn(expectedOutCharPo)
                .when(cutAppSpy)
                .cutFromFiles(eq(true), eq(false), eq(testRanges), eq(testFileNames));

        assertDoesNotThrow(
                () -> cutAppSpy.run(argsFiles, testInputStream, testOutputStream)
        );

        verify(cutAppSpy).parseArgs(eq(argsFiles));
        verify(mockCutParser).isReadingFromStdin();
        verify(mockCutParser).isCharPo();
        verify(mockCutParser).isBytePo();
        verify(mockCutParser).getRanges();
        verify(mockCutParser).getFiles();
        verify(cutAppSpy).cutFromFiles(eq(true), eq(false), eq(testRanges), eq(testFileNames));

        assertEquals(expectedOutCharPo, testOutputStream.toString());
    }

    @Test
    public void run_noFileNameSpecified_usesStdinAndWritesToStdoutSuccessfully() throws Exception {
        System.setOut(new PrintStream(testOutputStream));

        CutApplication cutAppSpy = spy(cutApplication);
        CutArgsParser mockCutParser = mock(CutArgsParser.class);

        doReturn(mockCutParser)
                .when(cutAppSpy)
                .parseArgs(argsStdin);

        when(mockCutParser.isReadingFromStdin()).thenReturn(true);
        when(mockCutParser.isCharPo()).thenReturn(false);
        when(mockCutParser.isBytePo()).thenReturn(true);
        when(mockCutParser.getRanges()).thenReturn(testRanges);

        doReturn(expectedOutBytePo)
                .when(cutAppSpy)
                .cutFromStdin(eq(false), eq(true), eq(testRanges), eq(testInputStream));

        assertDoesNotThrow(
                () -> cutAppSpy.run(argsStdin, testInputStream, testOutputStream)
        );

        verify(cutAppSpy).parseArgs(eq(argsStdin));
        verify(mockCutParser).isReadingFromStdin();
        verify(mockCutParser).isCharPo();
        verify(mockCutParser).isBytePo();
        verify(mockCutParser).getRanges();
        verify(cutAppSpy).cutFromStdin(eq(false), eq(true), eq(testRanges), eq(testInputStream));

        assertEquals(expectedOutBytePo, testOutputStream.toString());
    }

    @Test
    public void run_cutFromStdinError_throwsCutException() throws Exception {
        CutApplication cutAppSpy = spy(cutApplication);
        CutArgsParser mockCutParser = mock(CutArgsParser.class);

        doReturn(mockCutParser)
                .when(cutAppSpy)
                .parseArgs(argsStdin);

        when(mockCutParser.isReadingFromStdin()).thenReturn(true);
        when(mockCutParser.isCharPo()).thenReturn(false);
        when(mockCutParser.isBytePo()).thenReturn(true);
        when(mockCutParser.getRanges()).thenReturn(testRanges);

        doThrow(CutException.class)
                .when(cutAppSpy)
                .cutFromStdin(eq(false), eq(true), eq(testRanges), eq(testInputStream));

        assertThrows(CutException.class,
                () -> cutAppSpy.run(argsStdin, testInputStream, testOutputStream)
        );

        verify(cutAppSpy).parseArgs(eq(argsStdin));
        verify(mockCutParser).isReadingFromStdin();
        verify(mockCutParser).isCharPo();
        verify(mockCutParser).isBytePo();
        verify(mockCutParser).getRanges();
        verify(cutAppSpy).cutFromStdin(eq(false), eq(true), eq(testRanges), eq(testInputStream));
    }

    @Test
    public void run_cutFromFilesError_throwsCutException() throws Exception {
        CutApplication cutAppSpy = spy(cutApplication);
        CutArgsParser mockCutParser = mock(CutArgsParser.class);

        doReturn(mockCutParser)
                .when(cutAppSpy)
                .parseArgs(argsFiles);

        when(mockCutParser.isReadingFromStdin()).thenReturn(false);
        when(mockCutParser.isCharPo()).thenReturn(true);
        when(mockCutParser.isBytePo()).thenReturn(false);
        when(mockCutParser.getRanges()).thenReturn(testRanges);
        when(mockCutParser.getFiles()).thenReturn(testFileNames);

        doThrow(CutException.class)
                .when(cutAppSpy)
                .cutFromFiles(eq(true), eq(false), eq(testRanges), eq(testFileNames));

        assertThrows(CutException.class,
                () -> cutAppSpy.run(argsFiles, testInputStream, testOutputStream)
        );

        verify(cutAppSpy).parseArgs(eq(argsFiles));
        verify(mockCutParser).isReadingFromStdin();
        verify(mockCutParser).isCharPo();
        verify(mockCutParser).isBytePo();
        verify(mockCutParser).getRanges();
        verify(mockCutParser).getFiles();
        verify(cutAppSpy).cutFromFiles(eq(true), eq(false), eq(testRanges), eq(testFileNames));
    }

    @Test
    public void run_stdOutWritesThrowsIOException_throwsCutException() throws Exception {
        CutApplication cutAppSpy = spy(cutApplication);
        CutArgsParser mockCutParser = mock(CutArgsParser.class);

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

        doReturn(mockCutParser)
                .when(cutAppSpy)
                .parseArgs(argsStdin);

        when(mockCutParser.isReadingFromStdin()).thenReturn(true);
        when(mockCutParser.isCharPo()).thenReturn(false);
        when(mockCutParser.isBytePo()).thenReturn(true);
        when(mockCutParser.getRanges()).thenReturn(testRanges);

        doReturn(expectedOutBytePo)
                .when(cutAppSpy)
                .cutFromStdin(eq(false), eq(true), eq(testRanges), eq(testInputStream));

        Exception exception = assertThrows(CutException.class,
                () -> cutAppSpy.run(argsStdin, testInputStream, exOutputStream)
        );

        verify(cutAppSpy).parseArgs(eq(argsStdin));
        verify(mockCutParser).isReadingFromStdin();
        verify(mockCutParser).isCharPo();
        verify(mockCutParser).isBytePo();
        verify(mockCutParser).getRanges();
        verify(cutAppSpy).cutFromStdin(eq(false), eq(true), eq(testRanges), eq(testInputStream));
        assertTrue(exception.getMessage().contains(E_WRITE_STREAM));
    }

    @Test
    public void cutFromFiles_validFileName_returnCutByChar(@TempDir Path tempDir) throws Exception {
        CutApplication cutAppSpy = spy(cutApplication);

        String fileName = "cutFromFiles_validFileName_cutsSuccessfully.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, inputArray);
        FileInputStream exFileInputStream = new FileInputStream(filePath.toString());

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            ioUtils.when(() -> IOUtils.openInputStream(fileName))
                    .thenReturn(exFileInputStream);

            doReturn(Arrays.asList(expectedOutCharPo.split(System.lineSeparator())))
                    .when(cutAppSpy)
                    .cutLines(eq(true), eq(false), eq(testRanges), eq(exFileInputStream));

            String actual = assertDoesNotThrow(
                    () -> cutAppSpy.cutFromFiles(true, false, testRanges, fileName)
            );

            verify(cutAppSpy).cutLines(eq(true), eq(false), eq(testRanges), eq(exFileInputStream));

            assertTrue(Files.exists(filePath));
            assertEquals(expectedOutCharPo, actual);
        }

        exFileInputStream.close();
    }

    @Test
    public void cutFromFiles_validFileName_returnCutByByte(@TempDir Path tempDir) throws Exception {
        CutApplication cutAppSpy = spy(cutApplication);

        String fileName = "cutFromFiles_validFileName_cutsSuccessfully.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, inputArray);
        FileInputStream exFileInputStream = new FileInputStream(filePath.toString());

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            ioUtils.when(() -> IOUtils.openInputStream(fileName))
                    .thenReturn(exFileInputStream);

            doReturn(Arrays.asList(expectedOutBytePo.split(System.lineSeparator())))
                    .when(cutAppSpy)
                    .cutLines(eq(false), eq(true), eq(testRanges), eq(exFileInputStream));

            String actual = assertDoesNotThrow(
                    () -> cutAppSpy.cutFromFiles(false, true, testRanges, fileName)
            );

            verify(cutAppSpy).cutLines(eq(false), eq(true), eq(testRanges), eq(exFileInputStream));

            assertTrue(Files.exists(filePath));
            assertEquals(expectedOutBytePo, actual);
        }

        exFileInputStream.close();
    }

    @Test
    public void cutFromFiles_nullFileName_throwsCutException() {
        CutException exception = assertThrows(CutException.class,
                () -> cutApplication.cutFromFiles(true, false, testRanges, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_ARGS));
    }

    @Test
    public void cutFromFiles_invalidFileName_throwsCutException(@TempDir Path tempDir) {
        CutApplication cutAppSpy = spy(cutApplication);
        String fileName = "cutFromFiles_invalidFileName_throwsCutException.txt";
        Path filePath = tempDir.resolve(fileName);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            String actual = assertDoesNotThrow(() ->
                    cutAppSpy.cutFromFiles(true, false, testRanges, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));

            assertTrue(actual.contains("cut: " + fileName + ": " + E_FILE_NOT_FOUND));
        }
    }

    @Test
    public void cutFromFiles_fileIsADirectory_throwsCutException(@TempDir Path tempDir) {
        CutApplication cutAppSpy = spy(cutApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(tempDir.toString()))
                    .thenReturn(tempDir);

            String actual = assertDoesNotThrow(() ->
                    cutAppSpy.cutFromFiles(true, false, testRanges, tempDir.toString())
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(tempDir.toString()));

            assertTrue(actual.contains("cut: " + tempDir.getFileName().toString() + ": " + E_IS_DIR));
        }
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    public void cutFromFiles_fileCannotBeRead_throwsCutException(@TempDir Path tempDir) throws IOException {
        CutApplication cutAppSpy = spy(cutApplication);
        String fileName = "cutFromFiles_fileCannotBeRead_throwsCutException.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.createFile(filePath);
        Files.setPosixFilePermissions(filePath, new HashSet<>(Collections.singletonList(PosixFilePermission.OWNER_WRITE)));

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            String actual = assertDoesNotThrow(() ->
                    cutAppSpy.cutFromFiles(true, false, testRanges, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));

            assertTrue(actual.contains("cut: " + fileName + ": " + E_NO_PERM));
        }
    }

    @Test
    public void cutFromFiles_errorWithTryOpenInputStream_throwsCutException(@TempDir Path tempDir) throws IOException {
        CutApplication cutAppSpy = spy(cutApplication);
        String fileName = "cutFromFiles_errorWithTryOpenInputStream_throwsCutException.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.createFile(filePath);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            ioUtils.when(() -> IOUtils.openInputStream(fileName))
                    .thenThrow(ShellException.class);

            String actual = assertDoesNotThrow(() ->
                    cutAppSpy.cutFromFiles(true, false, testRanges, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));
            ioUtils.verify(() -> IOUtils.openInputStream(fileName));

            assertTrue(actual.contains("cut"));
        }
    }

    @Test
    public void cutFromStdin_validStdin_returnCutByByte() throws Exception {
        CutApplication cutAppSpy = spy(cutApplication);

        doReturn(Arrays.asList(expectedOutBytePo.split(System.lineSeparator())))
                .when(cutAppSpy)
                .cutLines(eq(false), eq(true), eq(testRanges), eq(testInputStream));

        String output = assertDoesNotThrow(
                () -> cutAppSpy.cutFromStdin(false, true, testRanges, testInputStream)
        );

        verify(cutAppSpy).cutLines(eq(false), eq(true), eq(testRanges), eq(testInputStream));

        assertEquals(expectedOutBytePo, output);
    }

    @Test
    public void cutFromStdin_validStdin_returnCutByChar() throws Exception {
        CutApplication cutAppSpy = spy(cutApplication);

        doReturn(Arrays.asList(expectedOutCharPo.split(System.lineSeparator())))
                .when(cutAppSpy)
                .cutLines(eq(true), eq(false), eq(testRanges), eq(testInputStream));

        String output = assertDoesNotThrow(
                () -> cutAppSpy.cutFromStdin(true, false, testRanges, testInputStream)
        );

        verify(cutAppSpy).cutLines(eq(true), eq(false), eq(testRanges), eq(testInputStream));

        assertEquals(expectedOutCharPo, output);
    }

    @Test
    public void cutFromStdin_cutLines_throwsCutException() throws Exception {
        CutApplication cutAppSpy = spy(cutApplication);

        doThrow(ShellException.class)
                .when(cutAppSpy)
                .cutLines(eq(false), eq(true), eq(testRanges), eq(testInputStream));

        assertThrows(CutException.class,
                () -> cutAppSpy.cutFromStdin(false, true, testRanges, testInputStream)
        );

        verify(cutAppSpy).cutLines(eq(false), eq(true), eq(testRanges), eq(testInputStream));
    }

    @Test
    public void cutFromStdin_nullStdin_throwsCutException() {
        CutException exception = assertThrows(CutException.class,
                () -> cutApplication.cutFromStdin(false, true, testRanges, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void cutLines_validInputStreamWithCharPo_returnsCorrectOutput() {
        CutApplication cutAppSpy = spy(cutApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArray);

            List<String> actual = assertDoesNotThrow(
                    () -> cutAppSpy.cutLines(true, false, testRanges, testInputStream)
            );

            List<String> expectedList = Arrays.asList(expectedOutCharPo.split(System.lineSeparator()));
            assertEquals(expectedList, actual);
        }
    }

    @Test
    public void cutLines_validInputStreamWithBytePo_returnsCorrectOutput() {
        CutApplication cutAppSpy = spy(cutApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArray);

            List<String> actual = assertDoesNotThrow(
                    () -> cutAppSpy.cutLines(false, true, testRanges, testInputStream)
            );

            List<String> expectedList = Arrays.asList(expectedOutBytePo.split(System.lineSeparator()));
            assertEquals(expectedList, actual);
        }
    }

    @Test
    public void cutLines_invalidInputStream_throwsShellException() {
        assertThrowsExactly(ShellException.class, () -> {
            InputStream inputStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }
            };
            cutApplication.cutLines(true, false, testRanges, inputStream);
        });
    }
}
