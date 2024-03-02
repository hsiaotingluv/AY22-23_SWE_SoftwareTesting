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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class GrepArgsParserTest {
    public static final String E_ILLEGAL_OPTION = "illegal option -- ";
    public static final String E_NO_REGEX = "No regular expression supplied";
    static final Set<Character> ALL_FLAGS = Set.of('c', 'i', 'H');
    private GrepArgsParser grepArgsParser;

    static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-i", "-i", "example1.txt"}),
                Arguments.of((Object) new String[]{"-i", "example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-c", "--", "--", "example1.txt", "-i"}),
                Arguments.of((Object) new String[]{"-c", "--", "example1.txt", "-i"}),
                Arguments.of((Object) new String[]{"-icH", "-c", "-i", "-H", "example1.txt"}),
                Arguments.of((Object) new String[]{"-H", "example1.txt", "--test"}),
                Arguments.of((Object) new String[]{"-i", "example1.txt", "-", "---testing"}),
                Arguments.of((Object) new String[]{"-i", "example1.txt", "-", "--", "-'hello'"}),
                Arguments.of((Object) new String[]{"-i", "example1.txt", "--", "test"}),
                Arguments.of((Object) new String[]{"example1.txt", "example.txt", "----file"}),
                Arguments.of((Object) new String[]{"-icH", "example1.txt", "--", "-c", "example.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "example.txt", "--c", "---w", "example3.txt", "----"})
        );
    }

    static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-", "-i", "example1.txt"}),
                Arguments.of((Object) new String[]{"-t", "-i", "example1.txt"}),
                Arguments.of((Object) new String[]{"-", "example1.txt", "-i"}),
                Arguments.of((Object) new String[]{"-c", "-", "-i", "example1.txt"}),
                Arguments.of((Object) new String[]{"-", "-c", "example1.txt"}),
                Arguments.of((Object) new String[]{"-", "-H", "example1.txt"}),
                Arguments.of((Object) new String[]{"-i", "example1.txt", "-c"}),
                Arguments.of((Object) new String[]{"-i", "example1.txt", "-i"}),
                Arguments.of((Object) new String[]{"-i", "example1.txt", "-H"}),
                Arguments.of((Object) new String[]{"example1.txt", "example.txt", "-i"}),
                Arguments.of((Object) new String[]{"example1.txt", "-c", "example.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-H", "-i", "example.txt", "-c"})
        );
    }

    static Stream<String> invalidFlags() {
        return Stream.of("-x", "-1", "-!", "-+", "-?", "-|");
    }

    static Stream<String> invalidPatterns() {
        return Stream.of("[", "BOOM\\", "(?(", "*", "AABB???", "AA(C(B)A", "AA(C)B)A", "/\\${[^}]+}/", "/\\${$/u", "^[A-Za-z0-9\\]");
    }

    @BeforeEach
    void setUp() {
        this.grepArgsParser = new GrepArgsParser();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse(input));
        assertEquals(thrown.getMessage(), E_NO_REGEX);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-i", "-c", "-H"})
    void parse_allValidFlag_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        grepArgsParser.parse(input, "pattern");
        assertFalse(Collections.disjoint(grepArgsParser.flags, ALL_FLAGS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-icH", "-iHc", "-ciH", "-cHi", "-Hic", "-Hci"})
    void parse_allValidFlagTogether_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        grepArgsParser.parse(input, "pattern");
        List<Character> sortedExpected = new ArrayList<>(ALL_FLAGS);
        Collections.sort(sortedExpected);
        assertTrue(grepArgsParser.isCount());
        assertTrue(grepArgsParser.isCaseInsensitive());
        assertTrue(grepArgsParser.isPrintFileName());
        assertTrue(grepArgsParser.flags.containsAll(sortedExpected));
    }

    @Test
    void parse_oneValidArg_shouldReturnMatchingNonFlagGiven() throws InvalidArgsException {
        grepArgsParser.parse("x");
        assertTrue(grepArgsParser.nonFlagArgs.contains("x"));
    }

    @Test
    void parse_noArg_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse());
        assertEquals(thrown.getMessage(), E_NO_REGEX);
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_shouldReturnNothing(String... args) {
        assertDoesNotThrow(() -> grepArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_shouldReturnErrorMessage(String... args) {
        assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidFlags")
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse(input));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + input.charAt(1));
    }

    @Test
    void isCaseInsensitive_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        grepArgsParser.parse("-i", "pattern");
        assertTrue(grepArgsParser.isCaseInsensitive());
    }

    @Test
    void isCaseInsensitive_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse("-I"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "I");
    }

    @Test
    void isCaseInsensitive_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        grepArgsParser.parse("-c", "pattern");
        assertFalse(grepArgsParser.isCaseInsensitive());
    }

    @Test
    void isCount_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        grepArgsParser.parse("-c", "pattern");
        assertTrue(grepArgsParser.isCount());
    }

    @Test
    void isCount_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse("-C"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "C");
    }

    @Test
    void isCount_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        grepArgsParser.parse("-i", "pattern");
        assertFalse(grepArgsParser.isCount());
    }

    @Test
    void isPrintFileName_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        grepArgsParser.parse("-H", "pattern");
        assertTrue(grepArgsParser.isPrintFileName());
    }

    @Test
    void isPrintFileName_lowercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse("-h"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "h");
    }

    @Test
    void isPrintFileName_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        grepArgsParser.parse("-i", "pattern");
        assertFalse(grepArgsParser.isPrintFileName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "4218", "!!", "\"white shark\"", "\"wow 4218\"", "'haha'", "'1010'", "^GNU", "and$", "..cept", "t[wo]o"})
    void parse_validPatterns_shouldReturnGivenPattern(String input) throws InvalidArgsException {
        grepArgsParser.parse(input);
        assertEquals(input, grepArgsParser.getPattern());
    }

    @ParameterizedTest
    @MethodSource("invalidPatterns")
    void parse_invalidPattern_shouldThrowErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse(input));
        try {
            Pattern.compile(input);
        } catch (PatternSyntaxException exception) {
            assertEquals(thrown.getMessage(), "Invalid pattern: " + exception.getDescription());
        }
    }

    //since pattern is required, the filename parsed will be taken as a pattern instead
    @Test
    void getFileNames_oneArg_shouldReturnNoFileName() throws InvalidArgsException {
        grepArgsParser.parse("example.txt");
        assertTrue(grepArgsParser.getPattern().equals("example.txt"));
        assertArrayEquals(grepArgsParser.getFileNames(), new String[]{"-"});
    }

    @Test
    void getFileNames_twoArgs_shouldReturnOneFileName() throws InvalidArgsException {
        grepArgsParser.parse("'test'", "-", "example.txt");
        assertTrue(grepArgsParser.getPattern().equals("'test'"));
        assertTrue(Arrays.asList(grepArgsParser.getFileNames()).contains("example.txt"));
    }

    @Test
    void getFileNames_multipleArgs_shouldReturnMultipleFileNames() throws InvalidArgsException {
        grepArgsParser.parse("'test'", "example.txt", "haha.java", "folder", "text");
        List<String> expected = List.of("example.txt", "haha.java", "folder", "text");
        List<String> actual = Arrays.asList(grepArgsParser.getFileNames());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}
