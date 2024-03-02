package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CutApplicationPublicIntegrationTest {
    public static final String CHAR_FLAG = "-c";
    public static final String BYTE_FLAG = "-b";
    public static final String TEST_RANGE = "1-3";
    CutApplication cutApplication;

    private String joinStringsByLineSeparator(String... strs) {
        return String.join(STRING_NEWLINE, strs);
    }

    private InputStream generateInputStreamFromStrings(String... strs) {
        return new ByteArrayInputStream(joinStringsByLineSeparator(strs).getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    public void setUp() {
        cutApplication = new CutApplication();
    }


    @Test
    void run_singleLineByCharRange_returnCutByLine() throws Exception {
        String[] argList = new String[]{CHAR_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings("hello world");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals("hel" + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void cutFromStdin_singleLineByByteRange_returnCutByByte() throws Exception {
        String[] argList = new String[]{BYTE_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings("hello world");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals("hel" + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void cutFromStdin_multipleLinesByCharRange_returnCutContentAtEachLineByByte() throws Exception {
        String[] argList = new String[]{CHAR_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings("hello", "world");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals("hel" + STRING_NEWLINE + "wor" + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void cutFromStdin_multipleLinesByByteRange_returnCutContentAtEachLineByByte() throws Exception {
        String[] argList = new String[]{BYTE_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings("hello", "world");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals("hel" + STRING_NEWLINE + "wor" + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }
}
