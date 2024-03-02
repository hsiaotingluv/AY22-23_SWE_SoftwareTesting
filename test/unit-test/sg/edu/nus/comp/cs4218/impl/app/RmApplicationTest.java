package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;


public class RmApplicationTest {
    private RmApplication rmApplication;
    private ByteArrayInputStream testInputStream;
    private ByteArrayOutputStream testOutputStream;
    private String[] expectedFileNames;
    private String[] args;

    @BeforeEach
    public void init() {
        rmApplication = new RmApplication();

        expectedFileNames = new String[]{"file.txt"};
        args = new String[]{"-r", "file.txt"};

        testInputStream = new ByteArrayInputStream("".getBytes());
        testOutputStream = new ByteArrayOutputStream();
    }

    @Test
    public void run_emptyStdout_throwsRmException() {
        RmException exception = assertThrows(RmException.class,
                () -> rmApplication.run(null, null, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void run_removeSingleFile_deletesFileSuccessfully() throws Exception {
        RmApplication rmAppSpy = spy(rmApplication);
        RmArgsParser mockRmArgsParser = mock(RmArgsParser.class);

        doReturn(mockRmArgsParser)
                .when(rmAppSpy)
                .parseArgs(args);

        when(mockRmArgsParser.isEmptyFolder()).thenReturn(false);
        when(mockRmArgsParser.isRecursive()).thenReturn(true);
        when(mockRmArgsParser.getFiles()).thenReturn(Arrays.asList(expectedFileNames));

        doNothing()
                .when(rmAppSpy)
                .remove(eq(false), eq(true), eq(expectedFileNames));

        assertDoesNotThrow(
                () -> rmAppSpy.run(args, testInputStream, testOutputStream)
        );

        verify(rmAppSpy).parseArgs(eq(args));
        verify(mockRmArgsParser, times(1)).isEmptyFolder();
        verify(mockRmArgsParser, times(1)).isRecursive();
        verify(mockRmArgsParser, times(1)).getFiles();
        verify(rmAppSpy).remove(eq(false), eq(true), eq(expectedFileNames));
    }

    @Test
    public void run_removeMethodThrowsException_appendsRmExceptionMessage() throws Exception {
        System.setOut(new PrintStream(testOutputStream));

        RmApplication rmAppSpy = spy(rmApplication);
        RmArgsParser mockRmArgsParser = mock(RmArgsParser.class);

        doReturn(mockRmArgsParser)
                .when(rmAppSpy)
                .parseArgs(args);

        when(mockRmArgsParser.isEmptyFolder()).thenReturn(false);
        when(mockRmArgsParser.isRecursive()).thenReturn(true);
        when(mockRmArgsParser.getFiles()).thenReturn(Arrays.asList(expectedFileNames));

        doThrow(RmException.class)
                .when(rmAppSpy)
                .remove(eq(false), eq(true), eq(expectedFileNames));

        assertDoesNotThrow(
                () -> rmAppSpy.run(args, testInputStream, testOutputStream)
        );

        verify(rmAppSpy).parseArgs(eq(args));
        verify(mockRmArgsParser, times(1)).isEmptyFolder();
        verify(mockRmArgsParser, times(1)).isRecursive();
        verify(mockRmArgsParser, times(1)).getFiles();
        verify(rmAppSpy).remove(eq(false), eq(true), eq(expectedFileNames));

        assertTrue(!testOutputStream.toString().isEmpty());
    }

    @Test
    public void remove_validFileNameWithoutFlag_deletesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        String fileName = "remove_validFileName_deletesFileSuccessfully.txt";
        Path filePath = tempDir.resolve(fileName);
        Path input = Files.createFile(filePath);

        RmApplication rmAppSpy = spy(rmApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            assertNotNull(input);
            assertDoesNotThrow(() -> rmAppSpy.remove(false, false, fileName));
            assertFalse(Files.exists(filePath));

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName), times(1));
        }
    }

