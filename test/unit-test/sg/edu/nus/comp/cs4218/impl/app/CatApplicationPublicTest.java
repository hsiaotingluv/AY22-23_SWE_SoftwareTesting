package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

public class CatApplicationPublicTest {
    private static final String TEXT_ONE = "Test line 1" + STRING_NEWLINE + "Test line 2" + STRING_NEWLINE +
            "Test line 3";
    private static final String EXPECT_ONE_NUM = "\t1 Test line 1" + STRING_NEWLINE + "\t2 Test line 2" +
            STRING_NEWLINE + "\t3 Test line 3";
    private static final String TEST_DIR = "temp-cat";
    public static final String E_IS_DIR = String.format("cat: %s: This is a directory", TEST_DIR);
    private static final String TEST_FILE = "fileA.txt";
    private static Path testDirPath;
    private static Path testFilePath;
    private CatApplication catApplication;

    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        testDirPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEST_DIR);
        Files.createDirectory(testDirPath);

        testFilePath = testDirPath.resolve(TEST_FILE);
        Files.createFile(testFilePath);
        Files.write(testFilePath, TEXT_ONE.getBytes());
    }

    @AfterAll
    static void deleteFiles() throws IOException {
        Files.delete(testFilePath);
        Files.delete(testDirPath);
    }

    @BeforeEach
    void setUp() {
        catApplication = new CatApplication();
    }

    @Test
    void catFiles_singleFileSpecifiedNoFlagAbsolutePath_returnsFileContentString() throws Exception {
        String result = catApplication.catFiles(false, testFilePath.toString());
        assertEquals(TEXT_ONE + STRING_NEWLINE, result); // not sure why it was missing a STRING_NEWLINE when everywhere else expects this
    }

    // Updated this test case to throw the exception
    @Test
    void catFiles_folderSpecifiedAbsolutePath_throwsException() throws Exception {
        CatException exception = assertThrows(CatException.class,
                () -> catApplication.catFiles(false, testDirPath.toString()));
        assertEquals(E_IS_DIR, exception.getMessage());
    }

    @Test
    void catStdin_noFlag_returnsStdinString() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes());
        String result = catApplication.catStdin(false, inputStream);
        assertEquals(TEXT_ONE + STRING_NEWLINE, result);
    }

    @Test
    void catStdin_emptyStringNoFlag_returnsEmptyString() throws Exception {
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        String result = catApplication.catStdin(false, inputStream);
        assertEquals(text, result);
    }

    @Test
    void catStdin_isLineNumberFlag_returnsStdinStringLineNo() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes());
        String result = catApplication.catStdin(true, inputStream);
        assertEquals(EXPECT_ONE_NUM + STRING_NEWLINE, result);
    }
}
