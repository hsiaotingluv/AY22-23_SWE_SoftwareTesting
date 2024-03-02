package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.EchoException;

class EchoApplicationDiffBlueTest {
    /**
     * Method under test: {@link EchoApplication#constructResult(String[])}
     */
    @Test
    void testConstructResult() throws EchoException {
        assertEquals("Args\r\n", (new EchoApplication()).constructResult(new String[]{"Args"}));
        assertThrows(EchoException.class, () -> (new EchoApplication()).constructResult(null));
        assertEquals("\r\n", (new EchoApplication()).constructResult(new String[]{}));
    }

    /**
     * Method under test: {@link EchoApplication#run(String[], InputStream, OutputStream)}
     */
    @Test
    void testRun() throws UnsupportedEncodingException, EchoException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        EchoApplication echoApplication = new EchoApplication();
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        echoApplication.run(new String[]{"Args"}, stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link EchoApplication#run(String[], InputStream, OutputStream)}
     */
    @Test
    void testRun2() throws UnsupportedEncodingException, EchoException {
        EchoApplication echoApplication = new EchoApplication();
        assertThrows(EchoException.class,
                () -> echoApplication.run(null, new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8")), null));
    }

    /**
     * Method under test: {@link EchoApplication#run(String[], InputStream, OutputStream)}
     */
    @Test
    void testRun3() throws UnsupportedEncodingException, EchoException {
        EchoApplication echoApplication = new EchoApplication();
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        assertThrows(EchoException.class, () -> echoApplication.run(null, stdin, new ByteArrayOutputStream()));
    }

    /**
     * Method under test: {@link EchoApplication#run(String[], InputStream, OutputStream)}
     */
    @Test
    void testRun4() throws UnsupportedEncodingException, EchoException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        EchoApplication echoApplication = new EchoApplication();
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        echoApplication.run(new String[]{}, stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link EchoApplication#run(String[], InputStream, OutputStream)}
     */
    @Test
    void testRun5() throws EchoException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        EchoApplication echoApplication = new EchoApplication();
        DataInputStream stdin = mock(DataInputStream.class);
        echoApplication.run(new String[]{"Args"}, stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link EchoApplication#run(String[], InputStream, OutputStream)}
     */
    @Test
    void testRun6() throws UnsupportedEncodingException, EchoException {
        EchoApplication echoApplication = new EchoApplication();
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        assertThrows(EchoException.class,
                () -> echoApplication.run(new String[]{"Args"}, stdin, new PipedOutputStream()));
    }
}

