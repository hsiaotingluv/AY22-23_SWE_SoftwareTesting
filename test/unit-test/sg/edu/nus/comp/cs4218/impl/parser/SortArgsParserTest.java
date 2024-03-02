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
public class SortArgsParserTest {
    public static final String E_ILLEGAL_OPTION = "illegal option -- ";
    static final Set<Character> ALL_FLAGS = Set.of('n', 'r', 'f');
    private SortArgsParser sortArgsParser;

    static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-nrf"}),
                Arguments.of((Object) new String[]{"-n", "-r", "f"}),
                Arguments.of((Object) new String[]{"-n", " ", "--", "--", "-example1.txt"}),
                Arguments.of((Object) new String[]{"-r", "!1example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-f", "!1example1.txt", "-", "-"}),
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
                Arguments.of((Object) new String[]{"-", "-n", "example1.txt"}),
                Arguments.of((Object) new String[]{"-n", "-l", "example1.txt"}),
                Arguments.of((Object) new String[]{"-r", "-f-", "example1.txt"}),
                Arguments.of((Object) new String[]{"-nw", "--", "example1.txt"}),
                Arguments.of((Object) new String[]{"-r", "-n", "-example1.txt", "-w"}),
                Arguments.of((Object) new String[]{"-f", "-example1.txt", "-l", "test.txt"}),
                Arguments.of((Object) new String[]{"-example1.txt", "-1", "test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-1", "--", "-test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-i"}),
                Arguments.of((Object) new String[]{"example1.txt", "-!"})

        );
    }

    @BeforeEach
    void setUp() {
        this.sortArgsParser = new SortArgsParser();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) throws InvalidArgsException, WcException {
        sortArgsParser.parse(input);
        assertTrue(sortArgsParser.flags.isEmpty());
        assertTrue(sortArgsParser.nonFlagArgs.contains(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-n", "-r", "-f"})
    void parse_allValidFlag_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        sortArgsParser.parse(input);
        assertFalse(Collections.disjoint(sortArgsParser.flags, ALL_FLAGS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-nrf", "-nfr", "-rfn", "-rnf", "-frn", "-fnr"})
    void parse_allValidFlagTogether_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        sortArgsParser.parse(input);
        List<Character> sortedExpected = new ArrayList<>(ALL_FLAGS);
        Collections.sort(sortedExpected);
        assertTrue(sortArgsParser.isCaseIndependent());
        assertTrue(sortArgsParser.isReverseOrder());
        assertTrue(sortArgsParser.isFirstWordNumber());
        assertTrue(sortArgsParser.flags.containsAll(sortedExpected));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-x", "-1", "-!", "-+", "-?", "-|"})
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> sortArgsParser.parse(input));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + input.charAt(1));
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_shouldReturnNothing(String... args) {
        assertDoesNotThrow(() -> sortArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_shouldReturnErrorMessage(String... args) {
        assertThrows(InvalidArgsException.class, () -> sortArgsParser.parse(args));
    }

    @Test
    void isReverseOrder_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        sortArgsParser.parse("-r");
        assertTrue(sortArgsParser.isReverseOrder());
    }

    @Test
    void isReverseOrder_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> sortArgsParser.parse("-R"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "R");
    }

    @Test
    void isReverseOrder_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        sortArgsParser.parse("-n");
        assertFalse(sortArgsParser.isReverseOrder());
    }

    @Test
    void isFirstWordNumber_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        sortArgsParser.parse("-n");
        assertTrue(sortArgsParser.isFirstWordNumber());
    }

    @Test
    void isFirstWordNumber_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> sortArgsParser.parse("-N"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "N");
    }

    @Test
    void isFirstWordNumber_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        sortArgsParser.parse("-r");
        assertFalse(sortArgsParser.isFirstWordNumber());
    }

    @Test
    void isCaseIndependent_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        sortArgsParser.parse("-f");
        assertTrue(sortArgsParser.isCaseIndependent());
    }

    @Test
    void isCaseIndependent_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> sortArgsParser.parse("-F"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "F");
    }

    @Test
    void isCaseIndependent_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        sortArgsParser.parse("-r");
        assertFalse(sortArgsParser.isCaseIndependent());
    }

    @Test
    void getFileNames_oneArg_shouldReturnOneFileName() throws InvalidArgsException {
        sortArgsParser.parse("-r", "example.txt");
        assertTrue(sortArgsParser.getFiles().contains("example.txt"));
    }

    @Test
    void getFileNames_multipleArgs_shouldReturnMultipleFileNames() throws InvalidArgsException {
        sortArgsParser.parse("'test'", "example.txt", "haha.java", "folder", "text");
        List<String> expected = List.of("'test'", "example.txt", "haha.java", "folder", "text");
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), sortArgsParser.getFiles().get(i));
        }
    }
}
