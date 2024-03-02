package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class UniqApplicationPublicTest {
    private static final File TEMP = new File("temp-uniq");
    private static final File NONEXISTENT = new File("uniq_nonexistent.txt");
    private static final File FILE_EMPTY = new File("uniq_empty.txt");
    private static final File OUTPUT = new File("output.txt");

    private static final File FILE_NO_ADJ_DUP = new File("uniq_no_duplicates.txt");
    private static final String TEST_NO_ADJ_DUP = String.format("Hello World%1$sAlice%1$sBob%1$sHello World%1$sBob%1$sAlice%1$sCS4218", System.lineSeparator());

    private static final File FILE_ALL_DUP = new File("uniq_all_duplicates.txt");
    private static final String TEST_ALL_DUP = ("CS4218" + System.lineSeparator()).repeat(50);

    private static final File FILE_MIXED_DUP = new File("uniq_interleaved_duplicates.txt");
    private static final String TEST_MIXED_DUP = ("CS4218" + System.lineSeparator()).repeat(10)
            + "CS1101S" + System.lineSeparator()
            + ("CS4218" + System.lineSeparator()).repeat(3)
            + ("CS4218" + System.lineSeparator()).repeat(3)
            + ("CS1101S" + System.lineSeparator()).repeat(20)
            + ("CS4218" + System.lineSeparator()).repeat(2);

    private static UniqApplication uniqApplication;

    @BeforeAll
    static void setUpBeforeAll() throws IOException {
        writeToFileWithText(FILE_EMPTY, null);
        writeToFileWithText(FILE_NO_ADJ_DUP, TEST_NO_ADJ_DUP);
        writeToFileWithText(FILE_ALL_DUP, TEST_ALL_DUP);
        writeToFileWithText(FILE_MIXED_DUP, TEST_MIXED_DUP);

        TEMP.mkdirs();
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
        FILE_NO_ADJ_DUP.delete();
        FILE_ALL_DUP.delete();
        FILE_MIXED_DUP.delete();

        TEMP.delete();
        OUTPUT.delete();
    }

    @BeforeEach
    void setUp() {
        uniqApplication = new UniqApplication();
    }

    @Test
    void uniqFromFile_emptyFile_returnsEmpty() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, false, false, FILE_EMPTY.toString(), null);
            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromFile_fileNoDuplicatesNoArguments_equalToItself() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, false, false, FILE_NO_ADJ_DUP.toString(), null);
            assertEquals(TEST_NO_ADJ_DUP + System.lineSeparator(), result);
        });
    }

    @Test
    void uniqFromFile_fileNoDuplicatesCountOnly_allOneCounts() {
        assertDoesNotThrow(() -> {
            String expected = "\t1 Hello World" + System.lineSeparator() + "\t1 Alice" + System.lineSeparator() + "\t1 Bob"
                    + System.lineSeparator() + "\t1 Hello World" + System.lineSeparator() + "\t1 Bob"
                    + System.lineSeparator() + "\t1 Alice" + System.lineSeparator() + "\t1 CS4218" + System.lineSeparator();
            String result = uniqApplication.uniqFromFile(true, false, false, FILE_NO_ADJ_DUP.toString(), null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_fileNoDuplicatesRepeatedOnly_returnsEmpty() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, true, false, FILE_NO_ADJ_DUP.toString(), null);
            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromFile_fileNoDuplicatesAllRepeatedOnly_returnsEmpty() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, false, true, FILE_NO_ADJ_DUP.toString(), null);
            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromFile_fileAllDuplicatesNoArguments_onlyOneResult() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218" + System.lineSeparator();
            String result = uniqApplication.uniqFromFile(false, false, false, FILE_ALL_DUP.toString(), null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_fileAllDuplicatesCountOnly_returnsCount() {
        assertDoesNotThrow(() -> {
            String expected = "\t50 CS4218" + System.lineSeparator();
            String result = uniqApplication.uniqFromFile(true, false, false, FILE_ALL_DUP.toString(), null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_fileAllDuplicatesRepeatedOnly_onlyOneResult() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218" + System.lineSeparator();
            String result = uniqApplication.uniqFromFile(false, true, false, FILE_ALL_DUP.toString(), null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_fileAllDuplicatesAllRepeatedOnly_returnsItself() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, false, true, FILE_ALL_DUP.toString(), null);
            assertEquals(TEST_ALL_DUP, result);
        });
    }

    @Test
    void uniqFromFile_fileAllDuplicatesCountAndRepeatedOnly_returnsCount() {
        assertDoesNotThrow(() -> {
            String expected = "\t50 CS4218" + System.lineSeparator();
            String result = uniqApplication.uniqFromFile(true, true, false, FILE_ALL_DUP.toString(), null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_fileAllDuplicatesCountAndAllRepeatedOnly_returnsCountRepeated() {
        assertDoesNotThrow(() -> {
            String expected = ("\t50 CS4218" + System.lineSeparator()).repeat(50);
            String result = uniqApplication.uniqFromFile(true, false, true, FILE_ALL_DUP.toString(), null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_fileAllDuplicatesRepeatedAndAllRepeatedOnly_returnsItself() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, true, true, FILE_ALL_DUP.toString(), null);
            assertEquals(TEST_ALL_DUP, result);
        });
    }

    @Test
    void uniqFromFile_fileAllDuplicatesAllArguments_returnsItself() {
        assertDoesNotThrow(() -> {
            String expected = ("\t50 CS4218" + System.lineSeparator()).repeat(50);
            String result = uniqApplication.uniqFromFile(true, true, true, FILE_ALL_DUP.toString(), null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_fileInterleavedDuplicatesNoArguments_success() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218" + System.lineSeparator() + "CS1101S" + System.lineSeparator()
                    + "CS4218" + System.lineSeparator() + "CS1101S" + System.lineSeparator() + "CS4218" + System.lineSeparator();
            String result = uniqApplication.uniqFromFile(false, false, false, FILE_MIXED_DUP.toString(), null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_fileInterleavedDuplicatesCountOnly_success() {
        assertDoesNotThrow(() -> {
            String expected = "\t10 CS4218" + System.lineSeparator() + "\t1 CS1101S" + System.lineSeparator() + "\t6 CS4218"
                    + System.lineSeparator() + "\t20 CS1101S" + System.lineSeparator() + "\t2 CS4218" + System.lineSeparator();
            String result = uniqApplication.uniqFromFile(true, false, false, FILE_MIXED_DUP.toString(), null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_fileInterleavedDuplicatesRepeatedOnly_success() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218" + System.lineSeparator() + "CS4218" + System.lineSeparator() + "CS1101S"
                    + System.lineSeparator() + "CS4218" + System.lineSeparator();
            String result = uniqApplication.uniqFromFile(false, true, false, FILE_MIXED_DUP.toString(), null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_fileInterleavedDuplicatesAllRepeatedOnly_success() {
        assertDoesNotThrow(() -> {
            String expected = ("CS4218" + System.lineSeparator()).repeat(16) + ("CS1101S" + System.lineSeparator()).repeat(20)
                    + ("CS4218" + System.lineSeparator()).repeat(2);
            String result = uniqApplication.uniqFromFile(false, false, true, FILE_MIXED_DUP.toString(), null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_nonExistentFile_throws() {
        assertThrows(Exception.class, () -> uniqApplication.uniqFromFile(true, true, true,
                NONEXISTENT.toString(), null));
    }

    @Test
    void uniqFromFile_directory_throws() {
        assertThrows(Exception.class, () -> uniqApplication.uniqFromFile(true, true, true,
                TEMP.toString(), null));
    }

    @Test
    void uniqFromStdIn_nullStream_throwsException() {
        assertThrows(Exception.class, () ->
                uniqApplication.uniqFromStdin(false, false, false, null, null));

    }

    @Test
    void uniqFromStdIn_emptyFile_returnsEmpty() {
        assertDoesNotThrow(() -> {
            InputStream stream = new ByteArrayInputStream("".getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, null);
            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromStdIn_noAdjacentDuplicates_success() {
        assertDoesNotThrow(() -> {
            InputStream stream = new ByteArrayInputStream(TEST_NO_ADJ_DUP.getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, null);
            assertEquals(TEST_NO_ADJ_DUP + TestStringUtils.STRING_NEWLINE, result);
        });
    }

    @Test
    void uniqFromStdIn_allDuplicates_success() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218" + System.lineSeparator();
            InputStream stream = new ByteArrayInputStream(TEST_ALL_DUP.getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, null);
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromStdIn_interleavedDuplicates_success() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218" + System.lineSeparator() + "CS1101S" + System.lineSeparator() + "CS4218"
                    + System.lineSeparator() + "CS1101S" + System.lineSeparator() + "CS4218" + System.lineSeparator();
            InputStream stream = new ByteArrayInputStream(TEST_MIXED_DUP.getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, null);
            assertEquals(expected, result);
        });
    }

}
