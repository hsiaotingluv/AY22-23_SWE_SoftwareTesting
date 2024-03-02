package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;

public class MvApplicationTest {

    private MvApplication mvApplication;

    private String fileName1;
    private String fileName2;
    private String folderName1;
    private String folderName2;
    private List<String> fileText1;
    private List<String> fileText2;

    @BeforeEach
    void init() {
        this.mvApplication = new MvApplication();
        this.fileName1 = "foo.txt";
        this.fileName2 = "foo1.txt";
        this.folderName1 = "folder1";
        this.folderName2 = "folder2";
        this.fileText1 = Arrays.asList("hi");
        this.fileText2 = Arrays.asList("hello");
    }

    @Test
    public void run_withNullArgs_throwsNullArgsException() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MvException exception = assertThrows(MvException.class,
                () -> mvApplication.run(null, System.in, outputStream)
        );

        assertEquals(new MvException(E_NULL_ARGS).getMessage(), exception.getMessage());

    }

    @Test
    public void mvSrcFileToDestFile_noOverWriteExistingSrcFileNoDestFile_movesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        String srcFile = fileName1;
        Path srcFilePath = tempDir.resolve(srcFile);
        Path src = Files.createFile(srcFilePath);
        Files.write(src, fileText1);

        String destFile = fileName2;
        Path destFilePath = tempDir.resolve(destFile);

        assertTrue(Files.exists(src));
        assertFalse(Files.exists(destFilePath));

        MvApplication mvAppSpy = spy(mvApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveExistFilePath(srcFile))
                    .thenReturn(srcFilePath);
            ioUtils.when(() -> IOUtils.resolveFilePath(destFile))
                    .thenReturn(destFilePath);

            assertDoesNotThrow(() -> mvAppSpy.mvSrcFileToDestFile(false, srcFile, destFile));

            assertFalse(Files.exists(src));
            assertTrue(Files.exists(destFilePath));
            assertEquals(fileText1, Files.readAllLines(destFilePath));
        }
    }

    @Test
    public void mvSrcFileToDestFile_overWriteExistingSrcFileNoDestFile_movesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        String srcFile = fileName1;
        Path srcFilePath = tempDir.resolve(srcFile);
        Path src = Files.createFile(srcFilePath);
        Files.write(src, fileText1);

        String destFile = fileName2;
        Path destFilePath = tempDir.resolve(destFile);

        assertTrue(Files.exists(src));
        assertFalse(Files.exists(destFilePath));

        MvApplication mvAppSpy = spy(mvApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveExistFilePath(srcFile))
                    .thenReturn(src);
            ioUtils.when(() -> IOUtils.resolveFilePath(destFile))
                    .thenReturn(destFilePath);

            assertDoesNotThrow(() -> mvAppSpy.mvSrcFileToDestFile(true, srcFile, destFile));

            assertFalse(Files.exists(src));
            assertTrue(Files.exists(destFilePath));
            assertEquals(fileText1, Files.readAllLines(destFilePath));
        }
    }

    @Test
    public void mvSrcFileToDestFile_noOverWriteExistingSrcDestFile_noChange(@TempDir Path tempDir) throws Exception {
        String srcFile = fileName1;
        Path srcFilePath = tempDir.resolve(srcFile);
        Path src = Files.createFile(srcFilePath);
        Files.write(src, fileText1);

        String destFile = fileName2;
        Path destFilePath = tempDir.resolve(destFile);
        Path dest = Files.createFile(destFilePath);
        Files.write(dest, fileText2);

        assertTrue(Files.exists(src));
        assertTrue(Files.exists(dest));

        MvApplication mvAppSpy = spy(mvApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveExistFilePath(srcFile))
                    .thenReturn(src);
            ioUtils.when(() -> IOUtils.resolveFilePath(destFile))
                    .thenReturn(dest);

            assertDoesNotThrow(() -> mvAppSpy.mvSrcFileToDestFile(false, srcFile, destFile));

            assertTrue(Files.exists(src));
            assertTrue(Files.exists(dest));
            assertEquals(fileText1, Files.readAllLines(src));
            assertEquals(fileText2, Files.readAllLines(dest));
        }
    }

    @Test
    public void mvSrcFileToDestFile_overWriteExistingSrcDestFile_movesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        String srcFile = fileName1;
        Path srcFilePath = tempDir.resolve(srcFile);
        Path src = Files.createFile(srcFilePath);
        Files.write(src, fileText1);

        String destFile = fileName2;
        Path destFilePath = tempDir.resolve(destFile);
        Path dest = Files.createFile(destFilePath);
        Files.write(dest, fileText2);

        assertTrue(Files.exists(src));
        assertTrue(Files.exists(dest));

        MvApplication mvAppSpy = spy(mvApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveExistFilePath(srcFile))
                    .thenReturn(src);
            ioUtils.when(() -> IOUtils.resolveFilePath(destFile))
                    .thenReturn(dest);

            assertDoesNotThrow(() -> mvAppSpy.mvSrcFileToDestFile(true, srcFile, destFile));

            assertFalse(Files.exists(src));
            assertTrue(Files.exists(dest));
            assertEquals(fileText1, Files.readAllLines(dest));
        }
    }

    @Test
    public void mvSrcFileToDestFile_noOverWriteExistingSrcFolderNoDestFolder_movesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        String srcFile = folderName1;
        Path srcFilePath = tempDir.resolve(srcFile);
        Path src = Files.createDirectory(srcFilePath);

        String srcFile2 = fileName1;
        Path srcFilePath2 = src.resolve(srcFile2);
        Path src2 = Files.createFile(srcFilePath2);

        String destFile = folderName2;
        Path destFilePath = tempDir.resolve(destFile);

        assertTrue(Files.exists(src));
        assertFalse(Files.exists(destFilePath));

        MvApplication mvAppSpy = spy(mvApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveExistFilePath(srcFile))
                    .thenReturn(src);
            ioUtils.when(() -> IOUtils.resolveFilePath(destFile))
                    .thenReturn(destFilePath);

            assertDoesNotThrow(() -> mvAppSpy.mvSrcFileToDestFile(true, srcFile, destFile));

            assertFalse(Files.exists(src));
            assertTrue(Files.exists(destFilePath));
            assertTrue(Files.isDirectory(destFilePath));
            assertTrue(Files.exists(Paths.get(destFilePath + File.separator + fileName1)));
        }
    }

    @Test
    public void mvFilesToFolder_noOverWriteExistingSrcFileDestFolder_movesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        MvApplication mvAppSpy = spy(mvApplication);

        String srcFile = fileName1;
        Path srcFilePath = tempDir.resolve(srcFile);
        Path src = Files.createFile(srcFilePath);
        Files.write(src, fileText1);

        String destFile = folderName2;
        Path destFilePath = tempDir.resolve(destFile);
        Path dest = Files.createDirectory(destFilePath);

        String destFile2 = fileName2;
        Path destFilePath2 = destFilePath.resolve(destFile2);
        Path dest2 = Files.createFile(destFilePath2);

        assertTrue(Files.exists(src));
        assertTrue(Files.exists(dest));
        assertTrue(Files.isDirectory(dest));
        assertTrue(Files.exists(dest2));

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(srcFile))
                    .thenReturn(src);

            ioUtils.when(() -> IOUtils.resolveExistFilePath(destFile))
                    .thenReturn(dest);

            ioUtils.when(() -> IOUtils.resolveExistFilePath(destFile2))
                    .thenReturn(dest2);

            assertDoesNotThrow(() -> mvAppSpy.mvFilesToFolder(true, destFile, srcFile));

            assertFalse(Files.exists(src));
            assertTrue(Files.exists(Paths.get(destFilePath + File.separator + srcFile)));
            assertTrue(Files.exists(Paths.get(destFilePath + File.separator + destFile2)));
        }
    }

    @Test
    public void mvFilesToFolder_overWriteExistingSrcFileDestFolder_movesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        MvApplication mvAppSpy = spy(mvApplication);

        String srcFile = fileName1;
        Path srcFilePath = tempDir.resolve(srcFile);
        Path src = Files.createFile(srcFilePath);
        Files.write(src, fileText1);

        String destFile = folderName2;
        Path destFilePath = tempDir.resolve(destFile);
        Path dest = Files.createDirectory(destFilePath);

        String destFile2 = fileName1;
        Path destFilePath2 = destFilePath.resolve(destFile2);
        Path dest2 = Files.createFile(destFilePath2);
        Files.write(dest2, fileText2);

        assertTrue(Files.exists(src));
        assertTrue(Files.exists(dest));
        assertTrue(Files.isDirectory(dest));
        assertTrue(Files.exists(dest2));

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(srcFile))
                    .thenReturn(src);

            ioUtils.when(() -> IOUtils.resolveExistFilePath(destFile))
                    .thenReturn(dest);

            assertDoesNotThrow(() -> mvAppSpy.mvFilesToFolder(true, destFile, srcFile));

            assertFalse(Files.exists(src));
            assertTrue(Files.exists(dest2));
            assertEquals(fileText1, Files.readAllLines(dest2));
        }
    }

    @Test
    public void mvFilesToFolder_noOverWriteMultipleSrcFileDestFolder_movesFileSuccessfully(@TempDir Path tempDir) throws Exception {
        MvApplication mvAppSpy = spy(mvApplication);

        String srcFile = fileName1;
        Path srcFilePath = tempDir.resolve(srcFile);
        Path src = Files.createFile(srcFilePath);
        Files.write(src, fileText1);

        String srcFile2 = fileName2;
        Path srcFilePath2 = tempDir.resolve(srcFile2);
        Path src2 = Files.createFile(srcFilePath2);
        Files.write(src2, fileText2);

        String destFile = folderName2;
        Path destFilePath = tempDir.resolve(destFile);
        Path dest = Files.createDirectory(destFilePath);

        assertTrue(Files.exists(src));
        assertTrue(Files.exists(src2));
        assertTrue(Files.exists(dest));
        assertTrue(Files.isDirectory(dest));

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(srcFile))
                    .thenReturn(src);

            ioUtils.when(() -> IOUtils.resolveFilePath(srcFile2))
                    .thenReturn(src2);

            ioUtils.when(() -> IOUtils.resolveExistFilePath(destFile))
                    .thenReturn(dest);

            assertDoesNotThrow(() -> mvAppSpy.mvFilesToFolder(true, destFile, srcFile, srcFile2));

            assertFalse(Files.exists(src));
            assertFalse(Files.exists(src2));
            assertTrue(Files.exists(Paths.get(destFilePath + File.separator + srcFile)));
            assertTrue(Files.exists(Paths.get(destFilePath + File.separator + srcFile2)));
        }
    }
}
