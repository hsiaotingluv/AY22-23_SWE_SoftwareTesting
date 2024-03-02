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
public class TeeArgsParserTest {
    public static final String E_ILLEGAL_OPTION = "illegal option -- ";
    static final Set<Character> ALL_FLAGS = Set.of('a');
    private String exampleFile;
    private TeeArgsParser teeArgsParser;

    static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-a"}),
                Arguments.of((Object) new String[]{"-a", " ", "--", "--", "-example1.txt"}),
                Arguments.of((Object) new String[]{"-a", "!1example1.txt", "-"}),
                Arguments.of((Object) new String[]{"-a", "!1example1.txt", "-", "-"}),
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
                Arguments.of((Object) new String[]{"-", "-a", "example1.txt"}),
                Arguments.of((Object) new String[]{"-a", "-l", "example1.txt"}),
                Arguments.of((Object) new String[]{"-a", "-R-", "example1.txt"}),
                Arguments.of((Object) new String[]{"-ab", "--", "example1.txt"}),
                Arguments.of((Object) new String[]{"-a", "-example1.txt", "-r"}),
                Arguments.of((Object) new String[]{"-a", "-example1.txt", "-a"}),
                Arguments.of((Object) new String[]{"-a", "-example1.txt", "-A", "test.txt"}),
                Arguments.of((Object) new String[]{"-example1.txt", "-1", "test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-1", "--", "-test.txt"}),
                Arguments.of((Object) new String[]{"example1.txt", "-i"}),
                Arguments.of((Object) new String[]{"example1.txt", "-!"})

        );
    }

    @BeforeEach
    void setUp() {
        this.teeArgsParser = new TeeArgsParser();
        exampleFile = "example.txt";
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) throws InvalidArgsException, WcException {
        teeArgsParser.parse(input);
        assertTrue(teeArgsParser.flags.isEmpty());
        assertTrue(teeArgsParser.nonFlagArgs.contains(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a"})
    void parse_allValidFlag_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        teeArgsParser.parse(input);
        assertFalse(Collections.disjoint(teeArgsParser.flags, ALL_FLAGS));
    }

    @Test
    void parse_oneValidArg_shouldReturnMatchingNonFlagGiven() throws InvalidArgsException {
        teeArgsParser.parse("test");
        assertTrue(teeArgsParser.nonFlagArgs.contains("test"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-W", "-12", "-!", "-+", "-?", "-|"})
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> teeArgsParser.parse(input));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + input.charAt(1));
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_shouldReturnNothing(String... args) {
        assertDoesNotThrow(() -> teeArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_shouldReturnErrorMessage(String... args) {
        assertThrows(InvalidArgsException.class, () -> teeArgsParser.parse(args));
    }

    @Test
    void isAppendToFile_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        teeArgsParser.parse("-a");
        Boolean isCaseSensitive = teeArgsParser.isAppendToFile();
        assertTrue(isCaseSensitive);
    }

    @Test
    void isAppendToFile_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> teeArgsParser.parse("-A"));
        assertEquals(thrown.getMessage(), E_ILLEGAL_OPTION + "A");
    }

    @Test
    void getFileNames_oneArg_shouldReturnOneFileName() throws InvalidArgsException {
        teeArgsParser.parse(exampleFile);
        assertTrue(teeArgsParser.getFiles().contains(exampleFile));
    }

    @Test
    void getFileNames_multipleArgs_shouldReturnMultipleFileNames() throws InvalidArgsException {
        teeArgsParser.parse("'test'", "example.txt", "haha.java", "folder", "text");
        List<String> expected = List.of("'test'", "example.txt", "haha.java", "folder", "text");
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), teeArgsParser.getFiles().get(i));
        }
    }
}
