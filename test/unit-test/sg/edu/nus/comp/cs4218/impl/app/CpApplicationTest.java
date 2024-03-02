package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.impl.parser.CpArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_MISSING_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


public class CpApplicationTest {

    private CpApplication cpApplication;
    private OutputStream outputStream;
    private String[] inputs;

    private String fileName1;
    private String fileName2;
    private String folderName1;
    private String folderName2;
    private List<String> fileText1;

    @BeforeEach
    void setUp() {
        this.cpApplication = new CpApplication();
        this.outputStream = new ByteArrayOutputStream();
        this.inputs = new String[]{"foo.txt", "foo1.txt"};

        this.fileName1 = "foo.txt";
        this.fileName2 = "foo1.txt";
        this.folderName1 = "folder1";
        this.folderName2 = "folder2";
        this.fileText1 = Arrays.asList("hi");

    }

    @Test
    void run_withNullArgs_throwNullArgumentCpException() {
        Throwable exception = assertThrows(CpException.class, () -> {
            this.cpApplication.run(null, System.in, outputStream);
        });

        assertEquals(new CpException(E_NULL_ARGS).getLocalizedMessage(), exception.getMessage());
    }

    @Test
    void run_withNullStreams_throwNullStreamsCpException() {
        Throwable exception = assertThrows(CpException.class, () -> {
            this.cpApplication.run(inputs, null, null);
        });
        assertEquals(new CpException(E_NULL_POINTER).getMessage(), exception.getMessage());
    }

    @Test
    void run_withNullInputStream_throwNoInputStreamCpException() {
        Throwable exception = assertThrows(CpException.class, () -> {
            this.cpApplication.run(inputs, null, outputStream);
        });
        assertEquals(new CpException(E_NO_ISTREAM).getMessage(), exception.getMessage());
    }

    @Test
    void run_withNullOutputStream_throwNoOutputStreamCpException() {
        Throwable exception = assertThrows(CpException.class, () -> {
            this.cpApplication.run(inputs, System.in, null);
        });
        assertEquals(new CpException(E_NO_OSTREAM).getMessage(), exception.getMessage());
    }

