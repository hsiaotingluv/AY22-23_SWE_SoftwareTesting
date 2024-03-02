package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CatApplicationPublicIntegrationTest {
    private static final String TEMP = "temp-cat";
    private static final String DIR = "dir";
    public static final String E_IS_DIR = String.format("cat: %s: This is a directory", DIR);
    private static final String TEXT_ONE = "Test line 1" + STRING_NEWLINE + "Test line 2" + STRING_NEWLINE +
            "Test line 3";
    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static Path tempPath;
    private static Path dirPath;

    private CatApplication catApplication;

    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        String initialDir = TestEnvironmentUtil.getCurrentDirectory();
        tempPath = Paths.get(initialDir, TEMP);
        dirPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP + CHAR_FILE_SEP + DIR);
        Files.createDirectory(tempPath);
        Files.createDirectory(dirPath);
    }

    @AfterAll
    static void deleteFiles() throws IOException {
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
        Files.deleteIfExists(dirPath);
        Files.delete(tempPath);
    }

    @BeforeEach
    void setUp() {
        catApplication = new CatApplication();
    }

    private void createFile(String name, String text) throws IOException {
        Path path = tempPath.resolve(name);
        Files.createFile(path);
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
        FILES.push(path);
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            if ("-".equals(file)) {
                args.add(file);
            } else {
                args.add(Paths.get(TEMP, file).toString());
            }
        }
        return args.toArray(new String[0]);
    }

    //catStdin cases
    @Test
    void run_singleStdinNoFlag_displaysStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs(""), inputStream, output);
        assertEquals((TEXT_ONE + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_singleStdinFlag_displaysNumberedStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String expectedText = "\t1 Test line 1" + STRING_NEWLINE + "\t2 Test line 2" + STRING_NEWLINE + "\t3 Test line 3";
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n"), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_singleStdinDashNoFlag_displaysStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("", "-"), inputStream, output);
        assertEquals((TEXT_ONE + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_singleStdinDashFlag_displaysNumberedStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String expectedText = "\t1 Test line 1" + STRING_NEWLINE + "\t2 Test line 2" + STRING_NEWLINE + "\t3 Test line 3";
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n", "-"), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_singleEmptyStdinNoFlag_displaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs(""), inputStream, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_singleEmptyStdinFlag_displaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n"), inputStream, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    //catFiles cases
    @Test
    void run_singleFileNoFlag_displaysFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileA.txt";
        String text = TEXT_ONE;
        createFile(fileName, text);
        catApplication.run(toArgs("", fileName), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_singleFileFlag_displaysNumberedFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileB.txt";
        String expectedText = "\t1 Test line 1" + STRING_NEWLINE + "\t2 Test line 2" + STRING_NEWLINE + "\t3 Test line 3";
        createFile(fileName, TEXT_ONE);
        catApplication.run(toArgs("n", fileName), System.in, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_singleEmptyFileNoFlag_displaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileC.txt";
        String text = "";
        createFile(fileName, text);
        catApplication.run(toArgs("", fileName), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_singleEmptyFileFlag_displaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileD.txt";
        String text = "";
        createFile(fileName, text);
        catApplication.run(toArgs("n", fileName), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_singleFileUnknownFlag_throwsException() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileE.txt";
        createFile(fileName, TEXT_ONE);
        assertThrows(Exception.class, () -> catApplication.run(toArgs("a", fileName), System.in, output));
    }

    @Test
    void run_multipleFilesNoFlag_displaysCatFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileF.txt";
        String fileName2 = "fileG.txt";
        String text1 = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2" + STRING_NEWLINE + "Test line 1.3";
        String text2 = "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
        String expectedText = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2" + STRING_NEWLINE + "Test line 1.3" + STRING_NEWLINE + "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
        createFile(fileName1, text1);
        createFile(fileName2, text2);
        catApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_multipleFilesFlag_displaysNumberedCatFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileH.txt";
        String fileName2 = "fileI.txt";
        String text1 = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2" + STRING_NEWLINE + "Test line 1.3";
        String text2 = "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
        String expectedText = "\t1 Test line 1.1" + STRING_NEWLINE + "\t2 Test line 1.2" + STRING_NEWLINE + "\t3 Test line 1.3" + STRING_NEWLINE + "\t1 Test line 2.1" + STRING_NEWLINE + "\t2 Test line 2.2";
        createFile(fileName1, text1);
        createFile(fileName2, text2);
        catApplication.run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_multipleEmptyFilesNoFlag_displaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileJ.txt";
        String fileName2 = "fileK.txt";
        String text = "";
        createFile(fileName1, text);
        createFile(fileName2, text);
        catApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_multipleEmptyFilesFlag_displaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileL.txt";
        String fileName2 = "fileM.txt";
        String text = "";
        createFile(fileName1, text);
        createFile(fileName2, text);
        catApplication.run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    //catFilesAndStdin cases
    @Test
    void run_singleStdinDashSingleFileNoFlag_displaysCatStdinFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2" + STRING_NEWLINE + "Test line 1.3";
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        String fileName = "fileN.txt";
        String fileText = "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
        createFile(fileName, fileText);
        String expectedText = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2" + STRING_NEWLINE + "Test line 1.3" + STRING_NEWLINE + "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
        catApplication.run(toArgs("", "-", fileName), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_singleFileSingleStdinDashNoFlag_displaysCatFileStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileText = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2" + STRING_NEWLINE + "Test line 1.3";
        String fileName = "fileO.txt";
        createFile(fileName, fileText);
        String stdinText = "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        String expectedText = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2" + STRING_NEWLINE + "Test line 1.3" + STRING_NEWLINE + "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
        catApplication.run(toArgs("", fileName, "-"), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }
}

