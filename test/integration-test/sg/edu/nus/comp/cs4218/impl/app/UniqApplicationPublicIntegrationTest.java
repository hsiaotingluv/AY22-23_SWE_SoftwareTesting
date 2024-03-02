package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.UniqException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class UniqApplicationPublicIntegrationTest {
    private static final String INPUT_FILE_TXT = "input_file.txt";
    private static final String OUTPUT_FILE_TXT = "output_file.txt";
    private static final String TEST_INPUT = "Hello World" + STRING_NEWLINE +
            "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE;
    private static final String WO_FLAG_OUT = "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE;
    private static final String COUNT_FLAG_OUT = "\t2 Hello World" + STRING_NEWLINE +
            "\t2 Alice" + STRING_NEWLINE +
            "\t1 Bob" + STRING_NEWLINE +
            "\t1 Alice" + STRING_NEWLINE +
            "\t1 Bob" + STRING_NEWLINE;
    private static final String DUP_FLAG_OUT = "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE;
    private static final String ALL_DUP_FLAG_OUT = "Hello World" + STRING_NEWLINE +
            "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE;
    private static final String BOTH_FLAGS_OUT = "\t2 Hello World" + STRING_NEWLINE +
            "\t2 Alice" + STRING_NEWLINE;
    private Deque<Path> files;
    @TempDir
    private Path currPath;
    private UniqApplication uniqApplication;

    @BeforeEach
    void init() {
        uniqApplication = new UniqApplication();
        files = new ArrayDeque<>();
    }

    private Path createFile(String name) throws IOException {
        Path path = currPath.resolve(name);
        Files.createFile(path);
        files.push(path);
        return path;
    }

    private void writeToFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }

    @Test
    void run_noFilesWithoutFlag_readsFromInputAndDisplaysAdjacentLines() throws UniqException {
        String[] args = {};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(WO_FLAG_OUT, outputStream.toString());
    }

    @Test
    void run_noFilesWithCountFlag_readsFromInputAndDisplaysCountOfAdjacentLines() throws UniqException {
        String[] args = {"-c"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(COUNT_FLAG_OUT, outputStream.toString());
    }

    @Test
    void run_noFilesWithDuplicateFlag_readsFromInputAndDisplaysRepeatedAdjacentLinesOnlyOnce() throws UniqException {
        String[] args = {"-d"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(DUP_FLAG_OUT, outputStream.toString());
    }

    @Test
    void run_noFilesWithAllDuplicateFlag_readsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() throws UniqException {
        String[] args = {"-D"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(ALL_DUP_FLAG_OUT, outputStream.toString());
    }

    @Test
    void run_noFilesWithDuplicateAndAllDuplicateFlags_readsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() throws UniqException {
        String[] args = {"-d", "-D"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(ALL_DUP_FLAG_OUT, outputStream.toString());
    }

    @Test
    void run_noFilesWithCountAndDuplicateFlags_readsFromInputAndDisplaysCountOfRepeatedAdjacentLinesOnlyOnce() throws UniqException {
        String[] args = {"-c", "-d"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(BOTH_FLAGS_OUT, outputStream.toString());
    }

    @Test
    void run_noFilesWithUnknownFlag_throws() {
        String[] args = {"-x"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(Exception.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_nonemptyInputFile_readsFileAndDisplaysAdjacentLines() throws IOException, UniqException {
        Path inputPath = createFile(INPUT_FILE_TXT);
        writeToFile(inputPath, TEST_INPUT);
        String[] args = {INPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        try (MockedStatic<Environment> mEnv = mockStatic(Environment.class)) {
            mEnv.when(Environment::getCurrentDirectory).thenReturn(currPath.toString());
            uniqApplication.run(args, stdin, outputStream);
        }
        assertEquals(WO_FLAG_OUT, outputStream.toString());
    }

    @Test
    void run_emptyInputFile_readsFileAndDisplaysNewline() throws Exception {
        createFile(INPUT_FILE_TXT);
        String[] args = {INPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        try (MockedStatic<Environment> mEnv = mockStatic(Environment.class)) {
            mEnv.when(Environment::getCurrentDirectory).thenReturn(currPath.toString());
            uniqApplication.run(args, stdin, outputStream);
        }
        assertEquals(STRING_NEWLINE, outputStream.toString());
    }

    @Test
    void run_nonexistentInputFile_throws() {
        String[] args = {"nonexistent_file.txt"};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(Exception.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_inputFileToOutputFile_displaysNewlineAndOverwritesOutputFile() throws Exception {
        Path inputPath = createFile(INPUT_FILE_TXT);
        Path outputPath = createFile(OUTPUT_FILE_TXT);
        writeToFile(inputPath, TEST_INPUT);
        writeToFile(outputPath, "This is the output file.");
        files.push(outputPath);
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        try (MockedStatic<Environment> mEnv = mockStatic(Environment.class)) {
            mEnv.when(Environment::getCurrentDirectory).thenReturn(currPath.toString());
            uniqApplication.run(args, stdin, outputStream);
        }
        assertArrayEquals(WO_FLAG_OUT.getBytes(), Files.readAllBytes(outputPath));
    }

    @Test
    void run_inputFileToNonexistentOutputFile_displaysNewlineAndCreatesOutputFile() throws Exception {
        Path inputPath = createFile(INPUT_FILE_TXT);
        writeToFile(inputPath, TEST_INPUT);
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        try (MockedStatic<Environment> mEnv = mockStatic(Environment.class)) {
            mEnv.when(Environment::getCurrentDirectory).thenReturn(currPath.toString());
            uniqApplication.run(args, stdin, outputStream);
        }
        Path outputPath = currPath.resolve(OUTPUT_FILE_TXT);
        files.push(outputPath);
        assertTrue(Files.exists(outputPath));
        assertArrayEquals(WO_FLAG_OUT.getBytes(), Files.readAllBytes(outputPath));
    }

    @Test
    void run_nonexistentInputFileToOutputFile_throws() throws IOException {
        Path outputPath = createFile(OUTPUT_FILE_TXT);
        writeToFile(outputPath, "This is the output file.");
        files.push(outputPath);
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        try (MockedStatic<Environment> mEnv = mockStatic(Environment.class)) {
            mEnv.when(Environment::getCurrentDirectory).thenReturn(currPath.toString());
            assertThrows(Exception.class, () -> uniqApplication.run(args, stdin, outputStream));
        }
    }

    @Test
    void run_nonexistentInputFileToNonexistentOutputFile_throws() {
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        try (MockedStatic<Environment> mEnv = mockStatic(Environment.class)) {
            mEnv.when(Environment::getCurrentDirectory).thenReturn(currPath.toString());
            assertThrows(Exception.class, () -> uniqApplication.run(args, stdin, outputStream));
        }
    }
}
