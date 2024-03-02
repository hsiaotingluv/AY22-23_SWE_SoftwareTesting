package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PasteApplicationPublicTest {
    private static final File DIRECTORY = new File("pasteTestDirectory");
    private static final File NONEXISTENT = new File("paste_nonexistent.txt");
    private static final File FILE_EMPTY = new File("paste_empty.txt");

    private static final File FILE_1 = new File("paste_1.txt");
    private static final String TEXT_FILE_1 = "A\nB\nC\nD\nE";

    private static final File FILE_2 = new File("paste_2.txt");
    private static final String TEXT_FILE_2 = "1\n2\n3\n4\n5";
    private static final String E_IS_DIR = String.format("paste: %s: Is a directory", DIRECTORY);
    private static final String E_NO_SUCH_FILE = String.format("paste: %s: No such file or directory", NONEXISTENT);

    private static PasteApplication pasteApplication;

    @BeforeAll
    static void setUpBeforeAll() throws IOException {
        writeToFileWithText(FILE_EMPTY, null);
        writeToFileWithText(FILE_1, TEXT_FILE_1);
        writeToFileWithText(FILE_2, TEXT_FILE_2);

        DIRECTORY.mkdirs();
    }

    public static void writeToFileWithText(File file, String text) throws IOException {
        FileWriter writer = new FileWriter(file);

        if (text == null || text.isBlank()) {
            writer.close();
            return;
        }

        writer.write(text);
        writer.close();
    }

    @AfterAll
    static void tearDownAfterAll() {
        FILE_EMPTY.delete();
        FILE_1.delete();
        FILE_2.delete();

        DIRECTORY.delete();
    }

    private void assertEqualsReplacingNewlines(String expected, String actual) {
        assertEquals(expected.replaceAll("\r\n", "\n"), actual.replaceAll("\r\n", "\n"));
    }

    @BeforeEach
    void setUp() {
        pasteApplication = new PasteApplication();
    }

    @Test
    void mergeFileAndStdin_nullStream_throwsException() {
        assertThrows(Exception.class, () -> pasteApplication.mergeFileAndStdin(true, null));
    }

    @Test
    void mergeFileAndStdin_nullFilename_throwsException() {
        assertThrows(Exception.class, () -> pasteApplication.mergeFileAndStdin(true, System.in, null));
    }

    @Test
    void mergeStdin_nullStream_throwsException() {
        assertThrows(Exception.class, () -> pasteApplication.mergeStdin(true, null));
    }

    @Test
    void mergeStdin_noSerial_returnsItself() throws Exception {
        InputStream stream = new ByteArrayInputStream(TEXT_FILE_1.getBytes());

        String result = pasteApplication.mergeStdin(false, stream);
        assertEqualsReplacingNewlines(TEXT_FILE_1, result);
    }

    @Test
    void mergeFile_nullFilename_throwsException() {
        assertThrows(Exception.class, () -> pasteApplication.mergeFile(true, null));
    }

    @Test
    void mergeFile_noSerialOneFile_returnsItself() throws Exception {
        String result = pasteApplication.mergeFile(false, FILE_1.toString());
        assertEqualsReplacingNewlines(TEXT_FILE_1, result);
    }
}