    @Test
    void run_withEmptyArgument_throwInsufficientArgumentsCpException() {
        Throwable exception = assertThrows(CpException.class, () -> {
            this.cpApplication.run(new String[0], System.in, outputStream);
        });
        assertEquals(new CpException(E_NO_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void run_withRecursiveAndOneArgument_throwMissingArgumentCpException() throws CpException {
        String[] inp = {"-r", "foo2.txt"};
        CpApplication cpAppSpy = spy(cpApplication);
        doThrow(new CpException(E_MISSING_ARG)).when(cpAppSpy).parseArgs(inp);
        Throwable exception = assertThrows(CpException.class,
                () -> cpAppSpy.run(inp, System.in, outputStream)
        );
        assertEquals(new CpException(E_MISSING_ARG).getMessage(), exception.getMessage());

    }

    @Test
    void run_withExistingSrcFileDestFolder_runsSuccessfully(@TempDir Path tempDir) throws Exception {

        CpApplication cpAppSpy = spy(cpApplication);

        String[] inp = new String[]{fileName1, folderName1};

        CpArgsParser mockCpArgsParser = mock(CpArgsParser.class);

        doReturn(mockCpArgsParser)
                .when(cpAppSpy)
                .parseArgs(inp);
        when(mockCpArgsParser.isRecursive()).thenReturn(false);

        Path folderPath = tempDir.resolve(folderName1);
        Path dest = Files.createDirectory(folderPath);
        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(folderName1))
                    .thenReturn(dest);

            doNothing().when(cpAppSpy).cpFilesToFolder(eq(false), eq(folderName1), eq(fileName1));

            assertDoesNotThrow(() ->
                    cpAppSpy.run(inp, System.in, outputStream));

            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName1));
            verify(cpAppSpy).cpFilesToFolder(eq(false), eq(folderName1), eq(fileName1));
        }
    }

    @Test
    void run_withMultipleSrcFileNoDestFolder_throwsDestNotDirCpException(@TempDir Path tempDir) throws Exception {

        CpApplication cpAppSpy = spy(cpApplication);

        String[] inp = new String[]{fileName1, fileName2, folderName1};

        CpArgsParser mockCpArgsParser = mock(CpArgsParser.class);

        doReturn(mockCpArgsParser)
                .when(cpAppSpy)
                .parseArgs(inp);

        when(mockCpArgsParser.isRecursive()).thenReturn(false);

        Path folderPath = tempDir.resolve(folderName1);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(folderName1))
                    .thenReturn(folderPath);

            Throwable exception = assertThrows(CpException.class, () ->
                    cpAppSpy.run(inp, System.in, outputStream));

            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName1));

            assertEquals(new CpException(folderPath.getFileName() + ": " + E_IS_NOT_DIR).getMessage(), exception.getMessage());
        }
    }

    @Test
    void run_withOneSrcFileNoDestFolder_runsSuccessfully(@TempDir Path tempDir) throws Exception {

        CpApplication cpAppSpy = spy(cpApplication);

        String[] inp = new String[]{fileName1, folderName1};

        CpArgsParser mockCpArgsParser = mock(CpArgsParser.class);

        doReturn(mockCpArgsParser)
                .when(cpAppSpy)
                .parseArgs(inp);

        when(mockCpArgsParser.isRecursive()).thenReturn(false);

        Path folderPath = tempDir.resolve(folderName1);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(folderName1))
                    .thenReturn(folderPath);

            doNothing().when(cpAppSpy).cpSrcFileToDestFile(eq(false), eq(fileName1), eq(folderName1));

            assertDoesNotThrow(() ->
                    cpAppSpy.run(inp, System.in, outputStream));

            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName1));
            verify(cpAppSpy).cpSrcFileToDestFile(eq(false), eq(fileName1), eq(folderName1));
        }
    }

    @Test
    void cpSrcFileToDestFile_withNoSrcFile_throwsSrcFileNotFoundException(@TempDir Path tempDir) {
        CpApplication cpAppSpy = spy(cpApplication);

        Path filePath = tempDir.resolve(fileName1);
        Path filePath2 = tempDir.resolve(fileName2);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(fileName1))
                    .thenReturn(filePath);
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName2))
                    .thenReturn(filePath2);

            Throwable exception = assertThrows(Exception.class, () ->
                    cpAppSpy.cpSrcFileToDestFile(false, fileName1, fileName2));

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName1));
            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName2));

            assertEquals(filePath.getFileName() + ": " + E_FILE_NOT_FOUND, exception.getMessage());
        }
    }

    @Test
    void cpSrcFileToDestFile_withRecursiveAndSrcFolderDestFile_throwsDestFileNotDirException(@TempDir Path tempDir) throws Exception {
        CpApplication cpAppSpy = spy(cpApplication);

        Path filePath = tempDir.resolve(folderName1);
        Path src = Files.createDirectory(filePath);

        Path filePath2 = tempDir.resolve(fileName2);
        Path dest = Files.createFile(filePath2);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(folderName1))
                    .thenReturn(src);
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName2))
                    .thenReturn(dest);

            Throwable exception = assertThrows(Exception.class, () ->
                    cpAppSpy.cpSrcFileToDestFile(true, folderName1, fileName2));

            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName1));
            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName2));

            assertEquals(dest.getFileName() + ": " + E_IS_NOT_DIR, exception.getMessage());
        }
    }

    @Test
    void cpSrcFileToDestFile_withNoRecursiveAndSrcFolder_throwsSrcFolderIsDirException(@TempDir Path tempDir) throws Exception {
        CpApplication cpAppSpy = spy(cpApplication);

        Path filePath = tempDir.resolve(folderName1);
        Path src = Files.createDirectory(filePath);

        Path filePath2 = tempDir.resolve(fileName2);
        Path dest = Files.createFile(filePath2);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(folderName1))
                    .thenReturn(src);
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName2))
                    .thenReturn(dest);

            Throwable exception = assertThrows(Exception.class, () ->
                    cpAppSpy.cpSrcFileToDestFile(false, folderName1, fileName2));

            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName1));
            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName2));

            assertEquals(src.getFileName() + ": " + E_IS_DIR, exception.getMessage());
        }
    }

    @Test
    void cpSrcFileToDestFile_withSrcFileDestFile_copiesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        CpApplication cpAppSpy = spy(cpApplication);

        Path filePath = tempDir.resolve(fileName1);
        Path src = Files.createFile(filePath);

        Path filePath2 = tempDir.resolve(fileName2);
        Path dest = Files.createFile(filePath2);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(fileName1))
                    .thenReturn(src);
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName2))
                    .thenReturn(dest);

            assertDoesNotThrow(() ->
                    cpAppSpy.cpSrcFileToDestFile(false, fileName1, fileName2));

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName1));
            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName2));
            verify(cpAppSpy).cpSrcFileToDestFile(eq(false), eq(fileName1), eq(fileName2));

            assertEquals(Files.readAllLines(src), Files.readAllLines(dest));
        }
    }

    @Test
    void cpSrcFileToDestFile_withRecursiveAndSrcFolder_copiesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        CpApplication cpAppSpy = spy(cpApplication);

        Path folderPath = tempDir.resolve(folderName1);
        Path folder = Files.createDirectories(folderPath);
        Path filePath = folder.resolve(fileName1);
        Path file = Files.createFile(filePath);
        Files.write(file, fileText1);

        Path folderPath2 = tempDir.resolve(folderName2);


        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(folderName1))
                    .thenReturn(folder);
            ioUtils.when(() -> IOUtils.resolveFilePath(folderName2))
                    .thenReturn(folderPath2);

            assertDoesNotThrow(() ->
                    cpAppSpy.cpSrcFileToDestFile(true, folderName1, folderName2));

            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName1));
            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName2));

            assertTrue(Files.list(folderPath2)
                    .anyMatch(x -> x.getFileName().toString().equals(fileName1)));
            Path newPath = folder.resolve(fileName1);
            assertEquals(Files.readAllLines(newPath), fileText1);
        }
    }

    @Test
    void cpFilesToFolder_withNoSrcFile_throwsSrcFileNotFoundException(@TempDir Path tempDir) {
        CpApplication cpAppSpy = spy(cpApplication);

        Path filePath = tempDir.resolve(fileName1);
        Path folderPath = tempDir.resolve(folderName1);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(fileName1))
                    .thenReturn(filePath);
            ioUtils.when(() -> IOUtils.resolveFilePath(folderName1))
                    .thenReturn(folderPath);

            Throwable exception = assertThrows(Exception.class, () ->
                    cpAppSpy.cpFilesToFolder(false, folderName1, fileName1));

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName1));
            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName1));

            assertEquals(filePath.getFileName() + ": " + E_FILE_NOT_FOUND, exception.getMessage());
        }
    }

    @Test
    void cpFilesToFolder_withNoRecursiveAndSrcFolder_throwsSrcFolderIsDirException(@TempDir Path tempDir) throws Exception {
        CpApplication cpAppSpy = spy(cpApplication);

        Path folderPath = tempDir.resolve(folderName1);
        Path src = Files.createDirectory(folderPath);

        Path folderPath2 = tempDir.resolve(folderName2);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(folderName1))
                    .thenReturn(src);
            ioUtils.when(() -> IOUtils.resolveFilePath(folderName2))
                    .thenReturn(folderPath2);

            Throwable exception = assertThrows(Exception.class, () ->
                    cpAppSpy.cpFilesToFolder(false, folderName2, folderName1));

            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName1));
            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName2));

            assertEquals(src.getFileName() + ": " + E_IS_DIR, exception.getMessage());
        }
    }

    @Test
    void cpFilesToFolder_withRecursiveAndSrcFolder_copiesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        CpApplication cpAppSpy = spy(cpApplication);

        Path folderPath = tempDir.resolve(folderName1);
        Path src = Files.createDirectory(folderPath);

        Path filePath = src.resolve(fileName1);
        Path file = Files.createFile(filePath);

        Path folderPath2 = tempDir.resolve(folderName2);
        Path dest = Files.createDirectory(folderPath2);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(folderName1))
                    .thenReturn(src);
            ioUtils.when(() -> IOUtils.resolveFilePath(folderName2))
                    .thenReturn(dest);

            assertDoesNotThrow(() ->
                    cpAppSpy.cpFilesToFolder(true, folderName2, folderName1));

            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName1));
            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName2));

            verify(cpAppSpy).cpFilesToFolder(eq(true), eq(folderName2), eq(folderName1));

            assertTrue(Files.list(folderPath2).anyMatch(x -> x.getFileName().toString().equals(folderName1)));
            assertTrue(Files.list(folderPath2.resolve(folderName1))
                    .anyMatch(x -> x.getFileName().toString().equals(fileName1)));
        }
    }

    @Test
    void cpFilesToFolder_withRecursiveAndSrcFolderAsDest_copiesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        CpApplication cpAppSpy = spy(cpApplication);

        Path folderPath = tempDir.resolve(folderName1);
        Path src = Files.createDirectory(folderPath);
        Path filePath = folderPath.resolve(fileName1);
        Path file = Files.createFile(filePath);
        Files.write(file, fileText1);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(folderName1))
                    .thenReturn(src);
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName1))
                    .thenReturn(file);

            assertDoesNotThrow(() ->
                    cpAppSpy.cpFilesToFolder(true, folderName1, folderName1));

            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName1), times(2));

            verify(cpAppSpy).cpFilesToFolder(eq(true), eq(folderName1), eq(folderName1));

            assertTrue(Files.list(src).anyMatch(x -> x.getFileName().toString().equals(folderName1)));
            assertTrue(Files.exists(src.resolve(folderName1)));
            assertTrue(Files.list(src.resolve(folderName1))
                    .anyMatch(x -> x.getFileName().toString().equals(fileName1)));
        }
    }

    @Test
    void cpFilesToFolder_withSrcFile_copiesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        CpApplication cpAppSpy = spy(cpApplication);

        Path filePath = tempDir.resolve(fileName1);
        Path file = Files.createFile(filePath);
        Files.write(file, fileText1);

        Path folderPath2 = tempDir.resolve(folderName2);
        Path dest = Files.createDirectory(folderPath2);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(fileName1))
                    .thenReturn(file);
            ioUtils.when(() -> IOUtils.resolveFilePath(folderName2))
                    .thenReturn(dest);

            assertDoesNotThrow(() ->
                    cpAppSpy.cpFilesToFolder(true, folderName2, fileName1));

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName1));
            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName2));

            verify(cpAppSpy).cpFilesToFolder(eq(true), eq(folderName2), eq(fileName1));

            assertTrue(Files.list(folderPath2).anyMatch(x -> x.getFileName().toString().equals(fileName1)));

            Path newPath = dest.resolve(fileName1);
            assertEquals(Files.readAllLines(newPath), fileText1);
        }
    }

    @Test
    void cpFilesToFolder_withMultipleNonExistentSrcFiles_throwsSrcFileNotFoundException(@TempDir Path tempDir) throws Exception {
        CpApplication cpAppSpy = spy(cpApplication);

        Path filePath = tempDir.resolve(fileName1);
        Path filePath2 = tempDir.resolve(fileName2);

        Path folderPath2 = tempDir.resolve(folderName2);
        Path dest = Files.createDirectory(folderPath2);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(fileName1))
                    .thenReturn(filePath);
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName2))

                    .thenReturn(filePath2);
            ioUtils.when(() -> IOUtils.resolveFilePath(folderName2))
                    .thenReturn(dest);

            Throwable exception = assertThrows(Exception.class, () ->
                    cpAppSpy.cpFilesToFolder(true, folderName2, fileName1, fileName2));

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName1));
            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName2));
            ioUtils.verify(() -> IOUtils.resolveFilePath(folderName2));

            assertEquals(fileName1 + ": "
                            + E_FILE_NOT_FOUND + STRING_NEWLINE + "cp: " + fileName2 + ": " + E_FILE_NOT_FOUND,
                    exception.getMessage());
        }
    }


}
