package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;

class CallCommandDiffBlueTest {
    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link CallCommand#CallCommand(List, ApplicationRunner)}
     *   <li>{@link CallCommand#terminate()}
     *   <li>{@link CallCommand#getArgsList()}
     * </ul>
     */
    @Test
    void testConstructor() {
        ArrayList<String> stringList = new ArrayList<>();
        CallCommand actualCallCommand = new CallCommand(stringList, new ApplicationRunner());
        actualCallCommand.terminate();
        assertSame(stringList, actualCallCommand.getArgsList());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        ArrayList<String> argsList = new ArrayList<>();
        CallCommand callCommand = new CallCommand(argsList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        assertThrows(ShellException.class, () -> callCommand.evaluate(stdin, new ByteArrayOutputStream()));
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate2()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: Invalid syntax: Invalid app
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.getApplication(ApplicationRunner.java:83)
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.runApp(ApplicationRunner.java:46)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:50)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("Invalid syntax");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate3()
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
        //   java.io.FileNotFoundException: C:\Users\lkl74\Downloads\nus\CS4218\cs4218-project-2023-team27\Invalid syntax (The system cannot find the file specified)
        //       at java.io.FileInputStream.open0(Native Method)
        //       at java.io.FileInputStream.open(FileInputStream.java:219)
        //       at java.io.FileInputStream.<init>(FileInputStream.java:157)
        //       at java.io.FileInputStream.<init>(FileInputStream.java:112)
        //       at sg.edu.nus.comp.cs4218.impl.util.IOUtils.openInputStream(IOUtils.java:34)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:67)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:41)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("<");
        stringList.add("Invalid syntax");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate4()
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
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("<");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate5()
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
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(">");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate6()
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
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(null);
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    void testEvaluate7()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Diffblue AI was unable to find a test

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate8()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: Invalid Filename
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:53)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:41)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(">");
        stringList.add("<");
        stringList.add("Invalid syntax");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate9()
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
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate10() throws FileNotFoundException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: foo: Invalid app
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.getApplication(ApplicationRunner.java:83)
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.runApp(ApplicationRunner.java:46)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:50)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("foo");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        DataInputStream stdin = mock(DataInputStream.class);
        callCommand.evaluate(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate11() throws IOException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: Unable to close streams
        //       at sg.edu.nus.comp.cs4218.impl.util.IOUtils.closeInputStream(IOUtils.java:76)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:53)
        //   java.io.FileNotFoundException
        //       at java.io.FilterInputStream.close(FilterInputStream.java:180)
        //       at sg.edu.nus.comp.cs4218.impl.util.IOUtils.closeInputStream(IOUtils.java:74)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:53)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        DataInputStream dataInputStream = mock(DataInputStream.class);
        doThrow(new FileNotFoundException()).when(dataInputStream).close();
        callCommand.evaluate(dataInputStream, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate12() throws IOException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        //       at jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        //       at jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        //       at jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        //       at java.util.Objects.checkIndex(Objects.java:372)
        //       at java.util.ArrayList.get(ArrayList.java:459)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:62)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:41)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(">");
        stringList.add("");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        DataInputStream dataInputStream = mock(DataInputStream.class);
        doThrow(new FileNotFoundException()).when(dataInputStream).close();
        callCommand.evaluate(dataInputStream, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#evaluate(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testEvaluate13() throws IOException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.isRedirOperator(IORedirectionHandler.java:88)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:52)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.evaluate(CallCommand.java:41)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(">");
        stringList.add(null);
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        DataInputStream dataInputStream = mock(DataInputStream.class);
        doThrow(new FileNotFoundException()).when(dataInputStream).close();
        callCommand.evaluate(dataInputStream, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#getIORedirection(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetIORedirection()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: Invalid syntax
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:33)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> argsList = new ArrayList<>();
        CallCommand callCommand = new CallCommand(argsList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.getIORedirection(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#getIORedirection(InputStream, OutputStream)}
     */
    @Test
    void testGetIORedirection2()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("Invalid syntax");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IORedirectionHandler actualIORedirection = callCommand.getIORedirection(byteArrayInputStream,
                byteArrayOutputStream);
        assertSame(byteArrayInputStream, actualIORedirection.getInputStream());
        assertSame(byteArrayOutputStream, actualIORedirection.getOutputStream());
        List<String> noRedirArgsList = actualIORedirection.getNoRedirArgsList();
        assertEquals(1, noRedirArgsList.size());
        assertEquals("Invalid syntax", noRedirArgsList.get(0));
    }

    /**
     * Method under test: {@link CallCommand#getIORedirection(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetIORedirection3()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: No such file or directory
        //       at sg.edu.nus.comp.cs4218.impl.util.IOUtils.openInputStream(IOUtils.java:36)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:67)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //   java.io.FileNotFoundException: C:\Users\lkl74\Downloads\nus\CS4218\cs4218-project-2023-team27\Invalid syntax (The system cannot find the file specified)
        //       at java.io.FileInputStream.open0(Native Method)
        //       at java.io.FileInputStream.open(FileInputStream.java:219)
        //       at java.io.FileInputStream.<init>(FileInputStream.java:157)
        //       at java.io.FileInputStream.<init>(FileInputStream.java:112)
        //       at sg.edu.nus.comp.cs4218.impl.util.IOUtils.openInputStream(IOUtils.java:34)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:67)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("<");
        stringList.add("Invalid syntax");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.getIORedirection(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#getIORedirection(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetIORedirection4()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.util.NoSuchElementException
        //       at java.util.ArrayList$Itr.next(ArrayList.java:1000)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:50)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("<");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.getIORedirection(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#getIORedirection(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetIORedirection5()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.util.NoSuchElementException
        //       at java.util.ArrayList$Itr.next(ArrayList.java:1000)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:50)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(">");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.getIORedirection(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#getIORedirection(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetIORedirection6()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: Invalid Filename
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:53)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add(">");
        stringList.add("<");
        stringList.add("Invalid syntax");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.getIORedirection(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#getIORedirection(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetIORedirection7()
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
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.getIORedirection(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#getIORedirection(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetIORedirection8() throws FileNotFoundException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: Invalid syntax
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:33)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> argsList = new ArrayList<>();
        CallCommand callCommand = new CallCommand(argsList, new ApplicationRunner());
        DataInputStream stdin = mock(DataInputStream.class);
        callCommand.getIORedirection(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#getIORedirection(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetIORedirection9()
            throws FileNotFoundException, UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        //       at jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        //       at jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        //       at jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        //       at java.util.Objects.checkIndex(Objects.java:372)
        //       at java.util.ArrayList.get(ArrayList.java:459)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:62)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("<");
        stringList.add("");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        ByteArrayInputStream stdin = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        callCommand.getIORedirection(stdin, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#getIORedirection(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetIORedirection10() throws FileNotFoundException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.ShellException: shell: No such file or directory
        //       at sg.edu.nus.comp.cs4218.impl.util.IOUtils.openInputStream(IOUtils.java:36)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:67)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //   java.io.FileNotFoundException: C:\Users\lkl74\Downloads\nus\CS4218\cs4218-project-2023-team27\Invalid syntax (The system cannot find the file specified)
        //       at java.io.FileInputStream.open0(Native Method)
        //       at java.io.FileInputStream.open(FileInputStream.java:219)
        //       at java.io.FileInputStream.<init>(FileInputStream.java:157)
        //       at java.io.FileInputStream.<init>(FileInputStream.java:112)
        //       at sg.edu.nus.comp.cs4218.impl.util.IOUtils.openInputStream(IOUtils.java:34)
        //       at sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler.extractRedirOptions(IORedirectionHandler.java:67)
        //       at sg.edu.nus.comp.cs4218.impl.cmd.CallCommand.getIORedirection(CallCommand.java:60)
        //   See https://diff.blue/R013 to resolve this issue.

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("<");
        stringList.add("Invalid syntax");
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        callCommand.getIORedirection(null, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link CallCommand#getIORedirection(InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetIORedirection11()
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
        CallCommand callCommand = new CallCommand(stringList, new ApplicationRunner());
        callCommand.getIORedirection(new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8")), null);
    }
}

