package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.EchoException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


public class EchoApplicationTest {

    private static final String STRING_1 = "string1";
    private static final String STRING_2 = "string2";
    private static final String SPACE_2 = "This is space: .";
    private static final String SPACE_9 = "'\"This is space `echo \" \"`\"'";
    private EchoApplication echoApplication;

    @BeforeEach
    void setUp() {
        this.echoApplication = new EchoApplication();
    }

    @Test
    void run_withOutputStreamWithIOException_throwIOEchoException() throws Exception {

        EchoApplication echoAppSpy = spy(echoApplication);
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int bytes) throws IOException {
                throw new IOException();
            }
        };

        String[] input = new String[]{STRING_1};

        doReturn(STRING_1).when(echoAppSpy).constructResult(input);

        Throwable exception = assertThrows(EchoException.class, () -> {
            echoAppSpy.run(input, System.in, outputStream);
        });
        assertEquals(new EchoException(E_IO_EXCEPTION).getMessage(), exception.getMessage());
    }

    @Test
    void run_withValidInputs_doesNotThrowException() throws Exception {
        EchoApplication echoAppSpy = spy(echoApplication);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        String[] input = new String[]{STRING_1};

        doReturn(STRING_1).when(echoAppSpy).constructResult(input);

        assertDoesNotThrow(() -> {
            echoAppSpy.run(input, System.in, outputStream);
        });
        assertEquals(STRING_1, outputStream.toString().trim());

    }


    @Test
    void constructResult_withNullArgument_throwNullArgumentEchoException() {
        Throwable exception = assertThrows(EchoException.class, () -> {
            this.echoApplication.constructResult(null);
        });
        assertEquals(new EchoException(E_NULL_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void constructResult_withEmptyArgument_returnNewLineString() {
        assertDoesNotThrow(() -> {
            String constructedResult = this.echoApplication.constructResult(new String[0]);
            assertEquals(STRING_NEWLINE, constructedResult);
        });
    }

    @Test
    void constructResult_withOneString_returnNewLine() {
        EchoApplication echoAppSpy = spy(echoApplication);
        String[] inp = {STRING_1};

        String actual = assertDoesNotThrow(
                () -> echoAppSpy.constructResult(inp));

        assertEquals(STRING_1 + STRING_NEWLINE, actual);
    }

    @Test
    void constructResult_withTwoStrings_returnTwoStringsJoined() {
        EchoApplication echoAppSpy = spy(echoApplication);
        String[] inp = {STRING_1, STRING_2};
        String actual = assertDoesNotThrow(
                () -> echoAppSpy.constructResult(inp));

        assertEquals(STRING_1 + " " + STRING_2 + STRING_NEWLINE, actual);
    }

    @Test
    void constructResult_withNonEmptyStringEmptyString_returnNonEmptyString() {
        EchoApplication echoAppSpy = spy(echoApplication);
        String[] inp = {STRING_1, ""};

        String actual = assertDoesNotThrow(
                () -> echoAppSpy.constructResult(inp));

        assertEquals("string1" + " " + STRING_NEWLINE, actual);
    }

    @Test
    void constructResult_withOneNonBlankStringOneBlankStringInArgument_returnStringsJoinedBySpacesWithNewLineAtEnd() {
        EchoApplication echoAppSpy = spy(echoApplication);
        String[] inp = {STRING_1, " "};

        String actual = assertDoesNotThrow(
                () -> echoAppSpy.constructResult(inp));

        assertEquals(STRING_1 + " " + " " + STRING_NEWLINE, actual);
    }


    @Test
    void run_withNullArgument_throwNullArgumentEchoException() {
        Throwable exception = assertThrows(EchoException.class, () -> {
            this.echoApplication.run(null, System.in, System.out);
        });
        assertEquals(new EchoException(E_NULL_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void run_withNullOutputStream_throwNoOutputStreamEchoException() {
        Throwable exception = assertThrows(EchoException.class, () -> {
            this.echoApplication.run(new String[0], System.in, null);
        });
        assertEquals(new EchoException(E_NO_OSTREAM).getMessage(), exception.getMessage());
    }

    @Test
    void run_withOutputStreamWithIOException_throwIOExceptionEchoException() {
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int bytes) throws IOException {
                throw new IOException();
            }
        };
        Throwable exception = assertThrows(EchoException.class, () -> {
            this.echoApplication.run(new String[0], System.in, outputStream);
        });
        assertEquals(new EchoException(E_IO_EXCEPTION).getMessage(), exception.getMessage());
    }

    @Test
    void run_withNullInputStreamAndOneStringArgument_returnStringWithNewLine() {
        EchoApplication echoAppSpy = spy(echoApplication);
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] inp = {STRING_1};


        assertDoesNotThrow(
                () -> echoAppSpy.run(inp, null, outputStream));

        assertEquals(STRING_1 + STRING_NEWLINE, outputStream.toString());

    }

    @Test
    void run_withEmptyArgument_returnNewLineString() {
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            this.echoApplication.run(new String[0], System.in, outputStream);
            assertEquals(STRING_NEWLINE, outputStream.toString());

        });
    }

    @Test
    void run_withOneStringArgument_returnStringWithNewLine() {
        EchoApplication echoAppSpy = spy(echoApplication);
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] inp = {STRING_1};

        assertDoesNotThrow(
                () -> echoAppSpy.run(inp, System.in, outputStream));


        assertEquals(STRING_1 + STRING_NEWLINE, outputStream.toString());

    }

    @Test
    void run_withTwoStringsInArgument_returnStringWithNewLine() {
        EchoApplication echoAppSpy = spy(echoApplication);
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] inp = {STRING_1, STRING_2};

        assertDoesNotThrow(
                () -> echoAppSpy.run(inp, System.in, outputStream));


        assertEquals(STRING_1 + " " + STRING_2 + STRING_NEWLINE, outputStream.toString());

    }


    @Test
    void run_withDoubleAndBackQuotesString_returnStringsJoinedBySpacesWithNewLineAtEndNotIgnoringBackQuote() {
        EchoApplication echoAppSpy = spy(echoApplication);
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] inp = {SPACE_2};

        assertDoesNotThrow(
                () -> echoAppSpy.run(inp, System.in, outputStream));

        assertEquals(SPACE_2 + STRING_NEWLINE, outputStream.toString());

    }


    @Test
    void run_withSingleDoubleBackQuotesString_returnStringsJoinedBySpacesWithNewLineAtEndIgnoringDoubleAndBackQuote() {
        EchoApplication echoAppSpy = spy(echoApplication);
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] inp = {SPACE_9};


        assertDoesNotThrow(
                () -> echoAppSpy.run(inp, System.in, outputStream));


        assertEquals(SPACE_9 + STRING_NEWLINE, outputStream.toString());
    }
}

