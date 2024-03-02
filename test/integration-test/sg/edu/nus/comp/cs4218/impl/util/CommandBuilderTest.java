package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.SHELL_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_SYNTAX;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
public class CommandBuilderTest {
    private ApplicationRunner appRunner;

    @BeforeEach
    void setup() {
        appRunner = mock(ApplicationRunner.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r", "\r\n",
            "|", "echo test|", ";", "echo test;", "echo \"'testing'", "echo 'testing`", "echo `testing",
            "echo testing`", "echo testing'", "echo \"testing\"'", "echo \"testing\"\"", "grep testing;;",
            "echo `test`ing`", "echo 'testing''", "paste test.txt ||", "echo testing; ;wc test.txt", "echo testing| |echo test"})
    void parseCommand_invalidArguments_throwShellException(String input) {
        Throwable throwable = assertThrowsExactly(ShellException.class, () -> CommandBuilder.parseCommand(input, appRunner));
        assertEquals(throwable.getMessage(), String.format(SHELL_EXCEPTION, E_SYNTAX));
    }

    @ParameterizedTest
    @MethodSource({"sg.edu.nus.comp.cs4218.CommandTestUtils#validCommandsStrings",
            "sg.edu.nus.comp.cs4218.CommandTestUtils#validCommandsStringsWithIO"})
    void parseCommand_validArguments_returnCallCommand(String commandStr) throws ShellException {
        CallCommand command = (CallCommand) CommandBuilder.parseCommand(commandStr, appRunner);
        assertIterableEquals(Arrays.asList(commandStr.split(" ")), command.getArgsList());
    }

    @ParameterizedTest
    @MethodSource("sg.edu.nus.comp.cs4218.CommandTestUtils#validCommandsWithQuotesStrings")
    void parseCommand_validArgumentsWithQuotes_returnCallCommand(List<String> expected) throws ShellException {
        CallCommand command = (CallCommand) CommandBuilder.parseCommand(String.join(" ", expected), appRunner);
        assertIterableEquals(expected, command.getArgsList());
    }

    @Test
    void parseCommand_validArgumentsWithoutSpace_returnCallCommand() throws ShellException {
        String commandStr = "paste – A.txt -<B.txt>AB.txt";
        List<String> expectedList = List.of("paste", "–", "A.txt", "-", "<", "B.txt", ">", "AB.txt");
        CallCommand command = (CallCommand) CommandBuilder.parseCommand(commandStr, appRunner);
        assertIterableEquals(expectedList, command.getArgsList());
    }

    @ParameterizedTest
    @MethodSource("sg.edu.nus.comp.cs4218.CommandTestUtils#validPipeCommandsStrings")
    void parseCommand_validPipeCommandsStrings_returnPipeCommand(String commandStr, List<List<String>> expected) throws ShellException {
        PipeCommand commands = (PipeCommand) CommandBuilder.parseCommand(commandStr, appRunner);
        List<CallCommand> parsedCommands = commands.getCallCommands();
        assertEquals(expected.size(), parsedCommands.size());
        for (int i = 0; i < expected.size(); i++) {
            assertIterableEquals(expected.get(i), parsedCommands.get(i).getArgsList());
        }
    }

    @ParameterizedTest
    @MethodSource("sg.edu.nus.comp.cs4218.CommandTestUtils#validSequenceCommandsStrings")
    void parseCommand_validSequenceCommandsStrings_returnSequenceCommand(String commandStr, List<List<String>> expected) throws ShellException {
        SequenceCommand commands = (SequenceCommand) CommandBuilder.parseCommand(commandStr, appRunner);
        List<Command> parsedCommands = commands.getCommands();

        int expectedIndex = 0;
        int parsedIndex = 0;
        while (expectedIndex < expected.size()) {
            Command result = parsedCommands.get(parsedIndex);
            if (result instanceof CallCommand) {
                CallCommand callCommand = (CallCommand) result;
                assertIterableEquals(expected.get(expectedIndex), callCommand.getArgsList());
                expectedIndex++;
                parsedIndex++;
            } else if (result instanceof PipeCommand) {
                PipeCommand pipeCommand = (PipeCommand) result;
                List<CallCommand> parsed = pipeCommand.getCallCommands();
                for (CallCommand callCommand : parsed) {
                    assertIterableEquals(expected.get(expectedIndex), callCommand.getArgsList());
                    expectedIndex++;
                }
                parsedIndex++;
            } else {
                fail("No such command.");
            }

        }
    }


}
