package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.WcException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class WcArgsParserTest {
    public static final String E_ILLEGAL_OPTION = "illegal option -- ";
    static final Set<Character> ALL_FLAGS = Set.of('c', 'l', 'w');
    private WcArgsParser wcArgsParser;

    static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-clw"}),
                Arguments.of((Object) new String[]{"-c", "-l", "w"}),
                Arguments.of((Object) new String[]{"-c", " ", "--", "--", "-example1.txt"}),
                Arguments.of((Object) new String[]{"-l", "!1example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-l", "!1example1.txt", "-", "-"}),
                Arguments.of((Object) new String[]{"!1example1.txt", "--", "-test"}),
                Arguments.of((Object) new String[]{"-", "--", "-test"}),
                Arguments.of((Object) new String[]{"-", "example1.txt", "-"}),
                Arguments.of((Object) new String[]{"example1.txt", "--", "-ok"}),
                Arguments.of((Object) new String[]{"--", "-", "example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-", "example1.txt", "--", "-test", "-"})
        );
    }

    static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-", "-l", "example1.txt"}),
                Arguments.of((Object) new String[]{"-n", "-l", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "-w-", "example1.txt"}),
                Arguments.of((Object) new String[]{"-nw", "--", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "-l", "-example1.txt", "-w"}),
                Arguments.of((Object) new String[]{"-c", "-example1.txt", "-l", "test.txt"}),
                Arguments.of((Object) new String[]{"-example1.txt", "-1", "test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-1", "--", "-test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-i"})

        );
    }

    @BeforeEach
    void setUp() {
        this.wcArgsParser = new WcArgsParser();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) throws InvalidArgsException, WcException {
        wcArgsParser.parse(input);
        Set<Character> expectedFlags = Set.of('c', 'l', 'w');
        assertTrue(wcArgsParser.flags.containsAll(expectedFlags));
        assertTrue(wcArgsParser.nonFlagArgs.contains(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-c", "-l", "-w"})
    void parse_allValidFlag_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        wcArgsParser.parse(input);
        assertFalse(Collections.disjoint(wcArgsParser.flags, ALL_FLAGS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-clw", "-cwl", "-lcw", "-lwc", "-wlc", "-wcl"})
    void parse_allValidFlagTogether_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        wcArgsParser.parse(input);
        List<Character> sortedExpected = new ArrayList<>(ALL_FLAGS);
        Collections.sort(sortedExpected);
        assertTrue(wcArgsParser.isLines());
        assertTrue(wcArgsParser.isBytes());
        assertTrue(wcArgsParser.isWords());
        assertTrue(wcArgsParser.flags.containsAll(sortedExpected));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-x", "-1", "-!", "-+", "-?", "-|"})
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> wcArgsParser.parse(input));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + input.charAt(1));
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_shouldReturnNothing(String... args) {
        assertDoesNotThrow(() -> wcArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_shouldReturnErrorMessage(String... args) {
        assertThrows(InvalidArgsException.class, () -> wcArgsParser.parse(args));
    }

    @Test
    void isLines_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        wcArgsParser.parse("-l");
        assertTrue(wcArgsParser.isLines());
    }

    @Test
    void isLines_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> wcArgsParser.parse("-L"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "L");
    }

    @Test
    void isLines_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        wcArgsParser.parse("-c");
        assertFalse(wcArgsParser.isLines());
    }

    @Test
    void isBytes_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        wcArgsParser.parse("-c");
        assertTrue(wcArgsParser.isBytes());
    }

    @Test
    void isBytes_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> wcArgsParser.parse("-C"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "C");
    }

    @Test
    void isBytes_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        wcArgsParser.parse("-l");
        assertFalse(wcArgsParser.isBytes());
    }

    @Test
    void isWords_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        wcArgsParser.parse("-w");
        assertTrue(wcArgsParser.isWords());
    }

    @Test
    void isWords_lowercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> wcArgsParser.parse("-W"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "W");
    }

    @Test
    void isWords_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        wcArgsParser.parse("-c");
        assertFalse(wcArgsParser.isWords());
    }

    @Test
    void getFileNames_oneArg_shouldReturnOneFileName() throws InvalidArgsException {
        wcArgsParser.parse("-l", "example.txt");
        assertTrue(wcArgsParser.getFiles().contains("example.txt"));
    }

    @Test
    void getFileNames_multipleArgs_shouldReturnMultipleFileNames() throws InvalidArgsException {
        wcArgsParser.parse("'test'", "example.txt", "haha.java", "folder", "text");
        List<String> expected = List.of("'test'", "example.txt", "haha.java", "folder", "text");
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), wcArgsParser.getFiles().get(i));
        }
    }
}
