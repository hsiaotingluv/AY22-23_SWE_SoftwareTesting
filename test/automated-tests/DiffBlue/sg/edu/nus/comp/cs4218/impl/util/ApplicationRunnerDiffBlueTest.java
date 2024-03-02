package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Application;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

class ApplicationRunnerDiffBlueTest {
    /**
     * Method under test: {@link ApplicationRunner#runApp(String, String[], InputStream, OutputStream)}
     */
    @Test
    void testRunApp() throws UnsupportedEncodingException, AbstractApplicationException, ShellException {
        ApplicationRunner applicationRunner = new ApplicationRunner();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        assertThrows(ShellException.class,
                () -> applicationRunner.runApp("App", new String[]{"Args Array"}, inputStream, new ByteArrayOutputStream()));
    }

    /**
     * Method under test: {@link ApplicationRunner#runApp(String, String[], InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testRunApp2() throws UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.CatException: cat: Args Array: No such file or directory
        //       at sg.edu.nus.comp.cs4218.impl.app.CatApplication.catFiles(CatApplication.java:111)
        //       at sg.edu.nus.comp.cs4218.impl.app.CatApplication.run(CatApplication.java:54)
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.runApp(ApplicationRunner.java:47)
        //   See https://diff.blue/R013 to resolve this issue.

        ApplicationRunner applicationRunner = new ApplicationRunner();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        applicationRunner.runApp(ApplicationRunner.APP_CAT, new String[]{"Args Array"}, inputStream,
                new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link ApplicationRunner#runApp(String, String[], InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testRunApp3() throws UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.CdException: cd: No such file or directory
        //       at sg.edu.nus.comp.cs4218.impl.app.CdApplication.getNormalizedAbsolutePath(CdApplication.java:70)
        //       at sg.edu.nus.comp.cs4218.impl.app.CdApplication.changeToDirectory(CdApplication.java:23)
        //       at sg.edu.nus.comp.cs4218.impl.app.CdApplication.run(CdApplication.java:55)
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.runApp(ApplicationRunner.java:47)
        //   See https://diff.blue/R013 to resolve this issue.

        ApplicationRunner applicationRunner = new ApplicationRunner();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        applicationRunner.runApp(ApplicationRunner.APP_CD, new String[]{"Args Array"}, inputStream,
                new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link ApplicationRunner#runApp(String, String[], InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testRunApp4() throws UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.CpException: cp: Missing Argument
        //       at sg.edu.nus.comp.cs4218.impl.app.CpApplication.parseArgs(CpApplication.java:168)
        //       at sg.edu.nus.comp.cs4218.impl.app.CpApplication.run(CpApplication.java:32)
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.runApp(ApplicationRunner.java:47)
        //   sg.edu.nus.comp.cs4218.exception.InvalidArgsException: Missing Argument
        //       at sg.edu.nus.comp.cs4218.impl.parser.CpArgsParser.validateArgs(CpArgsParser.java:30)
        //       at sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.parse(ArgsParser.java:73)
        //       at sg.edu.nus.comp.cs4218.impl.app.CpApplication.parseArgs(CpApplication.java:166)
        //       at sg.edu.nus.comp.cs4218.impl.app.CpApplication.run(CpApplication.java:32)
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.runApp(ApplicationRunner.java:47)
        //   sg.edu.nus.comp.cs4218.exception.InvalidArgsException: Missing Argument
        //       at sg.edu.nus.comp.cs4218.impl.parser.CpArgsParser.validateArgs(CpArgsParser.java:27)
        //       at sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.parse(ArgsParser.java:73)
        //       at sg.edu.nus.comp.cs4218.impl.app.CpApplication.parseArgs(CpApplication.java:166)
        //       at sg.edu.nus.comp.cs4218.impl.app.CpApplication.run(CpApplication.java:32)
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.runApp(ApplicationRunner.java:47)
        //   See https://diff.blue/R013 to resolve this issue.

        ApplicationRunner applicationRunner = new ApplicationRunner();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        applicationRunner.runApp(ApplicationRunner.APP_CP, new String[]{"Args Array"}, inputStream,
                new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link ApplicationRunner#runApp(String, String[], InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testRunApp5() throws AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.CatException: cat: Args Array: No such file or directory
        //       at sg.edu.nus.comp.cs4218.impl.app.CatApplication.catFiles(CatApplication.java:111)
        //       at sg.edu.nus.comp.cs4218.impl.app.CatApplication.run(CatApplication.java:54)
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.runApp(ApplicationRunner.java:47)
        //   See https://diff.blue/R013 to resolve this issue.

        ApplicationRunner applicationRunner = new ApplicationRunner();
        DataInputStream inputStream = mock(DataInputStream.class);
        applicationRunner.runApp(ApplicationRunner.APP_CAT, new String[]{"Args Array"}, inputStream,
                new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link ApplicationRunner#runApp(String, String[], InputStream, OutputStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testRunApp6() throws AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   sg.edu.nus.comp.cs4218.exception.CutException: cut: Invalid syntax
        //       at sg.edu.nus.comp.cs4218.impl.app.CutApplication.parseArgs(CutApplication.java:69)
        //       at sg.edu.nus.comp.cs4218.impl.app.CutApplication.run(CutApplication.java:36)
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.runApp(ApplicationRunner.java:47)
        //   sg.edu.nus.comp.cs4218.exception.InvalidArgsException: Invalid syntax
        //       at sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser.parse(CutArgsParser.java:32)
        //       at sg.edu.nus.comp.cs4218.impl.app.CutApplication.parseArgs(CutApplication.java:67)
        //       at sg.edu.nus.comp.cs4218.impl.app.CutApplication.run(CutApplication.java:36)
        //       at sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.runApp(ApplicationRunner.java:47)
        //   See https://diff.blue/R013 to resolve this issue.

        ApplicationRunner applicationRunner = new ApplicationRunner();
        DataInputStream inputStream = mock(DataInputStream.class);
        applicationRunner.runApp(ApplicationRunner.APP_CUT, new String[]{"Args Array"}, inputStream,
                new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link ApplicationRunner#getApplication(String)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetApplication() throws UnsupportedEncodingException, AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //   See https://diff.blue/R013 to resolve this issue.

        Application actualApplication = (new ApplicationRunner()).getApplication("App");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"));
        actualApplication.run(new String[]{"foo"}, byteArrayInputStream, new ByteArrayOutputStream());
    }

    /**
     * Method under test: {@link ApplicationRunner#getApplication(String)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetApplication2() throws AbstractApplicationException, ShellException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //   See https://diff.blue/R013 to resolve this issue.

        Application actualApplication = (new ApplicationRunner()).getApplication("App");
        DataInputStream dataInputStream = mock(DataInputStream.class);
        actualApplication.run(new String[]{"foo"}, dataInputStream, new ByteArrayOutputStream());
    }
}

