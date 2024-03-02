package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CdApplicationPublicTest {
    private static final String TEST_DIR = "temp-cd";
    private static final Path DIR_PATH = Paths.get(TEST_DIR).toAbsolutePath();
    private static final String INVALID_DIR_PATH = "invalid/testDir";
    private static String initialDirectory;
    private CdApplication cdApplication;

    @AfterEach
    void tearDown() throws IOException, NoSuchFieldException, IllegalAccessException {
        Files.deleteIfExists(DIR_PATH);
        TestEnvironmentUtil.setCurrentDirectory(initialDirectory);
    }

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        cdApplication = new CdApplication();
        initialDirectory = TestEnvironmentUtil.getCurrentDirectory();
        Files.createDirectory(DIR_PATH);
    }

    @Test
    void changeToDirectory_validPath_correctlyChangesEnvironment() throws NoSuchFieldException,
            IllegalAccessException {
        assertDoesNotThrow(() -> cdApplication.changeToDirectory(TEST_DIR));
        assertEquals(DIR_PATH.toString(), TestEnvironmentUtil.getCurrentDirectory());
    }

    @Test
    void changeToDirectory_invalidPath_correctlyChangesEnvironment() throws NoSuchFieldException,
            IllegalAccessException {
        assertThrows(Exception.class, () -> cdApplication.changeToDirectory(INVALID_DIR_PATH));
        assertNotEquals(Paths.get(INVALID_DIR_PATH).toAbsolutePath().toString(),
                TestEnvironmentUtil.getCurrentDirectory());
    }
}