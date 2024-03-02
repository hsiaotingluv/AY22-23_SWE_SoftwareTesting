package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;

class SequenceCommandDiffBlueTest {
    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link SequenceCommand#SequenceCommand(List)}
     *   <li>{@link SequenceCommand#terminate()}
     *   <li>{@link SequenceCommand#getCommands()}
     * </ul>
     */
    @Test
    void testConstructor() {
        ArrayList<Command> commandList = new ArrayList<>();
        SequenceCommand actualSequenceCommand = new SequenceCommand(commandList);
        actualSequenceCommand.terminate();
        assertSame(commandList, actualSequenceCommand.getCommands());
    }

    /**
     * Method under test: {@link SequenceCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        SequenceCommand sequenceCommand = new SequenceCommand(new ArrayList<>());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        sequenceCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link SequenceCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate2()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        ArrayList<Command> commandList = new ArrayList<>();
        commandList.add(new PipeCommand(new ArrayList<>()));
        SequenceCommand sequenceCommand = new SequenceCommand(commandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        sequenceCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link SequenceCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate3() throws FileNotFoundException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        SequenceCommand sequenceCommand = new SequenceCommand(new ArrayList<>());
        DataInputStream stdin = mock(DataInputStream.class);
        sequenceCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link SequenceCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate4()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        ArrayList<String> argsList = new ArrayList<>();
        callCommandList.add(new CallCommand(argsList, new ApplicationRunner()));
        PipeCommand e = new PipeCommand(callCommandList);

        ArrayList<Command> commandList = new ArrayList<>();
        commandList.add(e);
        SequenceCommand sequenceCommand = new SequenceCommand(commandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        sequenceCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link SequenceCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate5()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand.evaluate(SequenceCommand.java:39)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<Command> commandList = new ArrayList<>();
        commandList.add(null);
        SequenceCommand sequenceCommand = new SequenceCommand(commandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        sequenceCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link SequenceCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate6()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        ArrayList<Command> commandList = new ArrayList<>();
        commandList.add(new SequenceCommand(new ArrayList<>()));
        SequenceCommand sequenceCommand = new SequenceCommand(commandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        sequenceCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link SequenceCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate7()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ExitException: exit: An error occurred
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:36)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand.evaluate(SequenceCommand.java:39)
        //   See https://diff.blue/R013 to resolve this issue.

        CallCommand callCommand = mock(CallCommand.class);
        doThrow(new ExitException("An error occurred")).when(callCommand)
                .evaluate((InputStream) any(), (OutputStream) any());

        ArrayList<Command> commandList = new ArrayList<>();
        commandList.add(callCommand);
        SequenceCommand sequenceCommand = new SequenceCommand(commandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        sequenceCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link SequenceCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate8()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        CallCommand callCommand = mock(CallCommand.class);
        doThrow(new CatException("An error occurred")).when(callCommand)
                .evaluate((InputStream) any(), (OutputStream) any());

        ArrayList<Command> commandList = new ArrayList<>();
        commandList.add(callCommand);
        SequenceCommand sequenceCommand = new SequenceCommand(commandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        sequenceCommand.evaluate(stdin, new ByteArrayOutputStream());
        verify(callCommand).evaluate((InputStream) any(), (OutputStream) any());
    }

    /**
     * Method under test: {@link SequenceCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate9()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand.evaluate(SequenceCommand.java:55)
        //   See https://diff.blue/R013 to resolve this issue.

        CallCommand callCommand = mock(CallCommand.class);
        doThrow(new CatException("An error occurred")).when(callCommand)
                .evaluate((InputStream) any(), (OutputStream) any());

        ArrayList<Command> commandList = new ArrayList<>();
        commandList.add(callCommand);
        SequenceCommand sequenceCommand = new SequenceCommand(commandList);
        sequenceCommand.evaluate(new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8")), null);
    }
}

