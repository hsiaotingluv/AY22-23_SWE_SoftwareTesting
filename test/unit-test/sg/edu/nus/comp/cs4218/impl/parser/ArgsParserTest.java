package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ArgsParserTest {
    private ArgsParser argsParser;

    static Stream<Arguments> validSequenceCommandsStrings() {
        return Stream.of(
                Arguments.of(new String[]{"test", "example"},
                        List.of("test", "example")),
                Arguments.of(new String[]{"test example", "--", "-testing-", "_ok!?"},
                        List.of("test example", "-testing-", "_ok!?")),
                Arguments.of(new String[]{"-a", "--", "testing", "-_ok!?"},
                        List.of("testing", "-_ok!?")),
                Arguments.of(new String[]{"-ba", "_ok!?", "example.txt", "--", "-testing-"},
                        List.of("_ok!?", "example.txt", "-testing-"))
        );
    }

    @BeforeEach
    void setUp() {
        this.argsParser = spy(ArgsParser.class);
        argsParser.legalFlags.addAll(Set.of('a', 'b', 'c'));
    }

    @Test
    void validateArgs_validFlags_shouldHaveAllFlags() {
        argsParser.flags.addAll(Set.of('a', 'b', 'c'));
        assertDoesNotThrow(() -> argsParser.validateArgs());
    }

    @Test
    void validateArgs_invalidFlags_shouldReturnErrorMessage() {
        argsParser.flags.addAll(Set.of('a', 'e'));
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> argsParser.validateArgs());
        String expectedError = "illegal option -- e";
        assertEquals(thrown.getMessage(), expectedError);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) throws InvalidArgsException {
        doNothing().when(argsParser).validateArgs();
        argsParser.parse(input);
        assertTrue(argsParser.flags.isEmpty());
        assertTrue(argsParser.nonFlagArgs.contains(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-b", "-c"})
    void parse_validOneFlag_shouldReturnMatchingFlag(String input) throws InvalidArgsException {
        doNothing().when(argsParser).validateArgs();
        argsParser.parse(input);
        assertTrue(argsParser.flags.contains(input.charAt(1)));
        assertTrue(argsParser.nonFlagArgs.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-abc", "-acb", "-bac", "-bca", "-cab", "-cba"})
    void parse_validMultipleFlags_shouldReturnMatchingFlags(String input) throws InvalidArgsException {
        doNothing().when(argsParser).validateArgs();
        argsParser.parse(input);
        Set<Character> expectedFlags = Set.of('a', 'b', 'c');
        assertTrue(argsParser.flags.containsAll(expectedFlags));
        assertTrue(argsParser.nonFlagArgs.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c"})
    void parse_validOneNonFlag_shouldReturnMatchingNonFlag(String input) throws InvalidArgsException {
        doNothing().when(argsParser).validateArgs();
        argsParser.parse(input);
        assertTrue(argsParser.flags.isEmpty());
        assertTrue(argsParser.nonFlagArgs.contains(input));
    }

    @ParameterizedTest
    @MethodSource("validSequenceCommandsStrings")
    void parse_validMultipleNonFlags_shouldReturnMatchingNonFlags(String[] args, List<String> expected) throws InvalidArgsException {
        doNothing().when(argsParser).validateArgs();
        argsParser.parse(args);
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), argsParser.nonFlagArgs.get(i));
        }
    }
}
