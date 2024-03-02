package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CutApplicationPublicTest {
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
    void cutFromStdin_nullContent_throwsException() {
        int[] ranges = new int[]{1, 2};
        assertThrows(Exception.class, () -> cutApplication.cutFromStdin(false, true, List.of(ranges), null));
    }

    @Test
    void cutFromStdin_singleLineByCharRange_returnCutByLine() throws Exception {
        int[] ranges = new int[]{1, 3};
        InputStream stdin = generateInputStreamFromStrings("hello world");
        String result = cutApplication.cutFromStdin(true, false, List.of(ranges), stdin);
        assertEquals("hel" + STRING_NEWLINE, result);
    }

    @Test
    void cutFromStdin_singleLineByByteRange_returnCutByByte() throws Exception {
        int[] ranges = new int[]{1, 3};
        InputStream stdin = generateInputStreamFromStrings("hello world");
        String result = cutApplication.cutFromStdin(false, true, List.of(ranges), stdin);
        assertEquals("hel" + STRING_NEWLINE, result);
    }

    @Test
    void cutFromStdin_multipleLinesByCharRange_returnCutContentAtEachLineByByte() throws Exception {
        int[] ranges = new int[]{1, 3};
        InputStream stdin = generateInputStreamFromStrings("hello", "world");
        String result = cutApplication.cutFromStdin(true, false, List.of(ranges), stdin);
        assertEquals("hel" + STRING_NEWLINE + "wor" + STRING_NEWLINE, result);
    }

    @Test
    void cutFromStdin_multipleLinesByByteRange_returnCutContentAtEachLineByByte() throws Exception {
        int[] ranges = new int[]{1, 3};
        InputStream stdin = generateInputStreamFromStrings("hello", "world");
        String result = cutApplication.cutFromStdin(false, true, List.of(ranges), stdin);
        assertEquals("hel" + STRING_NEWLINE + "wor" + STRING_NEWLINE, result);
    }
}
