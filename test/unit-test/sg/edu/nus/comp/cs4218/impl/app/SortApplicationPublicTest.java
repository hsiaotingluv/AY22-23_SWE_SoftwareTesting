package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class SortApplicationPublicTest {
    private static final String TEMP = "temp-sort";
    private static final Path TEMP_PATH = Paths.get(TEMP);
    private static final String TEST_FILE = "file.txt";
    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static SortApplication sortApplication;
    private static String initialDir;

    private String joinStringsByLineSeparator(String... strs) {
        return String.join(TestStringUtils.STRING_NEWLINE, strs);
    }

    private InputStream generateInputStreamFromStrings(String... strs) {
        return new ByteArrayInputStream(joinStringsByLineSeparator(strs).getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException, IOException {
        sortApplication = new SortApplication();
        initialDir = TestEnvironmentUtil.getCurrentDirectory();
        Files.createDirectory(TEMP_PATH);
        TestEnvironmentUtil.setCurrentDirectory(TEMP_PATH.toString());
    }

    @AfterEach
    void tearDown() throws IOException, NoSuchFieldException, IllegalAccessException {
        Files.walk(TEMP_PATH)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        TestEnvironmentUtil.setCurrentDirectory(initialDir);
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
    }

    private void createFile(String content) throws IOException {
        Path path = TEMP_PATH.resolve(SortApplicationPublicTest.TEST_FILE);
        Files.createFile(path);
        Files.write(path, content.getBytes());
        FILES.push(path);
    }

    @Test
    void sortFromStdin_noFlags_returnsSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("a", "c", "b");
        String expected = joinStringsByLineSeparator("a", "b", "c") + STRING_NEWLINE;
        assertEquals(expected, sortApplication.sortFromStdin(false, false, false, stdin));
    }

    @Test
    void sortFromStdin_isFirstWordNumber_returnsSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("10 b", "5 c", "1 a");
        String expected = joinStringsByLineSeparator("1 a", "5 c", "10 b") + STRING_NEWLINE;
        assertEquals(expected, sortApplication.sortFromStdin(true, false, false, stdin));
    }

    @Test
    void sortFromStdin_reverseOrder_reverseSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("a", "c", "b");
        String expected = joinStringsByLineSeparator("c", "b", "a") + STRING_NEWLINE;
        assertEquals(expected, sortApplication.sortFromStdin(false, true, false, stdin));
    }

    @Test
    void sortFromStdin_caseIndependent_caseIndependentSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("A", "C", "b");
        String expected = joinStringsByLineSeparator("A", "b", "C") + STRING_NEWLINE;
        assertEquals(expected, sortApplication.sortFromStdin(false, false, true, stdin));
    }

    // File

    @Test
    void sortFromFiles_noFlags_returnsSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("a", "c", "b"));
        String expected = joinStringsByLineSeparator("a", "b", "c") + STRING_NEWLINE;
        assertEquals(expected, sortApplication.sortFromFiles(false, false,
                false, TEST_FILE));
    }

    @Test
    void sortFromFiles_isFirstWordNumber_returnsSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("10 b", "5 c", "1 a"));
        String expected = joinStringsByLineSeparator("1 a", "5 c", "10 b") + STRING_NEWLINE;
        assertEquals(expected, sortApplication.sortFromFiles(true, false,
                false, TEST_FILE));
    }

    @Test
    void sortFromFiles_reverseOrder_reverseSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("a", "c", "b"));
        String expected = joinStringsByLineSeparator("c", "b", "a") + STRING_NEWLINE;
        assertEquals(expected, sortApplication.sortFromFiles(false, true,
                false, TEST_FILE));
    }

    @Test
    void sortFromFiles_caseIndependent_caseIndependentSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("A", "C", "b"));
        String expected = joinStringsByLineSeparator("A", "b", "C") + STRING_NEWLINE;
        assertEquals(expected, sortApplication.sortFromFiles(false, false,
                true, TEST_FILE));
    }
}