package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

public class EchoApplicationPublicIntegrationTest {

    private EchoApplication echoApplication;

    @BeforeEach
    void setUp() {
        echoApplication = new EchoApplication();
    }

    @Test
    public void run_singleArgument_outputsArgument() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoApplication.run(new String[]{"A*B*C"}, System.in, output);
        assertArrayEquals(("A*B*C" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    public void run_multipleArgument_spaceSeparated() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoApplication.run(new String[]{"A", "B", "C"}, System.in, output);
        assertArrayEquals(("A B C" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    public void run_multipleArgumentWithSpace_spaceSeparated() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoApplication.run(new String[]{"A B", "C D"}, System.in, output);
        assertArrayEquals(("A B C D" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    public void run_zeroArguments_outputsNewline() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoApplication.run(new String[]{}, System.in, output);
        assertArrayEquals(STRING_NEWLINE.getBytes(), output.toByteArray());
    }
}
