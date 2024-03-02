package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LsApplicationPublicTest {

    public static final String SRC = "src";
    private static final String TEMP = "temp-ls";
    private static final String FIRST_LVL_FOLDER = "firstLevelFolder";
    private static final String FIRST_LVL_FILE = "firstLevelFile";
    private static final String FIRST_LVL_HFILE = ".firstLevelHidden";
    private static final String SEC_LVL_FOLDER = "secLevelFolder";
    private static Path tempPath;
    private static File[] firstLvlTestFiles;
    private static File[] allTestFiles;

    private LsApplication application;

    @BeforeAll
    static void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        tempPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
        Files.createDirectories(Paths.get(tempPath.toString(), FIRST_LVL_FOLDER, SEC_LVL_FOLDER));
        Files.createFile(Paths.get(tempPath.toString(), FIRST_LVL_FILE));
        Files.createFile(Paths.get(tempPath.toString(), FIRST_LVL_HFILE));
        firstLvlTestFiles = tempPath.toFile().listFiles();
        File[] secLvlTestFiles = tempPath.resolve(Paths.get(FIRST_LVL_FOLDER)).toFile().listFiles();
        allTestFiles = Stream.concat(Arrays.stream(firstLvlTestFiles), Arrays.stream(secLvlTestFiles))
                .toArray(size -> (File[]) Array.newInstance(firstLvlTestFiles.getClass().getComponentType(), size));
    }

    @AfterAll
    static void tearDown() throws IOException {
        Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @BeforeEach
    void createTemp() {
        application = new LsApplication();
    }

    private void assertResultContainsFiles(String result, File... files) {
        for (File file : files) {
            String fileName = file.getName();
            if (file.isHidden()) {
                assertFalse(result.contains(fileName));
            } else {
                assertTrue(result.contains(fileName));
            }
        }
    }

    @Test
    public void listFolderContent_noFoldersSpecified_returnsCurrentFolderContent() throws Exception {
        String result = application.listFolderContent(false, false);
        assertTrue(result.contains(SRC));
    }

    @Test
    public void listFolderContent_singleFolderSpecified_returnsFolderContent() throws Exception {
        String result = application.listFolderContent(false, false, TEMP);
        assertResultContainsFiles(result, firstLvlTestFiles);
    }

    @Test
    public void listFolderContent_singleFolderSpecifiedRecursive_returnsAllContent() throws Exception {
        String result = application.listFolderContent(true, false, TEMP);
        assertResultContainsFiles(result, allTestFiles);

    }

    @Test
    public void listFolderContent_multipleFoldersSpecified_returnsMultipleFoldersContent() throws Exception {
        String result = application.listFolderContent(false, false,
                TestStringUtils.STRING_CURR_DIR + TestStringUtils.CHAR_FILE_SEP, TEMP);
        assertTrue(result.contains("src"));
        assertResultContainsFiles(result, firstLvlTestFiles);
    }

    @Test
    public void listFolderContent_singleFolder2LevelsDownSpecified_returnsFolderContent() throws Exception {
        String result = application.listFolderContent(false, false,
                Paths.get(tempPath.toString(), FIRST_LVL_FOLDER).toString());
        assertTrue(result.contains(SEC_LVL_FOLDER));
    }
}
