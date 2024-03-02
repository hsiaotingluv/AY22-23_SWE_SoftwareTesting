package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

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
public class LsArgsParserTest {
    public static final String E_ILLEGAL_OPTION = "illegal option -- ";
    static final Set<Character> ALL_FLAGS = Set.of('R', 'X');
    private LsArgsParser lsArgsParser;

    static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-XR"}),
                Arguments.of((Object) new String[]{"-X", "-R"}),
                Arguments.of((Object) new String[]{"-X", " ", "--", "--", "-example1.txt"}),
                Arguments.of((Object) new String[]{"-R", "!1example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-R", "!1example1.txt", "-", "-"}),
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
                Arguments.of((Object) new String[]{"-", "-R", "example1.txt"}),
                Arguments.of((Object) new String[]{"-X", "-l", "example1.txt"}),
                Arguments.of((Object) new String[]{"-X", "-R-", "example1.txt"}),
                Arguments.of((Object) new String[]{"-Xw", "--", "example1.txt"}),
                Arguments.of((Object) new String[]{"-R", "-X", "-example1.txt", "-r"}),
                Arguments.of((Object) new String[]{"-X", "-example1.txt", "-R", "test.txt"}),
                Arguments.of((Object) new String[]{"-example1.txt", "-1", "test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-1", "--", "-test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-i"}),
                Arguments.of((Object) new String[]{"example1.txt", "-!"})

        );
    }

    @BeforeEach
    void setUp() {
        this.lsArgsParser = new LsArgsParser();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) throws InvalidArgsException {
        lsArgsParser.parse(input);
        assertTrue(lsArgsParser.flags.isEmpty());
        assertTrue(lsArgsParser.nonFlagArgs.contains(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-R", "-X"})
    void parse_allValidFlag_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        lsArgsParser.parse(input);
        assertFalse(Collections.disjoint(lsArgsParser.flags, ALL_FLAGS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-RX", "-XR"})
    void parse_allValidFlagTogether_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        lsArgsParser.parse(input);
        List<Character> sortedExpected = new ArrayList<>(ALL_FLAGS);
        Collections.sort(sortedExpected);
        assertTrue(lsArgsParser.flags.containsAll(sortedExpected));
    }

    @Test
    void parse_oneValidArg_shouldReturnMatchingNonFlagGiven() throws InvalidArgsException {
        lsArgsParser.parse("test");
        assertTrue(lsArgsParser.nonFlagArgs.contains("test"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-W", "-12", "-!", "-+", "-?", "-|"})
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> lsArgsParser.parse(input));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + input.charAt(1));
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_shouldReturnNothing(String... args) {
        assertDoesNotThrow(() -> lsArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_shouldReturnErrorMessage(String... args) {
        assertThrows(InvalidArgsException.class, () -> lsArgsParser.parse(args));
    }

    @Test
    void isRecursive_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        lsArgsParser.parse("-R");
        Boolean isCaseSensitive = lsArgsParser.isRecursive();
        assertTrue(isCaseSensitive);
    }

    @Test
    void isRecursive_lowercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> lsArgsParser.parse("-r"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "r");
    }

    @Test
    void isRecursive_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        lsArgsParser.parse("-X");
        Boolean isCaseSensitive = lsArgsParser.isRecursive();
        assertFalse(isCaseSensitive);
    }

    @Test
    void isSortByExt_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        lsArgsParser.parse("-X");
        Boolean isSortByExt = lsArgsParser.isSortByExt();
        assertTrue(isSortByExt);
    }

    @Test
    void isSortByExt_lowercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> lsArgsParser.parse("-x"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "x");
    }

    @Test
    void isSortByExt_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        lsArgsParser.parse("-R");
        Boolean isSortByExt = lsArgsParser.isSortByExt();
        assertFalse(isSortByExt);
    }

    @Test
    void getFileNames_oneArg_shouldReturnOneFileName() throws InvalidArgsException {
        lsArgsParser.parse("example.txt");
        assertTrue(lsArgsParser.getDirectories().contains("example.txt"));
    }

    @Test
    void getFileNames_multipleArgs_shouldReturnMultipleFileNames() throws InvalidArgsException {
        lsArgsParser.parse("'test'", "example.txt", "haha.java", "folder", "text");
        List<String> expected = List.of("'test'", "example.txt", "haha.java", "folder", "text");
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), lsArgsParser.getDirectories().get(i));
        }
    }
}
