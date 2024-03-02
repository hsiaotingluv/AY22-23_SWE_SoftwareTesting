package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
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
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;

class PipeCommandDiffBlueTest {
    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link PipeCommand#PipeCommand(List)}
     *   <li>{@link PipeCommand#terminate()}
     *   <li>{@link PipeCommand#getCallCommands()}
     * </ul>
     */
    @Test
    void testConstructor() {
        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        PipeCommand actualPipeCommand = new PipeCommand(callCommandList);
        actualPipeCommand.terminate();
        assertSame(callCommandList, actualPipeCommand.getCallCommands());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        PipeCommand pipeCommand = new PipeCommand(new ArrayList<>());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate2()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: Invalid syntax
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:37)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        ArrayList<String> argsList = new ArrayList<>();
        callCommandList.add(new CallCommand(argsList, new ApplicationRunner()));
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate3()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: Invalid syntax
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:37)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        ArrayList<String> argsList = new ArrayList<>();
        callCommandList.add(new CallCommand(argsList, new ApplicationRunner()));
        ArrayList<String> argsList1 = new ArrayList<>();
        callCommandList.add(new CallCommand(argsList1, new ApplicationRunner()));
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate4() throws FileNotFoundException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        PipeCommand pipeCommand = new PipeCommand(new ArrayList<>());
        DataInputStream stdin = mock(DataInputStream.class);
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate5()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: Invalid syntax: Invalid app
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.getApplication(ApplicationRunner.java:83)
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.runApp(ApplicationRunner.java:46)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:50)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("Invalid syntax");
        CallCommand e = new CallCommand(stringList, new ApplicationRunner());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        callCommandList.add(e);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate6()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: No such file or directory
        //       at sg.edu.nus.comp.cs4218.impl.util.IOUtils.openInputStream(IOUtils.java:36)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:67)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:41)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   java.io.FileNotFoundException: C:\Users\lkl74\Downloads\nus\CS4218\cs4218-project-2023-team27\Invalid syntax (The system cannot find the file specified)
        //       at java.io.FileInputStream.open0(Native Method)
        //       at java.io.FileInputStream.open(FileInputStream.java:219)
        //       at java.io.FileInputStream.<init>(FileInputStream.java:157)
        //       at java.io.FileInputStream.<init>(FileInputStream.java:112)
        //       at sg.edu.nus.comp.cs4218.impl.util.IOUtils.openInputStream(IOUtils.java:34)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:67)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:41)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("<");
        stringList.add("Invalid syntax");
        CallCommand e = new CallCommand(stringList, new ApplicationRunner());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        callCommandList.add(e);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate7()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.util.NoSuchElementException
        //       at java.util.ArrayList$Itr.next(ArrayList.java:1000)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:50)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:41)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("<");
        CallCommand e = new CallCommand(stringList, new ApplicationRunner());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        callCommandList.add(e);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate8()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.util.NoSuchElementException
        //       at java.util.ArrayList$Itr.next(ArrayList.java:1000)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:50)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:41)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(">");
        CallCommand e = new CallCommand(stringList, new ApplicationRunner());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        callCommandList.add(e);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
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
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.isRedirOperator(IORedirectionHandler.java:88)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:44)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:41)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(null);
        CallCommand e = new CallCommand(stringList, new ApplicationRunner());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        callCommandList.add(e);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate10()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("");
        CallCommand e = new CallCommand(stringList, new ApplicationRunner());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        callCommandList.add(e);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate11()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: Invalid Filename
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:53)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:41)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(">");
        stringList.add("<");
        stringList.add("Invalid syntax");
        CallCommand e = new CallCommand(stringList, new ApplicationRunner());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        callCommandList.add(e);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate12()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R011 Sandboxing policy violation.
        //   Diffblue Cover ran code in your project that tried
        //     to access files outside the temporary directory (file 'C:\Users\lkl74\Downloads\nus\CS4218\cs4218-project-2023-team27\Invalid syntax', permission 'write').
        //   Diffblue Cover's default sandboxing policy disallows this in order to prevent
        //   your code from damaging your system environment.
        //   See https://diff.blue/R011 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(">");
        stringList.add("Invalid syntax");
        CallCommand e = new CallCommand(stringList, new ApplicationRunner());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        callCommandList.add(e);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate13()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        CallCommand callCommand = mock(CallCommand.class);
        doNothing().when(callCommand).evaluate((InputStream) any(), (OutputStream) any());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        callCommandList.add(callCommand);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
        verify(callCommand).evaluate((InputStream) any(), (OutputStream) any());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate14()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: Invalid syntax
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:37)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   See https://diff.blue/R013 to resolve this issue.

        CallCommand callCommand = mock(CallCommand.class);
        doNothing().when(callCommand).terminate();
        doNothing().when(callCommand).evaluate((InputStream) any(), (OutputStream) any());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        ArrayList<String> argsList = new ArrayList<>();
        callCommandList.add(new CallCommand(argsList, new ApplicationRunner()));
        callCommandList.add(callCommand);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate15()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.CatException: cat: An error occurred
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:36)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   See https://diff.blue/R013 to resolve this issue.

        CallCommand callCommand = mock(CallCommand.class);
        doNothing().when(callCommand).terminate();
        doNothing().when(callCommand).evaluate((InputStream) any(), (OutputStream) any());
        CallCommand callCommand1 = mock(CallCommand.class);
        doThrow(new CatException("An error occurred")).when(callCommand1)
                .evaluate((InputStream) any(), (OutputStream) any());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        callCommandList.add(callCommand1);
        callCommandList.add(callCommand);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link PipeCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate16()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: An error occurred
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:36)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand.evaluate(PipeCommand.java:49)
        //   See https://diff.blue/R013 to resolve this issue.

        CallCommand callCommand = mock(CallCommand.class);
        doNothing().when(callCommand).terminate();
        doNothing().when(callCommand).evaluate((InputStream) any(), (OutputStream) any());
        CallCommand callCommand1 = mock(CallCommand.class);
        doThrow(new ShellException("An error occurred")).when(callCommand1)
                .evaluate((InputStream) any(), (OutputStream) any());

        ArrayList<CallCommand> callCommandList = new ArrayList<>();
        callCommandList.add(callCommand1);
        callCommandList.add(callCommand);
        PipeCommand pipeCommand = new PipeCommand(callCommandList);
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        pipeCommand.evaluate(stdin, new ByteArrayOutputStream());
    }
}

