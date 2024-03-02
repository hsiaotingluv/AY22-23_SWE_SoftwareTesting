package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

class CdApplicationPublicIntegrationTest {

    private static final String FOLDER = "folder";
    private static final String SUBFOLDER = "folder" + TestStringUtils.CHAR_FILE_SEP + "subfolder";
    private static final String BLOCKED_FOLDER = "blocked";
    private static final String VALID_FILE = "file.txt";
    @TempDir
    private static File tempDir;
    private static CdApplication cdApplication;

    @BeforeAll
    static void setupAll() throws IOException {
        new File(tempDir, FOLDER).mkdir();
        new File(tempDir, SUBFOLDER).mkdir();
        new File(tempDir, VALID_FILE).createNewFile();
        File blockedFolder = new File(tempDir, BLOCKED_FOLDER);
        blockedFolder.mkdir();
        blockedFolder.setExecutable(false);
    }

    @AfterAll
    static void tearDownAll() throws NoSuchFieldException, IllegalAccessException {
        TestEnvironmentUtil.setCurrentDirectory(System.getProperty("user.dir"));
    }

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        cdApplication = new CdApplication();
        TestEnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath());
    }

    // Cd into valid relative path
    @Test
    public void run_cdIntoValidRelativePath_success() throws Exception {
        String finalPath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + FOLDER;
        String[] argList = new String[]{FOLDER};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(finalPath, currDirectory);
    }

    @Test
    public void run_cdIntoNestedFolder_success() throws Exception {
        String finalPath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + SUBFOLDER;
        String[] argList = new String[]{SUBFOLDER};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(finalPath, currDirectory);
    }

    @Test
    public void run_cdOutFromFolder_success() throws Exception {
        String relativePath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + FOLDER;
        TestEnvironmentUtil.setCurrentDirectory(relativePath);
        String[] argList = new String[]{"../"};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(tempDir.getAbsolutePath(), currDirectory);
    }

    @Test
    public void run_cdOutFromNestedFolder_success() throws Exception {
        String relativePath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + SUBFOLDER;
        TestEnvironmentUtil.setCurrentDirectory(relativePath);
        String[] argList = new String[]{"../../"};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(tempDir.getAbsolutePath(), currDirectory);
    }

    // Cd with no args
    @Test
    public void run_cdWithNoArgs_success() throws Exception {
        String finalPath = System.getProperty("user.dir");
        String[] argList = new String[]{};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(finalPath, currDirectory);
    }

    // Cd with blank arg
    @Test
    public void run_cdIntoBlankPath_success() throws Exception {
        String finalPath = System.getProperty("user.dir");
        String[] argList = new String[]{""};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(finalPath, currDirectory);
    }

    // Cd into invalid relative path
    @Test
    public void run_invalidRelativePath_throwsException() {
        String[] argList = new String[]{"invalid"};
        assertThrows(Exception.class, () -> cdApplication.run(argList, System.in, System.out));
    }

    // Cd into valid absolute path
    @Test
    public void run_cdIntoValidAbsolutePath_success() throws Exception {
        String absolutePath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + FOLDER;
        String[] argList = new String[]{absolutePath};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(absolutePath, currDirectory);
    }

    // Cd into invalid absolute path
    @Test
    public void run_cdIntoInvalidAbsolutePath_throwsException() {
        String absolutePath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + "invalid";
        String[] argList = new String[]{absolutePath};
        assertThrows(Exception.class, () -> cdApplication.run(argList, System.in, System.out));
    }

    // Cd into non directory
    @Test
    public void run_cdIntoFile_throwsException() {
        String[] argList = new String[]{VALID_FILE};
        assertThrows(Exception.class, () -> cdApplication.run(argList, System.in, System.out));
    }

    // Cd into folder with no permissions
    @Test
    @DisabledOnOs(WINDOWS)
    public void run_blockedFolder_throwsException() {
        String[] argList = new String[]{BLOCKED_FOLDER};
        assertThrows(Exception.class, () -> cdApplication.run(argList, System.in, System.out));
    }

    // Cd with too many args
    @Test
    public void run_cdWithManyArgs_throwsException() {
        String[] argList = new String[]{FOLDER, SUBFOLDER};
        assertThrows(Exception.class, () -> cdApplication.run(argList, System.in, System.out));
    }

    // Cd with null args
    @Test
    public void run_cdWithNullArgs_throwsException() {
        assertThrows(Exception.class, () -> cdApplication.run(null, System.in, System.out));
    }

    // Cd with null input stream
    @Test
    public void run_cdWithNullInputStream_throwsException() {
        String[] argList = new String[]{};
        assertThrows(Exception.class, () -> cdApplication.run(argList, null, System.out));
    }

    // Cd with null output stream
    @Test
    public void run_cdWithNullOutputStream_throwsException() {
        String[] argList = new String[]{};
        assertThrows(Exception.class, () -> cdApplication.run(argList, System.in, null));
    }
}
