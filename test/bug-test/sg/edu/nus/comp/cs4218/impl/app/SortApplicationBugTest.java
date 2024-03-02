package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class SortApplicationBugTest {
    private SortApplication sortApplication;
    private static final String NUMBER_FLAG = "-n";

    @BeforeEach
    public void init() {
        sortApplication = new SortApplication();
    }

    private String joinStringsByLineSeparator(String... strs) {
        return String.join(TestStringUtils.STRING_NEWLINE, strs);
    }

    private InputStream generateInputStreamFromStrings(String... strs) {
        return new ByteArrayInputStream(joinStringsByLineSeparator(strs).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void run_inputWithNegativeNumbersWithoutNumberFlag_writesToStdoutSuccessfully() throws AbstractApplicationException {
        InputStream stdin = generateInputStreamFromStrings("-100", "-10", "-2", "-2456", "-352637", "-helloworld", "hello!", "hello1235", "123526", "-1000", "-4006");
        String expected = joinStringsByLineSeparator("-10", "-100", "-1000", "-2" , "-2456", "-352637", "-4006", "-helloworld", "123526", "hello!", "hello1235") + STRING_NEWLINE;
        String[] argList = new String[]{};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sortApplication.run(argList, stdin, output);
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void run_inputWithNegativeNumbersWithNumberFlag_writesToStdoutSuccessfully() throws AbstractApplicationException {
        InputStream stdin = generateInputStreamFromStrings("-100", "-10", "-2", "-2456", "-352637", "-helloworld", "hello!", "hello1235", "123526", "-1000", "-4006");
        String expected = joinStringsByLineSeparator("-helloworld", "-352637", "-4006", "-2456", "-1000", "-100", "-10", "-2", "123526", "hello1235", "hello!") + STRING_NEWLINE;
        String[] argList = new String[]{NUMBER_FLAG};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sortApplication.run(argList, stdin, output);
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }
}