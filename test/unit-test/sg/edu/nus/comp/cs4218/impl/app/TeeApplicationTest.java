package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;


public class TeeApplicationTest {
    private TeeApplication teeApplication;
    private ByteArrayInputStream testInputStream;
    private ByteArrayOutputStream testOutputStream;
    private String inputString;
    private String inputString2;
    private List<String> inputArray;
    private List<String> inputArray2;
    private String[] expectedFileNames;
    private String[] args;

    @BeforeEach
    public void init() {
        teeApplication = new TeeApplication();

        // Constants
        inputString = "This is the first line" + System.lineSeparator() + "This is the second line.";
        inputArray = Arrays.asList(inputString.split(System.lineSeparator()));
        inputString2 = "This is another first line." + System.lineSeparator() + "This is another second line.";
        inputArray2 = Arrays.asList(inputString2.split(System.lineSeparator()));

        expectedFileNames = new String[]{"file.txt"};
        args = new String[]{"-a", "file.txt"};

        testInputStream = new ByteArrayInputStream(inputString.getBytes());
        testOutputStream = new ByteArrayOutputStream();
    }

    @Test
    public void run_emptyStdout_throwsTeeException() {
        TeeException exception = assertThrows(TeeException.class,
                () -> teeApplication.run(null, null, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void run_teeFromStdinThrowsException_throwsTeeException() throws Exception {
        TeeApplication teeAppSpy = spy(teeApplication);
        TeeArgsParser mockTeeParser = mock(TeeArgsParser.class);

        doReturn(mockTeeParser)
                .when(teeAppSpy)
                .parseArgs(args);

        when(mockTeeParser.isAppendToFile()).thenReturn(true);
        when(mockTeeParser.getFiles()).thenReturn(Arrays.asList(expectedFileNames));

        doThrow(Exception.class)
                .when(teeAppSpy)
                .teeFromStdin(eq(true), eq(testInputStream), eq(expectedFileNames));

        assertThrows(Exception.class,
                () -> teeAppSpy.run(args, testInputStream, testOutputStream)
        );

        verify(teeAppSpy).parseArgs(eq(args));
        verify(mockTeeParser).isAppendToFile();
        verify(teeAppSpy).teeFromStdin(eq(true), eq(testInputStream), eq(expectedFileNames));
    }

    @Test
    public void run_outputNotEmpty_writesToStdoutSuccessfully() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        TeeApplication teeAppSpy = spy(teeApplication);
        TeeArgsParser mockTeeParser = mock(TeeArgsParser.class);

        doReturn(mockTeeParser)
                .when(teeAppSpy)
                .parseArgs(args);

        when(mockTeeParser.isAppendToFile()).thenReturn(true);
        when(mockTeeParser.getFiles()).thenReturn(Arrays.asList(expectedFileNames));

        doReturn(inputString)
                .when(teeAppSpy)
                .teeFromStdin(eq(true), eq(testInputStream), eq(expectedFileNames));

        assertDoesNotThrow(
                () -> teeAppSpy.run(args, testInputStream, outputStream)
        );

        verify(teeAppSpy).parseArgs(eq(args));
        verify(mockTeeParser).isAppendToFile();
        verify(mockTeeParser).getFiles();
        verify(teeAppSpy).teeFromStdin(eq(true), eq(testInputStream), eq(expectedFileNames));

        assertEquals(inputString, outputStream.toString().trim());
    }

    @Test
    public void run_stdOutWritesThrowsException_throwsTeeException() throws Exception {
        TeeApplication teeAppSpy = spy(teeApplication);
        TeeArgsParser mockTeeParser = mock(TeeArgsParser.class);

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

        doReturn(mockTeeParser)
                .when(teeAppSpy)
                .parseArgs(args);

        when(mockTeeParser.isAppendToFile()).thenReturn(true);
        when(mockTeeParser.getFiles()).thenReturn(Arrays.asList(expectedFileNames));

        doReturn(inputString)
                .when(teeAppSpy)
                .teeFromStdin(eq(true), eq(testInputStream), eq(expectedFileNames));

        TeeException exception = assertThrows(TeeException.class,
                () -> teeAppSpy.run(args, testInputStream, exOutputStream)
        );

        verify(teeAppSpy).parseArgs(eq(args));
        verify(mockTeeParser).isAppendToFile();
        verify(mockTeeParser).getFiles();
        verify(teeAppSpy).teeFromStdin(eq(true), eq(testInputStream), eq(expectedFileNames));

        assertTrue(exception.getMessage().contains(E_WRITE_STREAM));
    }

    @Test
    public void teeFromStdin_validArgsNoAppendSingleFile_returnsCorrectString(@TempDir Path tempDir) throws TeeException, IOException {
        String fileName = "teeFromStdin_validArgsNoAppendSingleFile_returnsCorrectString.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, inputArray);

        TeeApplication teeAppSpy = spy(teeApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArray);

            doReturn(filePath.toFile())
                    .when(teeAppSpy)
                    .resolveFile(fileName);

            doAnswer(i -> null)
                    .when(teeAppSpy)
                    .writeToFile(eq(false), eq(inputArray), eq(filePath.toFile()));

            String actual = assertDoesNotThrow(
                    () -> teeAppSpy.teeFromStdin(false, testInputStream, fileName)
            );

            ioUtils.verify(() -> IOUtils.getLinesFromInputStream(testInputStream));
            verify(teeAppSpy).resolveFile(eq(fileName));
            verify(teeAppSpy).writeToFile(eq(false), eq(inputArray), eq(filePath.toFile()));
        }
    }

    @Test
    public void teeFromStdin_validArgsAppendSingleFile_returnsCorrectString(@TempDir Path tempDir) throws TeeException, IOException {
        String fileName = "teeFromStdin_validArgsAppendSingleFile_returnsCorrectString.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, inputArray);

        TeeApplication teeAppSpy = spy(teeApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArray);

            doReturn(filePath.toFile())
                    .when(teeAppSpy)
                    .resolveFile(fileName);

            doAnswer(i -> null)
                    .when(teeAppSpy)
                    .writeToFile(eq(true), eq(inputArray), eq(filePath.toFile()));

            String actual = assertDoesNotThrow(
                    () -> teeAppSpy.teeFromStdin(true, testInputStream, fileName)
            );

            ioUtils.verify(() -> IOUtils.getLinesFromInputStream(testInputStream));
            verify(teeAppSpy).resolveFile(eq(fileName));
            verify(teeAppSpy).writeToFile(eq(true), eq(inputArray), eq(filePath.toFile()));
        }
    }

