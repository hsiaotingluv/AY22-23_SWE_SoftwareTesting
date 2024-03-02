package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_ILLEGAL_FLAG;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CutArgsParserTest {
    public static final String E_INVALID_SYN = "Invalid syntax";
    static final Set<Character> ALL_FLAGS = Set.of('c', 'b');
    private CutArgsParser cutArgsParser;

    static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-c", "1", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "1-10", "example1.txt"}),
                Arguments.of((Object) new String[]{"-b", "1,2,10,15", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "3-400", "-"}),
                Arguments.of((Object) new String[]{"-c", "100-100", "-"}),
                Arguments.of((Object) new String[]{"-b", "1-2000", "example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-b", "1-2000", "example1.txt", "-", "--", "-test"}),
                Arguments.of((Object) new String[]{"-b", "1-2000", "", "  ", "example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-b", "1-2000", "", "  ", "\n", "example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-c", "1,2,100,2000,30000", "example1.txt", "-", "example2.ok"}),
                Arguments.of((Object) new String[]{"-c", "1"}),
                Arguments.of((Object) new String[]{"-b", "1"})

        );
    }

    static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-", "-c", "1", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "-", "1", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "-", "0", "example1.txt"}),
                Arguments.of((Object) new String[]{"-cb", "1,2", "example1.txt", "-i"}),
                Arguments.of((Object) new String[]{"-c", "-", "-b", "1-2", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "1-0", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "0-0", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "0-10", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "1-test", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "test-1", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "1--2", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "-1-2", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "1-2-", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "-1-2-", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "-2", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "2-", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "2-1", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "100-1", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "2-!@#", "example1.txt"}),
                Arguments.of((Object) new String[]{"-b", "test", "example1.txt"}),
                Arguments.of((Object) new String[]{"-b", "1,0", "example1.txt"}),
                Arguments.of((Object) new String[]{"-b", "0,0", "example1.txt"}),
                Arguments.of((Object) new String[]{"-b", "1,12,4,0,1,2,5", "example1.txt"}),
                Arguments.of((Object) new String[]{"-b", "1,word", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "word,1", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "!!@#,1", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", ",1", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "1,,", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", ",,1", "example1.txt"}),
                Arguments.of((Object) new String[]{"-c", "2,,1", "example1.txt"}),
                Arguments.of((Object) new String[]{"1-2", "example.txt"}),
                Arguments.of((Object) new String[]{"example.txt"})
        );
    }

    static Stream<String> invalidFlags() {
        return Stream.of("-x", "-1", "-!", "-+", "-?", "-|");
    }

    static Stream<Arguments> validList() {
        return Stream.of(
                Arguments.of(new String[]{"-c", "1", "example1.txt"}, List.of(new int[]{1, 1})),
                Arguments.of(new String[]{"-c", "1-5", "example1.txt"}, List.of(new int[]{1, 5})),
                Arguments.of(new String[]{"-c", "10-10", "example1.txt"}, List.of(new int[]{10, 10})),
                Arguments.of(new String[]{"-b", "1,2,10,15", "example1.txt"}, List.of(new int[]{1, 1}, new int[]{2, 2}, new int[]{10, 10}, new int[]{15, 15})),
                Arguments.of(new String[]{"-b", "100,3,1521", "example1.txt"}, List.of(new int[]{100, 100}, new int[]{3, 3}, new int[]{1521, 1521})),
                Arguments.of(new String[]{"-c", "3-400", "-"}, List.of(new int[]{3, 400})),
                Arguments.of(new String[]{"-b", "1-2000", "example1.txt", "-"}, List.of(new int[]{1, 2000})),
                Arguments.of(new String[]{"-b", "1-3", "", "  ", "example1.txt", "-"}, List.of(new int[]{1, 3}))
        );
    }

    @BeforeEach
    void setUp() {
        this.cutArgsParser = new CutArgsParser();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse(input));
        String expectedError = "Invalid syntax";
        assertEquals(thrown.getMessage(), expectedError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-c", "-b"})
    void parse_allValidFlag_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        cutArgsParser.parse(input, "12", "-");
        assertFalse(Collections.disjoint(cutArgsParser.flags, ALL_FLAGS));
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_shouldReturnNothing(String... args) {
        assertDoesNotThrow(() -> cutArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_shouldReturnErrorMessage(String... args) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse(args));
        assertEquals(E_INVALID_SYN, thrown.getMessage());
    }

    @Test
    void parse_oneValidArg_shouldReturnMatchingNonFlagGiven() throws InvalidArgsException {
        cutArgsParser.parse("-c", "1", "test.txt");
        assertTrue(cutArgsParser.nonFlagArgs.contains("test.txt"));
    }

    @Test
    void parse_noArg_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse());
        assertEquals(E_INVALID_SYN, thrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidFlags")
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse(input));
        assertEquals(E_ILLEGAL_FLAG + input.charAt(1), thrown.getMessage());
    }

    @Test
    void isCharacter_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        cutArgsParser.parse("-c", "1", "test");
        assertTrue(cutArgsParser.isCharPo());
    }

    @Test
    void isCharacter_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse("-C", "1", "test"));
        assertEquals(E_ILLEGAL_FLAG + "C", thrown.getMessage());
    }

    @Test
    void isCharacter_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        cutArgsParser.parse("-b", "1", "test");
        assertFalse(cutArgsParser.isCharPo());
    }

    @Test
    void isByte_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        cutArgsParser.parse("-b", "1", "test");
        assertTrue(cutArgsParser.isBytePo());
    }

    @Test
    void isByte_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse("-B", "1", "test"));
        assertEquals(E_ILLEGAL_FLAG + "B", thrown.getMessage());
    }

    @Test
    void isByte_differentFlagGiven_shouldReturnFalse() throws InvalidArgsException {
        cutArgsParser.parse("-c", "1", "test");
        assertFalse(cutArgsParser.isBytePo());
    }

    @ParameterizedTest
    @MethodSource("validList")
    void getList_validList_shouldReturnListOfNumbers(String[] args, List<int[]> expected) throws InvalidArgsException {
        cutArgsParser.parse(args);
        for (int i = 0; i < cutArgsParser.getRanges().size(); i++) {
            int[] actual = cutArgsParser.getRanges().get(i);
            int[] expectedInner = expected.get(i);
            Arrays.sort(expectedInner);
            Arrays.sort(actual);
            assertTrue(Arrays.equals(expectedInner, actual));
        }
    }

    @Test
    void getFileNames_oneFileName_shouldReturnNoFileName() throws InvalidArgsException {
        cutArgsParser.parse("-c", "1", "example.txt");
        assertTrue(Arrays.asList(cutArgsParser.getFiles()).contains("example.txt"));
    }

    @Test
    void getFileNames_multipleArgs_shouldReturnMultipleFileNames() throws InvalidArgsException {
        cutArgsParser.parse("-c", "1,2", "-", "'test'", "example.txt", "haha.java", "-", "folder", "text");
        List<String> expected = List.of("-", "'test'", "example.txt", "haha.java", "-", "folder", "text");
        List<String> actual = Arrays.asList(cutArgsParser.getFiles());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}

