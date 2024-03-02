package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CpArgsParserTest {

    public static final String E_ILLEGAL_OPT = "illegal option -- ";
    static final Set<Character> ALL_FLAGS = Set.of('r', 'R');
    private String src;
    private String dest;
    private CpArgsParser cpArgsParser;

    @BeforeEach
    void setUp() {
        this.cpArgsParser = new CpArgsParser();
        src = "src.txt";
        dest = "dest.txt";
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) throws InvalidArgsException {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> cpArgsParser.parse(input));
        String expectedError = "Missing Argument";
        assertEquals(thrown.getMessage(), expectedError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-r", "-R"})
    void parse_allValidFlag_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        cpArgsParser.parse(input, src, dest);
        assertFalse(Collections.disjoint(cpArgsParser.flags, ALL_FLAGS));
    }

    @Test
    void parse_oneValidArg_shouldReturnException() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> cpArgsParser.parse(src));
        String expectedError = "Missing Argument";
        assertEquals(thrown.getMessage(), expectedError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-x", "-1", "-!", "-+", "-?", "-|"})
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> cpArgsParser.parse(input));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPT + input.charAt(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-r", "-R"})
    void isRecursive_validFlagGiven_shouldReturnTrue(String input) throws InvalidArgsException {
        cpArgsParser.parse(input, src, dest);
        assertTrue(cpArgsParser.isRecursive());
    }

    @Test
    void getFileNames_twoArgs_shouldReturnTwoFileNames() throws InvalidArgsException {
        cpArgsParser.parse(src, dest);
        assertTrue(cpArgsParser.getFiles().containsAll(List.of(src, dest)));
    }

    @Test
    void getFileNames_multipleArgs_shouldReturnMultipleFileNames() throws InvalidArgsException {
        cpArgsParser.parse("'test'", "example.txt", "haha.java", "folder", "text");
        List<String> expected = List.of("'test'", "example.txt", "haha.java", "folder", "text");
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), cpArgsParser.getFiles().get(i));
        }
    }
}
