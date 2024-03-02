package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.CommandTestUtils;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.SHELL_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_SYNTAX;

public class CallCommandTest {

    private CallCommand callCommand;
    private List<String> argsList;
    private ApplicationRunner appRunner;

    @BeforeEach
    void setup() {

        appRunner = mock(ApplicationRunner.class);
        argsList = new ArrayList<>();
        callCommand = spy(new CallCommand(argsList, appRunner));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void evaluate_invalidArgs_throwShellException(List<String> invalidArgsList) {
        callCommand = spy(new CallCommand(invalidArgsList, appRunner));
        Throwable throwable = assertThrowsExactly(ShellException.class,
                () -> callCommand.evaluate(new ByteArrayInputStream("".getBytes()), new ByteArrayOutputStream()));
        assertEquals(throwable.getMessage(), String.format(SHELL_EXCEPTION, E_SYNTAX));
    }

    @ParameterizedTest
    @MethodSource("sg.edu.nus.comp.cs4218.CommandTestUtils#validCommandsStrings")
    void evaluate_validCommandStrings_runApplication(String commandStr)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        List<String> expected = new LinkedList<>(Arrays.asList(commandStr.split(" ")));
        String command = expected.get(0);
        String[] expectedArgs = expected.subList(1, expected.size()).toArray(new String[0]);
        argsList.addAll(expected);
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        IORedirectionHandler mIORedirHandler = mock(IORedirectionHandler.class);
        when(mIORedirHandler.getNoRedirArgsList()).thenReturn(expected);
        when(mIORedirHandler.getInputStream()).thenReturn(inputStream);
        when(mIORedirHandler.getOutputStream()).thenReturn(outputStream);
        doReturn(mIORedirHandler).when(callCommand).getIORedirection(inputStream, outputStream);

        try (MockedStatic<ArgumentResolver> argumentResolver = mockStatic(ArgumentResolver.class)) {
            argumentResolver.when(() -> ArgumentResolver.parseArguments(expected)).thenReturn(expected);
            callCommand.evaluate(inputStream, outputStream);
            argumentResolver.verify(() -> ArgumentResolver.parseArguments(expected));
        }
        verify(appRunner, only()).runApp(command, expectedArgs, inputStream, outputStream);
    }

    @ParameterizedTest
    @MethodSource("sg.edu.nus.comp.cs4218.CommandTestUtils#validCommandsStringsWithIO")
    void evaluate_validCommandStringsWithIO_runApplication(String commandStr, String input, String output, @TempDir Path tempDir)
            throws AbstractApplicationException, ShellException, IOException {

        List<Path> paths = CommandTestUtils.createFiles(tempDir, input, output);

        List<String> expected = new LinkedList<>(Arrays.asList(commandStr.split("[>|<]", 1)));
        String command = expected.get(0);
        String[] expectedArgs = expected.subList(1, expected.size()).toArray(new String[0]);
        argsList.addAll(expected);
        InputStream inputStream = paths.get(0) != null ? new FileInputStream(paths.get(0).toFile()) : new ByteArrayInputStream("test".getBytes());
        OutputStream outputStream = paths.get(1) != null ? new FileOutputStream(paths.get(1).toFile()) : new ByteArrayOutputStream();

        IORedirectionHandler mIORedirHandler = mock(IORedirectionHandler.class);
        when(mIORedirHandler.getNoRedirArgsList()).thenReturn(expected);
        when(mIORedirHandler.getInputStream()).thenReturn(inputStream);
        when(mIORedirHandler.getOutputStream()).thenReturn(outputStream);
        doReturn(mIORedirHandler).when(callCommand).getIORedirection(inputStream, outputStream);

        try (MockedStatic<ArgumentResolver> argumentResolver = mockStatic(ArgumentResolver.class)) {
            argumentResolver.when(() -> ArgumentResolver.parseArguments(expected)).thenReturn(expected);
            callCommand.evaluate(inputStream, outputStream);
            argumentResolver.verify(() -> ArgumentResolver.parseArguments(expected));
        }
        verify(appRunner, only()).runApp(command, expectedArgs, inputStream, outputStream);

        inputStream.close();
        outputStream.close();
    }

}
