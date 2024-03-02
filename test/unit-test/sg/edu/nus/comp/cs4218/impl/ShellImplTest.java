package sg.edu.nus.comp.cs4218.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;
import sg.edu.nus.comp.cs4218.impl.util.ErrorConstants;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.only;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.INVALID_COMMAND;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.VALID_TEST_STRING;

public class ShellImplTest {


    static Stream<Arguments> generateExceptions() {
        return Stream.of(
                Arguments.of(new ShellException(INVALID_COMMAND)),
                Arguments.of(new FileNotFoundException(ErrorConstants.E_FILE_NOT_FOUND)),
                Arguments.of(new AbstractApplicationException(INVALID_COMMAND) {
                })
        );
    }

    @Test
    void parseAndEvaluate_validArguments_noError() throws FileNotFoundException, AbstractApplicationException, ShellException {
        ShellImpl shell = new ShellImpl();
        String testCommandString = "testApp";
        OutputStream outputStream = new ByteArrayOutputStream();
        CallCommand mockValidCommand = mock(CallCommand.class);
        doAnswer(args -> {
            OutputStream output = args.getArgument(1);
            output.write(VALID_TEST_STRING.getBytes());
            output.flush();
            return null;
        }).when(mockValidCommand).evaluate(any(), any());
        try (MockedStatic<CommandBuilder> commandBuilder = mockStatic(CommandBuilder.class)) {
            commandBuilder.when(() -> CommandBuilder.parseCommand(eq(testCommandString), any())).thenReturn(mockValidCommand);

            shell.parseAndEvaluate(testCommandString, outputStream);

            commandBuilder.verify(() -> CommandBuilder.parseCommand(eq(testCommandString), any()), only());

            assertEquals(VALID_TEST_STRING, outputStream.toString());
        }
    }

    @ParameterizedTest
    @MethodSource("generateExceptions")
    void parseAndEvaluate_invalidCommand_throwShellException(Throwable expectedException) throws FileNotFoundException, AbstractApplicationException, ShellException {
        ShellImpl shell = new ShellImpl();
        String testCommandString = "testApp";
        OutputStream outputStream = new ByteArrayOutputStream();
        CallCommand mockValidCommand = mock(CallCommand.class);
        doThrow(expectedException).when(mockValidCommand).evaluate(any(), any());
        try (MockedStatic<CommandBuilder> commandBuilder = mockStatic(CommandBuilder.class)) {
            commandBuilder.when(() -> CommandBuilder.parseCommand(eq(testCommandString), any())).thenReturn(mockValidCommand);

            Throwable exception = assertThrows(expectedException.getClass(), () -> shell.parseAndEvaluate(testCommandString, outputStream));

            commandBuilder.verify(() -> CommandBuilder.parseCommand(eq(testCommandString), any()), only());

            assertEquals(expectedException.getMessage(), exception.getMessage());
        }
    }
}
