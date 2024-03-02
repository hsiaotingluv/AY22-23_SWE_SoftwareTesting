package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Application;
import sg.edu.nus.comp.cs4218.app.CatInterface;
import sg.edu.nus.comp.cs4218.app.CdInterface;
import sg.edu.nus.comp.cs4218.app.CpInterface;
import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.app.EchoInterface;
import sg.edu.nus.comp.cs4218.app.ExitInterface;
import sg.edu.nus.comp.cs4218.app.GrepInterface;
import sg.edu.nus.comp.cs4218.app.LsInterface;
import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.app.SortInterface;
import sg.edu.nus.comp.cs4218.app.TeeInterface;
import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.app.WcInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class ApplicationRunnerTest {

    private ApplicationRunner appRunner;

    @BeforeEach
    void setUp() {
        this.appRunner = new ApplicationRunner();
    }

    @Test
        // EF1: mv, paste, uniq, ls
    void getApplication_eF1Interfaces_correctApplications() throws ShellException {
        assertTrue(appRunner.getApplication("mv") instanceof MvInterface);
        assertTrue(appRunner.getApplication("ls") instanceof LsInterface);
        assertTrue(appRunner.getApplication("uniq") instanceof UniqInterface);
        assertTrue(appRunner.getApplication("paste") instanceof PasteInterface);

    }

    @Test
        // EF2: sort, rm, tee, cat
    void getApplication_eF2Interfaces_correctApplications() throws ShellException {
        assertTrue(appRunner.getApplication("sort") instanceof SortInterface);
        assertTrue(appRunner.getApplication("rm") instanceof RmInterface);
        assertTrue(appRunner.getApplication("tee") instanceof TeeInterface);
        assertTrue(appRunner.getApplication("cat") instanceof CatInterface);
    }

    @Test
        //  BF: wc, cp, exit, cut, grep, cd, echo
    void getApplication_bFInterfaces_correctApplications() throws ShellException {
        assertTrue(appRunner.getApplication("wc") instanceof WcInterface);
        assertTrue(appRunner.getApplication("cp") instanceof CpInterface);
        assertTrue(appRunner.getApplication("exit") instanceof ExitInterface);
        assertTrue(appRunner.getApplication("cut") instanceof CutInterface);
        assertTrue(appRunner.getApplication("grep") instanceof GrepInterface);
        assertTrue(appRunner.getApplication("cd") instanceof CdInterface);
        assertTrue(appRunner.getApplication("echo") instanceof EchoInterface);
    }

    @Test
    void getApplication_invalidApplication_throwShellException() {
        assertThrowsExactly(ShellException.class, () -> appRunner.getApplication("doesNotExist"));
    }

    @Test
    void runApp_validArguments_noExceptions() throws ShellException, AbstractApplicationException {
        String app = "testApp";
        byte[] testString = "Test valid inputStream".getBytes();
        String[] argsArray = new String[0];
        InputStream inputStream = new ByteArrayInputStream(testString);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.appRunner = spy(ApplicationRunner.class);
        doReturn((Application) (args, stdin, stdout) -> {
            try {
                stdout.write(stdin.readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).when(appRunner).getApplication(app);
        this.appRunner.runApp(app, argsArray, inputStream, outputStream);
        assertArrayEquals(outputStream.toByteArray(), testString);
    }

}
