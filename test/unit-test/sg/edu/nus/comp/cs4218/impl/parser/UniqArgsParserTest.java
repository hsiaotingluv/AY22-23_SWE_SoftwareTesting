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

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class UniqArgsParserTest {
    public static final String E_ILLEGAL_OPTION = "illegal option -- ";
    static final Set<Character> ALL_FLAGS = Set.of('c', 'd', 'D');
    private UniqArgsParser uniqArgsParser;

    static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-c", "-d", "-D"}),
                Arguments.of((Object) new String[]{"-cdD"}),
                Arguments.of((Object) new String[]{"-cdD", "--c"}),
                Arguments.of((Object) new String[]{"-cdD", "-"}),
                Arguments.of((Object) new String[]{"-cdD", "in"}),
                Arguments.of((Object) new String[]{"-c", "--", "--", "example1.txt"}),
                Arguments.of((Object) new String[]{"-d", "1example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-D", "--", "-example1.txt", "out"}),
                Arguments.of((Object) new String[]{"in", "out"}),
                Arguments.of((Object) new String[]{"in", "--", "-out"}),
                Arguments.of((Object) new String[]{"--", "-", "example1.txt"}),
                Arguments.of((Object) new String[]{"--", "-", "-example1.txt"}),
                Arguments.of((Object) new String[]{"-", "--", "-"}),
                Arguments.of((Object) new String[]{"-", "--", "-example1.txt"})
        );
    }

    static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-cdD", "-w"}),
                Arguments.of((Object) new String[]{"-cdD", "-in", "--", "-out"}),
                Arguments.of((Object) new String[]{"-", "-cdD"}),
                Arguments.of((Object) new String[]{"-", "example.txt", "-i"}),
                Arguments.of((Object) new String[]{"-c", "-d-", "example1.txt"}),
                Arguments.of((Object) new String[]{"-", "-", "-example1.txt"}),
                Arguments.of((Object) new String[]{"-test", "--", "-example1.txt"}),
                Arguments.of((Object) new String[]{"-test", "-example1.txt"})

        );
    }

    @BeforeEach
    void setUp() {
        this.uniqArgsParser = new UniqArgsParser();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) throws InvalidArgsException, WcException {
        uniqArgsParser.parse(input);
        assertTrue(uniqArgsParser.flags.isEmpty());
        assertTrue(uniqArgsParser.nonFlagArgs.contains(input));
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_shouldReturnNothing(String... args) {
        assertDoesNotThrow(() -> uniqArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_shouldReturnErrorMessage(String... args) {
        assertThrows(InvalidArgsException.class, () -> uniqArgsParser.parse(args));
    }

    @Test
    void parse_allValidFlag_shouldReturnMatchingFlagGiven() throws InvalidArgsException {
        uniqArgsParser.parse("-c", "-d", "-D");
        assertTrue(uniqArgsParser.flags.containsAll(ALL_FLAGS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-x", "-1", "-!", "-+", "-?", "-|"})
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> uniqArgsParser.parse(input));
        String expectedError = "illegal option -- " + input.charAt(1);
        ;
        assertEquals(expectedError, thrown.getMessage());
    }

    @Test
    void isCount_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        uniqArgsParser.parse("-c");
        assertTrue(uniqArgsParser.isCount());
    }

    @Test
    void isCount_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> uniqArgsParser.parse("-C"));
        assertEquals(E_ILLEGAL_OPTION + "C", thrown.getMessage());
    }

    @Test
    void isCount_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        uniqArgsParser.parse("-d");
        assertFalse(uniqArgsParser.isCount());
    }

    @Test
    void isRepeated_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        uniqArgsParser.parse("-d");
        assertTrue(uniqArgsParser.isRepeated());
    }

    @Test
    void isRepeated_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        uniqArgsParser.parse("-c");
        assertFalse(uniqArgsParser.isRepeated());
    }

    @Test
    void isAllRepeated_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        uniqArgsParser.parse("-D");
        assertTrue(uniqArgsParser.isAllRepeated());
    }

    @Test
    void isAllRepeated_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        uniqArgsParser.parse("-d");
        assertFalse(uniqArgsParser.isAllRepeated());
    }

    @Test
    void getFiles_oneArg_shouldReturnNoFileName() throws InvalidArgsException {
        uniqArgsParser.parse("example.txt");
        assertTrue(uniqArgsParser.getFiles().contains("example.txt"));
    }

    @Test
    void getFiles_twoFileNames_shouldReturnBothFileNames() throws InvalidArgsException {
        uniqArgsParser.parse("in", "out");
        List<String> expected = List.of("in", "out");
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), uniqArgsParser.getFiles().get(i));
        }
    }

    @Test
    void getFiles_twoFileNamesWithDash_shouldReturnBothFileNames() throws InvalidArgsException {
        uniqArgsParser.parse("--", "-in", "out");
        List<String> expected = List.of("-in", "out");
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), uniqArgsParser.getFiles().get(i));
        }
    }

    @Test
    void getFiles_threeFiles_shouldReturnException() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> uniqArgsParser.parse("in", "out", "extra"));
        String expectedErr = "usage: uniq [-c | -d | -D] [INPUT [OUTPUT]] ...";
        assertEquals(expectedErr, thrown.getMessage());
    }

    @Test
    void hasOutputFile_outputFileGiven_shouldReturnTrue() throws InvalidArgsException {
        uniqArgsParser.parse("in", "out");
        assertTrue(uniqArgsParser.hasOutputFile());
    }

    @Test
    void hasOutputFile_noOutputFileGiven_shouldReturnTrue() throws InvalidArgsException {
        uniqArgsParser.parse("in");
        assertFalse(uniqArgsParser.hasOutputFile());
    }

    @Test
    void hasInputFile_inputFileGiven_shouldReturnTrue() throws InvalidArgsException {
        uniqArgsParser.parse("in");
        assertTrue(uniqArgsParser.hasInputFile());
        uniqArgsParser.parse("-");
        assertTrue(uniqArgsParser.hasInputFile());
    }

    @Test
    void hasInputFile_noInputFileGiven_shouldReturnTrue() throws InvalidArgsException {
        uniqArgsParser.parse();
        assertFalse(uniqArgsParser.hasInputFile());
    }
}
