package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.createFiles;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CallCommandIntegrationTest {
    private List<String> argsList;
    private ApplicationRunner appRunner;

    @BeforeEach
    void setup() {

        appRunner = mock(ApplicationRunner.class);
        argsList = new ArrayList<>();
    }

    @Test
    void evaluate_ioRedirectionHandled_noError() throws AbstractApplicationException, ShellException, FileNotFoundException {
        String command = "echo";
        String expectedArgs = "testing";
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        argsList.addAll(List.of(command, expectedArgs));
        CallCommand callCommand = new CallCommand(argsList, appRunner);

        callCommand.evaluate(inputStream, outputStream);

        verify(appRunner, only()).runApp(command, expectedArgs.split(" "), inputStream, outputStream);
    }

    @ParameterizedTest
    @MethodSource("sg.edu.nus.comp.cs4218.CommandTestUtils#validCommandsStringsWithIO")
    void evaluate_validCommandStringsWithIO_runApplication(String commandStr, String input, String output, @TempDir Path tempDir)
            throws AbstractApplicationException, ShellException, IOException {

        List<Path> paths = createFiles(tempDir, input, output);

        List<String> expected = new LinkedList<>(Arrays.asList(commandStr.split(" [>|<] ")));
        String[] command = expected.get(0).split(" ", 2);
        String[] expectedArgs = command.length > 1 && !command[1].isEmpty() ? command[1].split(" ") : new String[0];
        argsList.addAll(new LinkedList<>(Arrays.asList(commandStr.split(" "))));
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        CallCommand callCommand = new CallCommand(argsList, appRunner);
        try (MockedStatic<Environment> mEnv = mockStatic(Environment.class)) {
            mEnv.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
            callCommand.evaluate(inputStream, outputStream);
        }

        verify(appRunner, only()).runApp(eq(command[0]), eq(expectedArgs),
                paths.get(0) == null ? eq(inputStream) : any(), paths.get(1) == null ? eq(outputStream) : any());

        inputStream.close();
        outputStream.close();
    }

    @ParameterizedTest
    @MethodSource("sg.edu.nus.comp.cs4218.CommandTestUtils#validCommandsWithInvalidIO")
    void evaluate_invalidRedirect_noError(String command, String expectedArgs) throws AbstractApplicationException, ShellException, FileNotFoundException {
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        argsList.add(command);
        argsList.addAll(new LinkedList<>(Arrays.asList(expectedArgs.split(" "))));
        CallCommand callCommand = new CallCommand(argsList, appRunner);

        callCommand.evaluate(inputStream, outputStream);

        verify(appRunner, only()).runApp(command, expectedArgs.split(" "), inputStream, outputStream);
    }

    @ParameterizedTest
    @MethodSource("sg.edu.nus.comp.cs4218.CommandTestUtils#invalidCommandsWithvalidIO")
    void evaluate_invalidCommandStringsWithIO_throwsError(String commandStr, @TempDir Path tempDir) throws IOException {

        argsList.addAll(new LinkedList<>(Arrays.asList(commandStr.split(" "))));
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        CallCommand callCommand = new CallCommand(argsList, appRunner);
        try (MockedStatic<Environment> mEnv = mockStatic(Environment.class)) {
            mEnv.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
            assertThrows(ShellException.class, () -> callCommand.evaluate(inputStream, outputStream));
        }

        inputStream.close();
        outputStream.close();
    }

}