    @Test
    public void remove_multipleValidFileNameWithoutFlag_deletesFilesSuccessfully(@TempDir Path tempDir) throws Exception {
        String fileName1 = "1.txt";
        String fileName2 = "2.txt";
        Path filePath1 = tempDir.resolve(fileName1);
        Path filePath2 = tempDir.resolve(fileName2);
        Path input1 = Files.createFile(filePath1);
        Path input2 = Files.createFile(filePath2);

        RmApplication rmAppSpy = spy(rmApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName1))
                    .thenReturn(filePath1);
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName2))
                    .thenReturn(filePath2);

            assertNotNull(input1);
            assertNotNull(input2);
            assertDoesNotThrow(() -> rmAppSpy.remove(false, false, fileName1, fileName2));
            assertFalse(Files.exists(filePath1));
            assertFalse(Files.exists(filePath2));

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName1), times(1));
            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName2), times(1));
        }
    }

    @Test
    public void remove_nonEmptyDirectoryWithRecursiveFlag_deletesSuccessfully(@TempDir Path tempDir) throws IOException {
        String fileName = "remove_nonEmptyDirectoryWithCorrectFlag_deletesSuccessfully.txt";
        Path filePath = tempDir.resolve(fileName);
        Path file = Files.createFile(filePath);

        RmApplication rmAppSpy = spy(rmApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()))
                    .thenReturn(tempDir);

            doReturn(true)
                    .when(rmAppSpy)
                    .checkEmptyFolder(tempDir.toFile());

            doReturn(true)
                    .when(rmAppSpy)
                    .deleteDirectory(tempDir.toFile());

            assertNotNull(file);
            assertDoesNotThrow(() -> rmAppSpy.remove(false, true, tempDir.getFileName().toString()));

            ioUtils.verify(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()), times(1));
            verify(rmAppSpy).checkEmptyFolder(eq(tempDir.toFile()));
            verify(rmAppSpy).deleteDirectory(eq(tempDir.toFile()));
        }
    }

    @Test
    public void remove_emptyDirectoryWithEmptyFlag_deletesSuccessfully(@TempDir Path tempDir) {
        RmApplication rmAppSpy = spy(rmApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()))
                    .thenReturn(tempDir);

            doReturn(true)
                    .when(rmAppSpy)
                    .checkEmptyFolder(tempDir.toFile());

            assertDoesNotThrow(() -> rmAppSpy.remove(true, false, tempDir.getFileName().toString()));

            ioUtils.verify(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()), times(1));
            verify(rmAppSpy).checkEmptyFolder(eq(tempDir.toFile()));

            assertFalse(Files.exists(tempDir));
        }
    }

    @Test
    public void remove_invalidFileName_throwsRmException() {
        String fileName = "remove_invalidFileName_throwsRmException.txt";

        RmException exception = assertThrows(RmException.class,
                () -> rmApplication.remove(false, false, fileName)
        );

        assertEquals("rm: " + fileName + ": " + E_FILE_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void remove_resolveFilePathThrowsError_throwsRmException() {
        RmApplication rmAppSpy = spy(rmApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenThrow(ShellException.class);

            assertThrows(RmException.class,
                    () -> rmAppSpy.remove(false, false, expectedFileNames)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(anyString()));
        }
    }

    @Test
    public void remove_nonEmptyDirectoryWithNoFlag_throwsRmException(@TempDir Path tempDir) throws IOException {
        String fileName = "remove_nonEmptyDirectoryWithNoFlag_throwsRmException.txt";
        Path filePath = tempDir.resolve(fileName);
        Path file = Files.createFile(filePath);

        RmApplication rmAppSpy = spy(rmApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()))
                    .thenReturn(tempDir);

            doReturn(false)
                    .when(rmAppSpy)
                    .checkEmptyFolder(tempDir.toFile());

            assertNotNull(file);
            RmException exception = assertThrows(RmException.class,
                    () -> rmAppSpy.remove(false, false, tempDir.getFileName().toString())
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()), times(1));
            verify(rmAppSpy).checkEmptyFolder(eq(tempDir.toFile()));

            assertTrue(exception.getMessage().contains(E_IS_DIR));
            assertTrue(Files.exists(tempDir));
            assertTrue(Files.exists(filePath));
        }
    }

    @Test
    public void remove_nonEmptyDirectoryWithEmptyFlag_throwsRmException(@TempDir Path tempDir) throws IOException {
        String fileName = "remove_nonEmptyDirectoryWithEmptyFlag_throwsRmException.txt";
        Path filePath = tempDir.resolve(fileName);
        Path file = Files.createFile(filePath);

        RmApplication rmAppSpy = spy(rmApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()))
                    .thenReturn(tempDir);

            doReturn(false)
                    .when(rmAppSpy)
                    .checkEmptyFolder(tempDir.toFile());

            assertNotNull(file);
            RmException exception = assertThrows(RmException.class,
                    () -> rmAppSpy.remove(true, false, tempDir.getFileName().toString())
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()), times(1));
            verify(rmAppSpy).checkEmptyFolder(eq(tempDir.toFile()));

            assertEquals("rm: " + tempDir.getFileName().toString() + ": " + E_IS_DIR, exception.getMessage());
            assertTrue(Files.exists(tempDir));
            assertTrue(Files.exists(filePath));
        }
    }

    @Test
    public void remove_emptyDirectoryWithNoFlag_throwsRmException(@TempDir Path tempDir) {
        RmApplication rmAppSpy = spy(rmApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()))
                    .thenReturn(tempDir);

            doReturn(true)
                    .when(rmAppSpy)
                    .checkEmptyFolder(tempDir.toFile());

            RmException exception = assertThrows(RmException.class,
                    () -> rmAppSpy.remove(false, false, tempDir.getFileName().toString())
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()), times(1));
            verify(rmAppSpy).checkEmptyFolder(eq(tempDir.toFile()));

            assertEquals("rm: " + tempDir.getFileName().toString() + ": " + E_IS_DIR, exception.getMessage());
        }
    }

    @Test
    public void remove_noFileName_throwsRmException() {
        RmException exception = assertThrows(RmException.class,
                () -> rmApplication.remove(false, false, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_ARGS));
    }

    @Test
    public void remove_deleteDirectoryMethodFails_throwsRmException(@TempDir Path tempDir) {
        RmApplication rmAppSpy = spy(rmApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()))
                    .thenReturn(tempDir);

            doReturn(false)
                    .when(rmAppSpy)
                    .checkEmptyFolder(tempDir.toFile());

            doReturn(false)
                    .when(rmAppSpy)
                    .deleteDirectory(tempDir.toFile());

            RmException exception = assertThrows(RmException.class,
                    () -> rmAppSpy.remove(false, true, tempDir.getFileName().toString())
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(tempDir.getFileName().toString()), times(1));
            verify(rmAppSpy).checkEmptyFolder(eq(tempDir.toFile()));
            verify(rmAppSpy).deleteDirectory(eq(tempDir.toFile()));

            assertTrue(exception.getMessage().contains(E_NULL_POINTER));
        }
    }

    @Test
    public void checkEmptyFolder_folderIsEmpty_returnsTrue() {
        File mockDirectory = mock(File.class);

        when(mockDirectory.list()).thenReturn(null);
        boolean actual = rmApplication.checkEmptyFolder(mockDirectory);
        assertTrue(actual);

        verify(mockDirectory).list();
    }

    @Test
    public void checkEmptyFolder_folderIsNotEmpty_returnsFalse() {
        File mockDirectory = mock(File.class);
        String[] contents = {"file.txt"};

        when(mockDirectory.list()).thenReturn(contents);
        boolean actual = rmApplication.checkEmptyFolder(mockDirectory);
        assertFalse(actual);

        verify(mockDirectory).list();
    }

    @Test
    public void deleteDirectory_recursiveDelete_returnsTrue(@TempDir Path tempDir) {
        File mockDirectory = mock(File.class);
        File file1 = new File(tempDir.toString(), "file1.txt");
        File file2 = new File(tempDir.toString(), "file2.txt");
        File[] files = {file1, file2};

        when(mockDirectory.listFiles()).thenReturn(files);
        rmApplication.deleteDirectory(mockDirectory);
        assertFalse(mockDirectory.exists());

        verify(mockDirectory).listFiles();
    }
}
