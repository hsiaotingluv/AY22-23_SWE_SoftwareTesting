package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.SUB_COMMAND_TEST;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.TEST_STR_NEWLINES;

public class ArgumentResolverTest {

    @Test
    void evaluateSubCommand_validArguments_noError()
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        String testCommandString = "testApp";
        CallCommand mockValidCommand = mock(CallCommand.class);
        doAnswer(args -> {
            OutputStream output = args.getArgument(1);
            output.write(TEST_STR_NEWLINES.getBytes());
            output.flush();
            return null;
        }).when(mockValidCommand).evaluate(any(), any());
        try (MockedStatic<CommandBuilder> commandBuilder = mockStatic(CommandBuilder.class)) {
            commandBuilder.when(() -> CommandBuilder.parseCommand(eq(testCommandString), any())).thenReturn(mockValidCommand);

            String actualOutput = ArgumentResolver.evaluateSubCommand(testCommandString);
            commandBuilder.verify(() -> CommandBuilder.parseCommand(eq(testCommandString), any()), only());

            assertEquals(TEST_STR_NEWLINES.replace(System.lineSeparator(), " "), actualOutput);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r", "\r\n"})
    void evaluateSubCommand_invalidArguments_returnEmptyString(String testCommandString)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = ArgumentResolver.class
                .getDeclaredMethod("evaluateSubCommand", String.class);
        method.setAccessible(true);
        String actualOutput = (String) method.invoke(ArgumentResolver.class, testCommandString);
        assertEquals("", actualOutput);
    }

    @Test
    void appendParsedArgIntoSegment_emptyList_addIntoList() {
        List<RegexArgument> expParsedArgSeg = new LinkedList<>();
        LinkedList<RegexArgument> parsedArgSeg = new LinkedList<>();
        RegexArgument regexArgs = mock(RegexArgument.class);
        expParsedArgSeg.add(regexArgs);
        ArgumentResolver.appendParsedArgIntoSegment(parsedArgSeg, regexArgs);
        assertIterableEquals(expParsedArgSeg, parsedArgSeg);
    }

    @Test
    void appendParsedArgIntoSegment_nonEmptyList_addIntoList() {
        LinkedList<RegexArgument> parsedArgSeg = new LinkedList<>();
        RegexArgument lastParsedArg = mock(RegexArgument.class);
        parsedArgSeg.add(lastParsedArg);
        ArgumentResolver.appendParsedArgIntoSegment(parsedArgSeg, lastParsedArg);
        verify(lastParsedArg, only()).merge(lastParsedArg);
    }

    @Test
    void parseArguments_emptyList_returnParsedList() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> argsList = List.of("test1", "test2", "test3");
        try (MockedStatic<ArgumentResolver> argumentResolver = mockStatic(ArgumentResolver.class)) {
            argumentResolver.when(() -> ArgumentResolver.resolveOneArgument(any())).thenAnswer(i -> List.of((String) i.getArgument(0)));
            argumentResolver.when(() -> ArgumentResolver.parseArguments(argsList)).thenCallRealMethod();

            List<String> actualArgsList = ArgumentResolver.parseArguments(argsList);
            argumentResolver.verify(() -> ArgumentResolver.resolveOneArgument(any()), times(argsList.size()));

            assertIterableEquals(argsList, actualArgsList);
        }
    }

    @ParameterizedTest
    @MethodSource({"sg.edu.nus.comp.cs4218.CommandTestUtils#validCommandsWithQuotesStrings",
            "sg.edu.nus.comp.cs4218.CommandTestUtils#validCommandsWithAsterisks"})
    void resolveOneArgument_validArguments_returnValidList(List<String> commandString, String expectedArgs)
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        String args = commandString.get(1);
        RegexArgument regexArgs = mock(RegexArgument.class);
        when(regexArgs.globFiles()).thenReturn(List.of(expectedArgs));
        try (MockedStatic<ArgumentResolver> argumentResolver = mockStatic(ArgumentResolver.class, Mockito.CALLS_REAL_METHODS)) {
            argumentResolver.when(ArgumentResolver::makeRegexArgument).thenReturn(regexArgs);
            argumentResolver.when(() -> ArgumentResolver.makeRegexArgument(any())).thenReturn(regexArgs);
            argumentResolver.when(() -> ArgumentResolver.evaluateSubCommand(any())).thenReturn(SUB_COMMAND_TEST);
            argumentResolver.when(() -> ArgumentResolver.appendParsedArgIntoSegment(any(), any())).thenAnswer(i -> {
                List<RegexArgument> parsedArgs = i.getArgument(0);
                if (parsedArgs.isEmpty()) {
                    parsedArgs.add(regexArgs);
                }
                return null;
            });

            List<String> actualArgsList = ArgumentResolver.resolveOneArgument(args);

            assertIterableEquals(List.of(expectedArgs), actualArgsList);
        }
    }
}
