package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RmApplicationPublicTest {
    private static final String TEMP = "temp-rm" + File.separator;
    private static final String DIR_A = "rmDirectoryA";
    private static final String DIR_B = "rmDirectoryB";
    private static final String FILE_A = "rmA";
    private static final String FILE_B = "rmB";
    private static final String FILE_C = "rmC";
    private static final String FILE_D = "rmD";

    private static Path tempPath;
    private RmApplication rmApplication;


    @BeforeAll
    static void createTemp() throws NoSuchFieldException, IllegalAccessException {
        tempPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
    }

    @BeforeEach
    public void setUp() throws Exception {
        rmApplication = new RmApplication();
        Files.createDirectory(tempPath);
        Files.createFile(Paths.get(TEMP + FILE_A));
        Files.createFile(Paths.get(TEMP + FILE_B));
        Files.createFile(Paths.get(TEMP + FILE_C));
        Files.createDirectory(Paths.get(TEMP + DIR_A));
        Files.createDirectory(Paths.get(TEMP + DIR_B));
        Files.createFile(Paths.get(TEMP + DIR_A + File.separator + FILE_D));
    }

    @AfterEach
    public void clean() throws Exception {
        Files.walk(Paths.get(TEMP))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void removeTest_removeSingleFileNoFlag_removesFile() throws Exception {
        rmApplication.remove(false, false, TEMP + FILE_A);
        assertFalse(Files.exists(Paths.get(TEMP + FILE_A)));
    }

    @Test
    public void removeTest_removeEmptyFolderNoFlag_throwsException() {
        assertThrows(Exception.class, () -> rmApplication.remove(false, false, TEMP + DIR_B));
    }

    @Test
    public void removeTest_removeEmptyFolderEmptyFlag_removesFolder() throws Exception {
        rmApplication.remove(true, false, TEMP + DIR_B);
        assertFalse(Files.exists(Paths.get(TEMP + DIR_B)));
    }

    @Test
    public void removeTest_removeFolderNoFlag_throwsException() {
        assertThrows(Exception.class, () -> rmApplication.remove(false, false, TEMP + DIR_A));
    }

    @Test
    public void removeTest_removeFolderEmptyFlag_throwsException() {
        assertThrows(Exception.class, () -> rmApplication.remove(true, false, TEMP + DIR_A));
    }

    @Test
    public void removeTest_removeMultipleFilesNoFlag_removesSelectedFiles() throws Exception {
        rmApplication.remove(false, false, TEMP + FILE_A, TEMP + FILE_B, TEMP + FILE_C);
        assertFalse(Files.exists(Paths.get(TEMP + FILE_A)));
        assertFalse(Files.exists(Paths.get(TEMP + FILE_B)));
        assertFalse(Files.exists(Paths.get(TEMP + FILE_C)));
    }

    @Test
    public void removeTest_removeFolderRecurseFlag_removesAllFoldersAndFiles() throws Exception {
        rmApplication.remove(false, true, TEMP + DIR_A);
        assertFalse(Files.exists(Paths.get(TEMP + DIR_A)));
        assertFalse(Files.exists(Paths.get(TEMP + DIR_A + File.separator + FILE_D)));
    }
}