    @Test
    public void teeFromStdin_nullStdin_throwsTeeException() {
        TeeException exception = assertThrows(TeeException.class,
                () -> teeApplication.teeFromStdin(false, null, expectedFileNames)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void teeFromStdin_getLinesFromInputStreamThrowsException_throwsTeeException() {
        TeeApplication teeAppSpy = spy(teeApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenThrow(ShellException.class);

            assertThrows(TeeException.class,
                    () -> teeAppSpy.teeFromStdin(false, testInputStream, expectedFileNames)
            );

            ioUtils.verify(() -> IOUtils.getLinesFromInputStream(testInputStream));
        }
    }

    @Test
    public void resolveFile_validFileName_returnsFileNode(@TempDir Path tempDir) {
        TeeApplication teeAppSpy = spy(teeApplication);
        String fileName = "resolveFile_validFileName_returnsFileNode.txt";
        Path filePath = tempDir.resolve(fileName);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            assertDoesNotThrow(
                    () -> teeAppSpy.resolveFile(fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));
        }
    }

    @Test
    public void resolveFile_resolveFilePathThrowsError_throwsTeeException() {
        TeeApplication teeAppSpy = spy(teeApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenThrow(ShellException.class);

            assertThrows(TeeException.class, () ->
                    teeAppSpy.resolveFile("file.txt")
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(anyString()));
        }
    }

    @Test
    public void resolveFile_fileIsADirectory_throwsTeeException(@TempDir Path tempDir) {
        TeeApplication teeAppSpy = spy(teeApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(tempDir.toString()))
                    .thenReturn(tempDir);

            TeeException exception = assertThrows(TeeException.class, () ->
                    teeAppSpy.resolveFile(tempDir.toString())
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(tempDir.toString()));

            assertEquals("tee: " + tempDir.getFileName().toString() + ": " + E_IS_DIR, exception.getMessage());
        }
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    public void resolveFile_fileCannotBeWritten_throwsTeeException(@TempDir Path tempDir) throws IOException {
        TeeApplication teeAppSpy = spy(teeApplication);
        String fileName = "resolveFile_fileCannotBeWritten_throwsTeeException.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.createFile(filePath);
        Files.setPosixFilePermissions(filePath, new HashSet<>(Collections.singletonList(PosixFilePermission.OWNER_READ)));

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            TeeException exception = assertThrows(TeeException.class, () ->
                    teeAppSpy.resolveFile(fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));

            assertEquals("tee: " + fileName + ": " + E_NO_PERM, exception.getMessage());
        }
    }

    @Test
    public void writeToFile_noAppendSingleFileValidInputs_writesSuccessfully(@TempDir Path tempDir) throws IOException {
        TeeApplication teeAppSpy = spy(teeApplication);
        String fileName = "writeToFile_singleFileValidInputs_writesSuccessfully.txt";
        Path filePath = tempDir.resolve(fileName);

        assertDoesNotThrow(
                () -> teeAppSpy.writeToFile(false, inputArray, filePath.toFile())
        );

        List<String> actual = Files.readAllLines(filePath);
        assertTrue(Files.exists(filePath));
        assertEquals(inputArray, actual);
    }

    @Test
    public void writeToFile_appendSingleFileValidInputs_writesSuccessfully(@TempDir Path tempDir) throws IOException {
        TeeApplication teeAppSpy = spy(teeApplication);
        String fileName = "writeToFile_appendSingleFileValidInputs_writesSuccessfully.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, inputArray);

        assertDoesNotThrow(
                () -> teeAppSpy.writeToFile(true, inputArray2, filePath.toFile())
        );

        String actual = String.join(System.lineSeparator(), Files.readAllLines(filePath));
        assertTrue(Files.exists(filePath));
        assertEquals(inputString + System.lineSeparator() + inputString2, actual);
    }
}
