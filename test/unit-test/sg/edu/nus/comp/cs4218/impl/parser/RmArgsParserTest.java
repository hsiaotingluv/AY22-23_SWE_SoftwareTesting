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
public class RmArgsParserTest {
    public static final String E_ILLEGAL_OPTION = "illegal option -- ";
    static final Set<Character> ALL_FLAGS = Set.of('r', 'd');
    private String exampleFile;
    private RmArgsParser rmArgsParser;

    static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"file"}),
                Arguments.of((Object) new String[]{"-rd", "file"}),
                Arguments.of((Object) new String[]{"-r", "d"}),
                Arguments.of((Object) new String[]{"-r", "  --", "--", "-example1.txt"}),
                Arguments.of((Object) new String[]{"-r", "!1example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-d", "!1example1.txt", "-", "-"}),
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
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-rd"}),
                Arguments.of((Object) new String[]{"-", "-rd", "example1.txt"}),
                Arguments.of((Object) new String[]{"-r", "-l", "example1.txt"}),
                Arguments.of((Object) new String[]{"-r", "-d-", "example1.txt"}),
                Arguments.of((Object) new String[]{"-rw", "--", "example1.txt"}),
                Arguments.of((Object) new String[]{"-r", "-d", "-example1.txt", "-w"}),
                Arguments.of((Object) new String[]{"-r", "-example1.txt", "-l", "test.txt"}),
                Arguments.of((Object) new String[]{"-example1.txt", "-1", "test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-1", "--", "-test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-i"}),
                Arguments.of((Object) new String[]{"-example1.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-!"})

        );
    }

    @BeforeEach
    void setUp() {
        this.rmArgsParser = new RmArgsParser();
        exampleFile = "example.txt";
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) throws InvalidArgsException {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> rmArgsParser.parse(input));
        String expectedError = "usage: rm [-dr] FILES ...";
        assertEquals(thrown.getMessage(), expectedError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-r", "-d"})
    void parse_allValidFlag_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        rmArgsParser.parse(input, exampleFile);
        assertFalse(Collections.disjoint(rmArgsParser.flags, ALL_FLAGS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-rd", "-dr"})
    void parse_allValidFlagTogether_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        rmArgsParser.parse(input, exampleFile);
        List<Character> sortedExpected = new ArrayList<>(ALL_FLAGS);
        Collections.sort(sortedExpected);
        assertTrue(rmArgsParser.flags.containsAll(sortedExpected));
    }

    @Test
    void parse_oneValidArg_shouldReturnMatchingNonFlagGiven() throws InvalidArgsException {
        rmArgsParser.parse("test");
        assertTrue(rmArgsParser.nonFlagArgs.contains("test"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-W", "-12", "-!", "-+", "-?", "-|"})
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> rmArgsParser.parse(input));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + input.charAt(1));
    }

    @Test
    void validateArgs_zeroNonFlagArgs_shouldThrowInvalidArgsException() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> rmArgsParser.parse("-r"));
        String expectedError = "usage: rm [-dr] FILES ...";
        assertEquals(thrown.getMessage(), expectedError);
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_shouldReturnNothing(String... args) {
        assertDoesNotThrow(() -> rmArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_shouldReturnErrorMessage(String... args) {
        assertThrows(InvalidArgsException.class, () -> rmArgsParser.parse(args));
    }

    @Test
    void isRecursive_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        rmArgsParser.parse("-r", exampleFile);
        Boolean isCaseSensitive = rmArgsParser.isRecursive();
        assertTrue(isCaseSensitive);
    }

    @Test
    void isRecursive_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> rmArgsParser.parse("-R"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "R");
    }

    @Test
    void isRecursive_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        rmArgsParser.parse("-d", exampleFile);
        Boolean isCaseSensitive = rmArgsParser.isRecursive();
        assertFalse(isCaseSensitive);
    }

    @Test
    void isEmptyFolder_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        rmArgsParser.parse("-d", exampleFile);
        Boolean isCaseSensitive = rmArgsParser.isEmptyFolder();
        assertTrue(isCaseSensitive);
    }

    @Test
    void isEmptyFolder_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> rmArgsParser.parse("-D"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "D");
    }

    @Test
    void isEmptyFolder_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        rmArgsParser.parse("-r", exampleFile);
        Boolean isCaseSensitive = rmArgsParser.isEmptyFolder();
        assertFalse(isCaseSensitive);
    }

    @Test
    void getFileNames_oneArg_shouldReturnOneFileName() throws InvalidArgsException {
        rmArgsParser.parse(exampleFile);
        assertTrue(rmArgsParser.getFiles().contains(exampleFile));
    }

    @Test
    void getFileNames_multipleArgs_shouldReturnMultipleFileNames() throws InvalidArgsException {
        rmArgsParser.parse("'test'", "example.txt", "haha.java", "folder", "text");
        List<String> expected = List.of("'test'", "example.txt", "haha.java", "folder", "text");
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), rmArgsParser.getFiles().get(i));
        }
    }
}
