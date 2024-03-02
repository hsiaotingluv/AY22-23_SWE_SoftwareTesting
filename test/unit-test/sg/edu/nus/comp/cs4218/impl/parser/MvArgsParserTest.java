package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

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
public class MvArgsParserTest {
    public static final String ERR_MISS_ARG = "Missing Argument";
    public static final String ERR_ILLEGAL_OPT = "illegal option -- ";
    static final Set<Character> ALL_FLAGS = Set.of('n');
    private String src;
    private String dest;
    private MvArgsParser mvArgsParser;

    static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"example.txt", "example1.txt"}),
                Arguments.of((Object) new String[]{"src", "example1.txt"}),
                Arguments.of((Object) new String[]{"-n", "example.txt", "example1.txt"}),
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
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-n"}),
                Arguments.of((Object) new String[]{"-"}),
                Arguments.of((Object) new String[]{"-", "-n", "example1.txt"}),
                Arguments.of((Object) new String[]{"-n", "-l", "example1.txt"}),
                Arguments.of((Object) new String[]{"-n", "-n-", "example1.txt"}),
                Arguments.of((Object) new String[]{"-n--", "example1.txt"}),
                Arguments.of((Object) new String[]{"-nw", "--", "example1.txt"}),
                Arguments.of((Object) new String[]{"-n", "-example1.txt", "-w"}),
                Arguments.of((Object) new String[]{"-n", "-example1.txt", "-l", "test.txt"}),
                Arguments.of((Object) new String[]{"-example1.txt", "-1", "test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-1", "--", "-test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-i"})

        );
    }

    @BeforeEach
    void setUp() {
        this.mvArgsParser = new MvArgsParser();
        src = "src.txt";
        dest = "dest.txt";
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) throws InvalidArgsException {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> mvArgsParser.parse(input));
        assertEquals(ERR_MISS_ARG, thrown.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-n"})
    void parse_allValidFlag_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        mvArgsParser.parse(input, src, dest);
        assertFalse(Collections.disjoint(mvArgsParser.flags, ALL_FLAGS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-x", "-1", "-!", "-+", "-?", "-|"})
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> mvArgsParser.parse(input));
        assertEquals(thrown.getMessage(), ERR_ILLEGAL_OPT + input.charAt(1));
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_shouldReturnNothing(String... args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_shouldReturnErrorMessage(String... args) {
        assertThrows(InvalidArgsException.class, () -> mvArgsParser.parse(args));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-n"})
    void isOverwrite_validFlagGiven_shouldReturnFalse(String input) throws InvalidArgsException {
        mvArgsParser.parse(input, src, dest);
        assertFalse(mvArgsParser.isOverwrite());
    }

    @Test
    void getFiles_twoArgs_shouldReturnOneFileName() throws InvalidArgsException {
        mvArgsParser.parse(src, dest);
        assertTrue(mvArgsParser.getFiles().contains(src));
    }

    @Test
    void getFiles_multipleArgs_shouldReturnMultipleFileNames() throws InvalidArgsException {
        mvArgsParser.parse(src, "example.txt", "haha", dest);
        assertTrue(mvArgsParser.getFiles().containsAll(List.of(src, "example.txt", "haha")));
    }

    @Test
    void getDestDir_noDestDir_shouldReturnException() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> mvArgsParser.parse(src));
        assertEquals(ERR_MISS_ARG, thrown.getMessage());
    }

    @Test
    void getDestDir_destDirGiven_shouldReturnException() throws InvalidArgsException {
        mvArgsParser.parse(src, dest);
        assertTrue(mvArgsParser.getDestDir().contains(dest));
    }
}
