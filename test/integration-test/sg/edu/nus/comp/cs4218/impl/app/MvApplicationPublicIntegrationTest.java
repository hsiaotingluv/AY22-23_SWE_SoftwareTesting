package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class MvApplicationPublicIntegrationTest {

    private static final String SUBFOLDER = "subfolder";
    private static final String SUBFOLDER_1 = "subfolder1";
    private static final String SUBFOLDER_2 = "subfolder2";
    private static final String SUBFOLDER_3 = "subfolder3";
    private static final String SUB_SUBFOLDER_2 = "subsubfolder2";
    private static final String SUB_SUBFOLDER_1 = "subsubfolder1";
    private static final String FILE_1_TXT = "file1.txt";
    private static final String FILE_2_TXT = "file2.txt";
    private static final String BLOCKED_FILE = "blocked";
    private static final String UNWRITABLE_FILE = "unwritable";
    private static final String FILE1_CONTENTS = "This is file1.txt content";
    private static final String FILE2_CONTENTS = "This is another file2.txt content";
    private static final String SUBFILE2_CONTENTS = "This is a subfolder1 file2.txt content";
    @TempDir
    File tempDir;
    private MvApplication mvApplication;

    @AfterAll
    static void tearDownAll() throws NoSuchFieldException, IllegalAccessException {
        TestEnvironmentUtil.setCurrentDirectory(System.getProperty("user.dir"));
    }

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        mvApplication = new MvApplication();
        TestEnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath());

        new File(tempDir, SUBFOLDER_1).mkdir();
        new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUB_SUBFOLDER_1).mkdir();
        new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT).createNewFile();
        File subFile2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        FileWriter subFile2Writer = new FileWriter(subFile2);
        subFile2Writer.write(SUBFILE2_CONTENTS);
        subFile2Writer.close();

        new File(tempDir, SUBFOLDER_2).mkdir();
        new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2).mkdir();

        new File(tempDir, SUBFOLDER_3).mkdir();

        new File(tempDir, FILE_1_TXT).createNewFile();
        File file1 = new File(tempDir, FILE_1_TXT);
        FileWriter file1Writer = new FileWriter(file1);
        file1Writer.write(FILE1_CONTENTS);
        file1Writer.close();

        new File(tempDir, FILE_2_TXT).createNewFile();
        File file2 = new File(tempDir, FILE_2_TXT);
        FileWriter file2Writer = new FileWriter(file2);
        file2Writer.write(FILE2_CONTENTS);
        file2Writer.close();

        File blockedFolder = new File(tempDir, BLOCKED_FILE);
        blockedFolder.mkdir();
        blockedFolder.setWritable(false);

        File unwritableFile = new File(tempDir, UNWRITABLE_FILE);
        unwritableFile.createNewFile();
        unwritableFile.setWritable(false);
    }

    @AfterEach
    void tearDown() {
        // set files and folders to be writable to enable clean up
        File blockedFolder = new File(tempDir, BLOCKED_FILE);
        blockedFolder.setWritable(true);
        File unwritableFile = new File(tempDir, UNWRITABLE_FILE);
        unwritableFile.setWritable(true);
    }

    @Test
    public void run_nullArgs_throwsException() {
        assertThrows(Exception.class, () -> mvApplication.run(null, System.in, System.out));
    }

    @Test
    public void run_invalidFlag_throwsException() {
        String[] argList = new String[]{"-a", FILE_1_TXT, FILE_2_TXT};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_invalidNumOfArgs_throwsException() {
        String[] argList = new String[]{"-n", FILE_2_TXT};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_unwritableSrcFile_throwsException() {
        // no permissions to rename unwritable
        String[] argList = new String[]{UNWRITABLE_FILE, "file4.txt"};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_unwritableDestFileWithoutFlag_throwsException() throws Exception {
        // no permissions to override unwritable
        String[] argList = new String[]{FILE_2_TXT, UNWRITABLE_FILE};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));

        File expectedSrcFile = new File(tempDir, FILE_2_TXT);
        File expectedDestFile = new File(tempDir, UNWRITABLE_FILE);

        assertTrue(expectedSrcFile.exists());
        assertTrue(expectedDestFile.exists());
        List<String> expFileContents = Files.readAllLines(expectedDestFile.toPath());
        assertEquals(0, expFileContents.size());
    }

    @Test
    public void run_unwritableDestFileWithFlag_noChange() throws Exception {
        // not overriding unwritable, so no error thrown
        String[] argList = new String[]{"-n", FILE_2_TXT, UNWRITABLE_FILE};
        mvApplication.run(argList, System.in, System.out);

        // no change
        File expectedSrcFile = new File(tempDir, FILE_2_TXT);
        File expectedDestFile = new File(tempDir, UNWRITABLE_FILE);

        assertTrue(expectedSrcFile.exists());
        assertTrue(expectedDestFile.exists());
        List<String> expFileContents = Files.readAllLines(expectedDestFile.toPath());
        assertEquals(0, expFileContents.size());
    }

    @Test
    @DisabledOnOs(WINDOWS)
    public void run_unwritableDestFolder_throwsException() {
        // no permissions to move files into blocked folder
        String[] argList = new String[]{FILE_1_TXT, FILE_2_TXT, BLOCKED_FILE};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));

        File expFile1 = new File(tempDir, FILE_1_TXT);
        File expFile2 = new File(tempDir, FILE_2_TXT);

        assertTrue(expFile1.exists());
        assertTrue(expFile2.exists());
    }

    @Test
    public void run_withoutFlag2ArgsDestExist_removeSrcAndOverrideFile() throws Exception {
        String[] argList = new String[]{FILE_1_TXT, FILE_2_TXT};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, FILE_1_TXT);
        File expNew = new File(tempDir, FILE_2_TXT);

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        List<String> expFileContents = Files.readAllLines(expNew.toPath());
        assertEquals(FILE1_CONTENTS, expFileContents.get(0));
    }

    @Test
    public void run_withFlag2ArgsDestFileExist_noChange() throws Exception {
        String[] argList = new String[]{"-n", FILE_1_TXT, FILE_2_TXT};
        mvApplication.run(argList, System.in, System.out);

        File expOld = new File(tempDir, FILE_1_TXT);
        File expNew = new File(tempDir, FILE_2_TXT);

        assertTrue(expOld.exists());
        List<String> expOldContents = Files.readAllLines(expOld.toPath());
        assertEquals(FILE1_CONTENTS, expOldContents.get(0));
        assertTrue(expNew.exists());
        List<String> expFileContents = Files.readAllLines(expNew.toPath());
        assertEquals(FILE2_CONTENTS, expFileContents.get(0));
    }

    @Test
    public void run_withoutFlags2ArgsDestFileNonExist_renameFile() throws Exception {
        String[] argList = new String[]{FILE_2_TXT, "file4.txt"};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, FILE_2_TXT);
        File expNew = new File(tempDir, "file4.txt");

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        List<String> expFileContents = Files.readAllLines(expNew.toPath());
        assertEquals(FILE2_CONTENTS, expFileContents.get(0));
    }

    @Test
    public void run_withFlagRenameOneSubFileIntoFolder_renameSubFile() throws Exception {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT, "file5.txt"};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expNew = new File(tempDir, "file5.txt");

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        List<String> expFileContents = Files.readAllLines(expNew.toPath());
        assertEquals(SUBFILE2_CONTENTS, expFileContents.get(0));
    }

    @Test
    public void run_withFlagRenameOneSubFileIntoSubFile_renameSubFile() throws Exception {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT,
                SUBFOLDER_2 + CHAR_FILE_SEP + "file5.txt"};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expNew = new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP + "file5.txt");

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        List<String> expFileContents = Files.readAllLines(expNew.toPath());
        assertEquals(SUBFILE2_CONTENTS, expFileContents.get(0));
    }

    @Test
    public void run_withFlags2ArgsDestFoldersNonExist_renameFile() throws Exception {
        String[] argList = new String[]{"-n", SUBFOLDER_1, "newSubFolder"};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, SUBFOLDER_1);
        File expNew = new File(tempDir, "newSubFolder");

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        assertTrue(Files.isDirectory(expNew.toPath()));
        List<String> subFiles = Arrays.stream(expNew.listFiles()).map(File::getName).collect(Collectors.toList());
        assertEquals(2, subFiles.size());
        assertTrue(subFiles.contains(FILE_2_TXT));
        assertTrue(subFiles.contains(SUB_SUBFOLDER_1));
    }

    @Test
    public void run_withFlags2ArgsSrcFolderDestFileNonExist_renameFile() throws Exception {
        String[] argList = new String[]{"-n", SUBFOLDER_1, "file3.txt"};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, SUBFOLDER_1);
        File expNew = new File(tempDir, "file3.txt");

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        assertTrue(Files.isDirectory(expNew.toPath()));
        List<String> subFiles = Arrays.stream(expNew.listFiles()).map(File::getName).collect(Collectors.toList());
        assertEquals(2, subFiles.size());
        assertTrue(subFiles.contains(FILE_2_TXT));
        assertTrue(subFiles.contains(SUB_SUBFOLDER_1));
    }

    @Test
    public void run_withFlags2ArgsDiffFileTypesNonExist_convertFolderToFile() throws Exception {
        String[] argList = new String[]{"-n", FILE_1_TXT, "file1.png"};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, FILE_1_TXT);
        File expNew = new File(tempDir, "file1.png");

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        List<String> expFileContents = Files.readAllLines(expNew.toPath());
        assertEquals(FILE1_CONTENTS, expFileContents.get(0));
    }

    @Test
    public void run_withoutFlagsSameSrcAndDestExist_noChange() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String[] argList = new String[]{FILE_1_TXT, FILE_1_TXT};
        mvApplication.run(argList, System.in, output);
        File expectedFile = new File(tempDir, FILE_1_TXT);
        assertTrue(expectedFile.exists());
        List<String> expFileContents = Files.readAllLines(expectedFile.toPath());
        assertEquals(FILE1_CONTENTS, expFileContents.get(0));
    }

    @Test
    public void run_withoutFlagsSameSrcToCurrFolder_throwException() {
        String[] argList = new String[]{FILE_1_TXT, "."};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_invalidSourceFile_throwException() {
        String[] argList = new String[]{"file3.txt", FILE_1_TXT};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_withoutFlagMoveOneFileIntoFolder_movedIntoFolder() throws Exception {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_1};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, FILE_1_TXT);
        File expNew = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        List<String> expFileContents = Files.readAllLines(expNew.toPath());
        assertEquals(FILE1_CONTENTS, expFileContents.get(0));
    }

    @Test
    public void run_withoutFlagMoveOneSubFileIntoFolder_movedIntoFolder() throws Exception {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT, SUBFOLDER_2};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expNew = new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP + FILE_2_TXT);

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        List<String> expFileContents = Files.readAllLines(expNew.toPath());
        assertEquals(SUBFILE2_CONTENTS, expFileContents.get(0));
    }

    @Test
    public void run_withoutFlagMoveOneSubFileIntoSubSFolder_movedIntoSubFolder() throws Exception {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT,
                SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expNew = new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2 +
                CHAR_FILE_SEP + FILE_2_TXT);

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        List<String> expFileContents = Files.readAllLines(expNew.toPath());
        assertEquals(SUBFILE2_CONTENTS, expFileContents.get(0));
    }

    @Test
    public void run_withoutFlagMoveOneAbsolutePathFileIntoSubFolder_movedIntoSubFolder() throws Exception {
        String[] argList = new String[]{tempDir.getAbsolutePath() + CHAR_FILE_SEP + SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT,
                tempDir.getAbsolutePath() + CHAR_FILE_SEP + SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expNew = new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP +
                SUB_SUBFOLDER_2 + CHAR_FILE_SEP + FILE_2_TXT);

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        List<String> expFileContents = Files.readAllLines(expNew.toPath());
        assertEquals(SUBFILE2_CONTENTS, expFileContents.get(0));
    }

    @Test
    public void run_withoutFlagsMoveOneFolderIntoFolder_movedIntoFolder() throws Exception {
        String[] argList = new String[]{SUBFOLDER_2, SUBFOLDER_1};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile = new File(tempDir, SUBFOLDER_2);
        File expNew = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertFalse(expRmFile.exists());
        assertTrue(expNew.exists());
        assertTrue(Files.isDirectory(expNew.toPath()));
        File[] subFiles = expNew.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_withoutFlagsMoveMultipleFilesIntoFolder_movedAllIntoFolder() throws Exception {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_2, SUBFOLDER_1};
        mvApplication.run(argList, System.in, System.out);

        File expSubFile2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expRmFile1 = new File(tempDir, FILE_1_TXT);
        File expRmSubFolder2 = new File(tempDir, SUBFOLDER_2);
        File expNew1 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);
        File expNewSubFolder2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertTrue(expSubFile2.exists());
        assertFalse(expRmFile1.exists());
        assertFalse(expRmSubFolder2.exists());
        assertTrue(expNew1.exists());
        List<String> expNew1Contents = Files.readAllLines(expNew1.toPath());
        assertEquals(FILE1_CONTENTS, expNew1Contents.get(0));
        assertTrue(expNewSubFolder2.exists());
        assertTrue(Files.isDirectory(expNewSubFolder2.toPath()));
        File[] subFiles = expNewSubFolder2.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_withoutFlagsMoveFileWithSameNameIntoFolder_movedIntoFolderWithOverriding() throws Exception {
        String[] argList = new String[]{FILE_1_TXT, FILE_2_TXT, SUBFOLDER_2, SUBFOLDER_1};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile1 = new File(tempDir, FILE_1_TXT);
        File expRmFile2 = new File(tempDir, FILE_2_TXT);
        File expRmSubFolder2 = new File(tempDir, SUBFOLDER_2);
        File expNew1 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);
        File expNew2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expNewSubFolder2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertFalse(expRmFile1.exists());
        assertFalse(expRmFile2.exists());
        assertFalse(expRmSubFolder2.exists());
        assertTrue(expNew1.exists());
        List<String> expNew1Contents = Files.readAllLines(expNew1.toPath());
        assertEquals(FILE1_CONTENTS, expNew1Contents.get(0));
        assertTrue(expNew2.exists());
        List<String> expNew2Contents = Files.readAllLines(expNew2.toPath());
        assertEquals(FILE2_CONTENTS, expNew2Contents.get(0)); //override with file2.txt contents
        assertTrue(expNewSubFolder2.exists());
        assertTrue(Files.isDirectory(expNewSubFolder2.toPath()));
        File[] subFiles = expNewSubFolder2.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_withFlagsMoveFilesWithSameNameIntoFolder_movedIntoFolderWithoutOverriding() throws Exception {
        String[] argList = new String[]{"-n", FILE_1_TXT, FILE_2_TXT, SUBFOLDER_2, SUBFOLDER_1};
        mvApplication.run(argList, System.in, System.out);

        File expRmFile1 = new File(tempDir, FILE_1_TXT);
        File expRmFile2 = new File(tempDir, FILE_2_TXT);
        File expRmSubFolder2 = new File(tempDir, SUBFOLDER_2);
        File expNew1 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);
        File expNew2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expNewSubFolder2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertFalse(expRmFile1.exists());
        assertTrue(expRmFile2.exists()); // file2.txt not moved
        assertFalse(expRmSubFolder2.exists());
        assertTrue(expNew1.exists());
        List<String> expNew1Contents = Files.readAllLines(expNew1.toPath());
        assertEquals(FILE1_CONTENTS, expNew1Contents.get(0));
        assertTrue(expNew2.exists());
        List<String> expNew2Contents = Files.readAllLines(expNew2.toPath());
        assertEquals(SUBFILE2_CONTENTS, expNew2Contents.get(0)); //NOT override with file2.txt contents
        assertTrue(expNewSubFolder2.exists());
        assertTrue(Files.isDirectory(expNewSubFolder2.toPath()));
        File[] subFiles = expNewSubFolder2.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_nonExistentDestFolder_throwsException() {
        String[] argList = new String[]{"-n", FILE_1_TXT, FILE_2_TXT, "nonExistentFolder"};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_existentNonDirDestFile_throwsException() {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_2, FILE_2_TXT};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_nonExistentNonDirDestFile_throwsException() {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_2, "f"};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_invalidSrcFileFirst_throwsException() {
        String[] argList = new String[]{"f", SUBFOLDER_2, SUBFOLDER_1};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));

        File expFile = new File(tempDir, SUBFOLDER_2);
        assertTrue(expFile.exists());
    }

    @Test
    public void run_invalidSrcFilesAfter_throwsException() {
        String[] argList = new String[]{SUBFOLDER_2, SUBFOLDER, SUBFOLDER_1};
        assertThrows(Exception.class, () -> mvApplication.run(argList, System.in, System.out));

        File expectedMovedFile = new File(tempDir, SUBFOLDER_2);
        File expNew = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);
        assertFalse(expectedMovedFile.exists());
        assertTrue(expNew.exists());
        assertTrue(Files.isDirectory(expNew.toPath()));
        File[] subFiles = expNew.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }
}