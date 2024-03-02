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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CatArgsParserTest {
    public static final String E_ILLEGAL_OPTION = "illegal option -- ";
    static final Set<Character> ALL_FLAGS = Set.of('n');
    private CatArgsParser catArgsParser;

    static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-n"}),
                Arguments.of((Object) new String[]{"-"}),
                Arguments.of((Object) new String[]{"-n", "--", "--", "-example1.txt"}),
                Arguments.of((Object) new String[]{"-n", "1example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-n", "-", "example1.txt", "-"}),
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
                Arguments.of((Object) new String[]{"-n", "-n-", "example1.txt"}),
                Arguments.of((Object) new String[]{"-nw", "--", "example1.txt"}),
                Arguments.of((Object) new String[]{"-n", "-w-", "-example1.txt", "-w"}),
                Arguments.of((Object) new String[]{"-n", "-example1.txt", "-l", "test.txt"}),
                Arguments.of((Object) new String[]{"-example1.txt", "-1", "test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-1", "--", "-test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-i"})

        );
    }

    @BeforeEach
    void setUp() {
        this.catArgsParser = new CatArgsParser();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) throws InvalidArgsException, WcException {
        catArgsParser.parse(input);
        assertTrue(catArgsParser.flags.isEmpty());
        assertTrue(catArgsParser.nonFlagArgs.contains(input));
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_shouldReturnNothing(String... args) {
        assertDoesNotThrow(() -> catArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_shouldReturnErrorMessage(String... args) {
        assertThrows(InvalidArgsException.class, () -> catArgsParser.parse(args));
    }

    @Test
    void parse_allValidFlag_shouldReturnMatchingFlagGiven() throws InvalidArgsException {
        catArgsParser.parse("-n");
        assertTrue(catArgsParser.flags.containsAll(ALL_FLAGS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-x", "-1", "-!", "-+", "-?", "-|"})
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> catArgsParser.parse(input));
        String expectedError = "illegal option -- " + input.charAt(1);
        ;
        assertEquals(expectedError, thrown.getMessage());
    }

    @Test
    void isLineNumber_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        catArgsParser.parse("-n");
        assertTrue(catArgsParser.isLineNumber());
    }

    @Test
    void isLines_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> catArgsParser.parse("-N"));
        assertEquals(E_ILLEGAL_OPTION + "N", thrown.getMessage());
    }

    @Test
    void getFiles_oneArg_shouldReturnOneFileName() throws InvalidArgsException {
        catArgsParser.parse("example.txt");
        assertTrue(Arrays.asList(catArgsParser.getFiles()).contains("example.txt"));
    }

    @Test
    void getFiles_multipleArgs_shouldReturnMultipleFileNames() throws InvalidArgsException {
        catArgsParser.parse("'test'", "example.txt", "haha.java", "folder", "-", "text");
        List<String> expected = List.of("'test'", "example.txt", "haha.java", "folder", "-", "text");
        List<String> actual = Arrays.asList(catArgsParser.getFiles());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}
