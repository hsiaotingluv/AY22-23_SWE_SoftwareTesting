package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CpApplicationPublicIntegrationTest {
    private static final String TEMP = "temp-cp";
    private static final String SRC_FILE_NAME = "src_file.txt";
    private static final String SRC_FILE_CONTENT = "This is the source file.";
    private static final String SRC_DIR_NAME = "src_dir";
    private static final String DEST_FILE_NAME = "dest_file.txt";
    private static final String DEST_FILE_CONTENT = "This is the destination file.";
    private static final String DEST_DIR_NAME = "dest_dir";

    private static Path tempPath;

    private CpApplication cpApplication;

    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {
        tempPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
    }

    @BeforeEach
    void createTemp() throws IOException {
        cpApplication = new CpApplication();
        Files.createDirectory(tempPath);
    }

    @AfterEach
    void deleteTemp() throws IOException {
        Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private Path createFile(String name) throws IOException {
        return createFile(name, tempPath);
    }

    private Path createDirectory(String folder) throws IOException {
        return createDirectory(folder, tempPath);
    }

    private Path createFile(String name, Path inPath) throws IOException {
        Path path = inPath.resolve(name);
        Files.createFile(path);
        return path;
    }

    private Path createDirectory(String folder, Path inPath) throws IOException {
        Path path = inPath.resolve(folder);
        Files.createDirectory(path);
        return path;
    }

    private void writeToFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            args.add(Paths.get(TEMP, file).toString());
        }
        return args.toArray(new String[0]);
    }

    @Test
    void run_emptyFileToNonemptyFile_overwritesDestWithEmpty() throws Exception {
        createFile(SRC_FILE_NAME);
        Path destFile = createFile(DEST_FILE_NAME);
        writeToFile(destFile, DEST_FILE_CONTENT);
        cpApplication.run(toArgs("", SRC_FILE_NAME, DEST_FILE_NAME), System.in, System.out);
        assertArrayEquals(("").getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_nonemptyFileToEmptyFile_copiesContentToDest() throws Exception {
        Path srcFile = createFile(SRC_FILE_NAME);
        Path destFile = createFile(DEST_FILE_NAME);
        writeToFile(srcFile, SRC_FILE_CONTENT);
        cpApplication.run(toArgs("", SRC_FILE_NAME, DEST_FILE_NAME), System.in, System.out);
        assertArrayEquals(SRC_FILE_CONTENT.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_nonemptyFileToNonemptyFile_overwritesDest() throws Exception {
        Path srcFile = createFile(SRC_FILE_NAME);
        Path destFile = createFile(DEST_FILE_NAME);
        writeToFile(srcFile, SRC_FILE_CONTENT);
        writeToFile(destFile, DEST_FILE_CONTENT);
        cpApplication.run(toArgs("", SRC_FILE_NAME, DEST_FILE_NAME), System.in, System.out);
        assertArrayEquals(SRC_FILE_CONTENT.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_nonemptyFileToSameFile_throws() throws Exception {
        Path srcFile = createFile(SRC_FILE_NAME);
        writeToFile(srcFile, SRC_FILE_CONTENT);
        assertThrows(Exception.class, () -> cpApplication.run(toArgs("", SRC_FILE_NAME, SRC_FILE_NAME), System.in, System.out));
    }

    @Test
    void run_directoryToFile_throws() throws Exception {
        createDirectory(SRC_DIR_NAME);
        Path destFile = createFile(DEST_FILE_NAME);
        writeToFile(destFile, DEST_FILE_CONTENT);
        assertThrows(Exception.class, () -> cpApplication.run(toArgs("", SRC_DIR_NAME, DEST_FILE_NAME), System.in, System.out));
    }

    @Test
    void run_nonexistentFileToFile_throws() throws IOException {
        Path destFile = createFile(DEST_FILE_NAME);
        writeToFile(destFile, DEST_FILE_CONTENT);
        assertThrows(Exception.class, () -> cpApplication.run(toArgs("", SRC_FILE_NAME, DEST_FILE_NAME), System.in, System.out));
    }

    @Test
    void run_fileToNonexistentFile_createsNewDestFile() throws Exception {
        Path srcFile = createFile(SRC_FILE_NAME);
        writeToFile(srcFile, SRC_FILE_CONTENT);
        cpApplication.run(toArgs("", SRC_FILE_NAME, DEST_FILE_NAME), System.in, System.out);
        Path destFile = tempPath.resolve(DEST_FILE_NAME);
        assertTrue(Files.exists(destFile));
        assertArrayEquals(SRC_FILE_CONTENT.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_directoryToNonexistentFile_createsDirectoryWithDestNameWithSrcContent() throws Exception {
        String fileInSrcDirName = "file_in_src_dir.txt";
        Path srcDir = createDirectory(SRC_DIR_NAME);
        Path fileInSrcDir = createFile(fileInSrcDirName, srcDir);
        writeToFile(fileInSrcDir, SRC_FILE_CONTENT);
        cpApplication.run(toArgs("r", SRC_DIR_NAME, DEST_FILE_NAME), System.in, System.out);
        Path destFile = tempPath.resolve(DEST_FILE_NAME);
        assertTrue(Files.exists(destFile));
        assertTrue(Files.isDirectory(destFile));
        Path fileInDestDir = destFile.resolve(fileInSrcDirName);
        assertTrue(Files.exists(fileInDestDir));
        assertArrayEquals(SRC_FILE_CONTENT.getBytes(), Files.readAllBytes(fileInDestDir));
    }

    @Test
    void run_fileToEmptyDirectory_copiesToDirectory() throws Exception {
        Path srcFile = createFile(SRC_FILE_NAME);
        writeToFile(srcFile, SRC_FILE_CONTENT);
        Path destDir = createDirectory(DEST_DIR_NAME);
        cpApplication.run(toArgs("", SRC_FILE_NAME, DEST_DIR_NAME), System.in, System.out);
        Path destFile = destDir.resolve(SRC_FILE_NAME);
        assertTrue(Files.exists(destFile));
        assertArrayEquals(SRC_FILE_CONTENT.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_fileToNonemptyDirectory_copiesToDirectory() throws Exception {
        Path srcFile = createFile(SRC_FILE_NAME);
        String srcContent = "This is the source file.";
        writeToFile(srcFile, srcContent);
        Path destDir = createDirectory(DEST_DIR_NAME);
        String destOrigName = "dest_orig_file.txt";
        Path destOrigFile = createFile(destOrigName, destDir);
        cpApplication.run(toArgs("", SRC_FILE_NAME, DEST_DIR_NAME), System.in, System.out);
        assertTrue(Files.exists(destOrigFile));
        Path destFile = destDir.resolve(SRC_FILE_NAME);
        assertTrue(Files.exists(destFile));
        assertArrayEquals(srcContent.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_directoryToDirectoryWithFlag_copiesSrcDirectoryToDestDirectory() throws Exception {
        createDirectory(SRC_DIR_NAME);
        Path destDir = createDirectory(DEST_DIR_NAME);
        cpApplication.run(toArgs("r", SRC_DIR_NAME, DEST_DIR_NAME), System.in, System.out);
        Path destFile = destDir.resolve(SRC_DIR_NAME);
        assertTrue(Files.exists(destFile));
        assertTrue(Files.isDirectory(destFile));
    }

    @Test
    void run_directoryToDirectoryWithoutFlag_throws() throws Exception {
        createDirectory(SRC_DIR_NAME);
        createDirectory(DEST_DIR_NAME);
        assertThrows(Exception.class, () -> cpApplication.run(toArgs("", SRC_DIR_NAME, DEST_DIR_NAME), System.in, System.out));
    }

    @Test
        //Changed test case from throwing exception to running successfully - follows bash implementation
    void run_directoryToSameDirectoryWithFlag_copiesSrcDirectoryToItself() throws Exception {
        Path srcDir = createDirectory(SRC_DIR_NAME);
        String fileInSrcDirName = "file_in_src_dir.txt";
        Path fileInSrcDir = createFile(fileInSrcDirName, srcDir);
        String srcContent = "This is the file in the source directory.";
        writeToFile(fileInSrcDir, srcContent);
        cpApplication.run(toArgs("r", SRC_DIR_NAME, SRC_DIR_NAME), System.in, System.out);

        Path folderInDestDir = tempPath.resolve(SRC_DIR_NAME).resolve(SRC_DIR_NAME);
        assertTrue(Files.exists(folderInDestDir));
        Path fileInDestDir = folderInDestDir.resolve(fileInSrcDirName);
        assertTrue(Files.exists(fileInDestDir));
        assertArrayEquals(srcContent.getBytes(), Files.readAllBytes(fileInDestDir));
    }

    @Test
    void run_nonexistentDirectoryToDirectoryWithFlag_throws() throws IOException {
        String nonExistSrc = "nonexistent_dir";
        createDirectory(DEST_DIR_NAME);
        assertThrows(Exception.class, () -> cpApplication.run(toArgs("r", nonExistSrc, DEST_DIR_NAME), System.in, System.out));
    }

    @Test
    void run_fileToNonexistentDirectory_createsFileWithDestNameWithSrcContent() throws Exception {
        Path srcFile = createFile(SRC_FILE_NAME);
        writeToFile(srcFile, SRC_FILE_CONTENT);
        cpApplication.run(toArgs("", SRC_FILE_NAME, DEST_DIR_NAME), System.in, System.out);
        Path destFile = tempPath.resolve(DEST_DIR_NAME);
        assertTrue(Files.exists(destFile));
        assertArrayEquals(SRC_FILE_CONTENT.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_directoryToNonexistentDirectoryWithoutFlag_throws() throws IOException {
        createDirectory(SRC_DIR_NAME);
        String nonExistDir = "nonexistent_dir";
        assertThrows(Exception.class, () -> cpApplication.run(toArgs("", SRC_DIR_NAME, nonExistDir), System.in, System.out));
    }

    @Test
    void run_directoryToNonexistentDirectoryWithFlag_createsDirectoryWithSrcContent() throws Exception {
        Path srcDir = createDirectory(SRC_DIR_NAME);
        String fileInSrcDirName = "file_in_src_dir.txt";
        Path fileInSrcDir = createFile(fileInSrcDirName, srcDir);
        String srcContent = "This is the file in the source directory.";
        writeToFile(fileInSrcDir, srcContent);
        cpApplication.run(toArgs("r", SRC_DIR_NAME, DEST_DIR_NAME), System.in, System.out);
        Path destDir = tempPath.resolve(DEST_DIR_NAME);
        assertTrue(Files.exists(destDir));
        assertTrue(Files.isDirectory(destDir));
        Path fileInDestDir = tempPath.resolve(DEST_DIR_NAME).resolve(fileInSrcDirName);
        assertTrue(Files.exists(fileInDestDir));
        assertArrayEquals(srcContent.getBytes(), Files.readAllBytes(fileInDestDir));
    }

    @Test
    void run_missingSrcAndDestArguments_throws() {
        assertThrows(Exception.class, () -> cpApplication.run(toArgs(""), System.in, System.out));
    }

    @Test
    void run_fileToMissingDestArgument_throws() throws IOException {
        createFile(SRC_FILE_NAME);
        assertThrows(Exception.class, () -> cpApplication.run(toArgs("", SRC_FILE_NAME), System.in, System.out));
    }

    @Test
    void run_directoryToMissingDestArgument_throws() throws IOException {
        createDirectory(SRC_DIR_NAME);
        assertThrows(Exception.class, () -> cpApplication.run(toArgs("", SRC_DIR_NAME), System.in, System.out));
    }

    @Test
    void run_multipleFilesToDirectory_copiesToDirectory() throws Exception {
        String srcAName = "srcA_file.txt";
        String srcBName = "srcB_file.txt";
        createFile(srcAName);
        createFile(srcBName);
        Path destDir = createDirectory(DEST_DIR_NAME);
        cpApplication.run(toArgs("", srcAName, srcBName, DEST_DIR_NAME), System.in, System.out);
        Path destAFile = destDir.resolve(srcAName);
        Path destBFile = destDir.resolve(srcBName);
        assertTrue(Files.exists(destAFile));
        assertTrue(Files.exists(destBFile));
    }

    @Test
    void run_multipleFilesToFile_throws() throws IOException {
        String srcAName = "srcA_file.txt";
        String srcBName = "srcB_file.txt";
        createFile(srcAName);
        createFile(srcBName);
        createFile(DEST_FILE_NAME);
        assertThrows(Exception.class, () -> cpApplication.run(toArgs("", srcAName, srcBName, DEST_FILE_NAME), System.in, System.out));
    }

    @Test
    void run_multipleDirectoriesToDirectory_copiesToDirectory() throws Exception {
        String srcAName = "srcA_dir";
        String srcBName = "srcB_dir";
        createDirectory(srcAName);
        createDirectory(srcBName);
        Path destDir = createDirectory(DEST_DIR_NAME);
        cpApplication.run(toArgs("r", srcAName, srcBName, DEST_DIR_NAME), System.in, System.out);
        Path destADir = destDir.resolve(srcAName);
        Path destBDir = destDir.resolve(srcBName);
        assertTrue(Files.exists(destADir));
        assertTrue(Files.exists(destBDir));
        assertTrue(Files.isDirectory(destADir));
        assertTrue(Files.isDirectory(destBDir));
    }

    @Test
    void run_multipleDirectoriesToFile_throws() throws IOException {
        String srcAName = "srcA_dir";
        String srcBName = "srcB_dir";
        createDirectory(srcAName);
        createDirectory(srcBName);
        createFile(DEST_FILE_NAME);
        assertThrows(Exception.class, () -> cpApplication.run(toArgs("r", srcAName, srcBName, DEST_FILE_NAME), System.in, System.out));
    }

    @Test
    void run_multipleFilesAndDirectoriesToDirectory_copiesToDirectory() throws Exception {
        String srcAName = "srcA_file.txt";
        String srcBName = "srcB_dir";
        createFile(srcAName);
        createDirectory(srcBName);
        Path destDir = createDirectory(DEST_DIR_NAME);
        cpApplication.run(toArgs("r", srcAName, srcBName, DEST_DIR_NAME), System.in, System.out);
        Path destAFile = destDir.resolve(srcAName);
        Path destBFile = destDir.resolve(srcBName);
        assertTrue(Files.exists(destAFile));
        assertTrue(Files.exists(destBFile));
    }

    @Test
    void run_multipleFilesAndDirectoriesToFile_throws() throws Exception {
        String srcAName = "srcA_file.txt";
        String srcBName = "srcB_dir";
        createFile(srcAName);
        createDirectory(srcBName);
        createFile(DEST_FILE_NAME);
        assertThrows(Exception.class, () -> cpApplication.run(toArgs("", srcAName, srcBName, DEST_FILE_NAME), System.in, System.out));
    }
}
