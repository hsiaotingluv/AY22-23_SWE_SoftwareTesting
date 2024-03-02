package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.WcException;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasteArgsParserTest {
    static final Set<Character> ALL_FLAGS = Set.of('s');
    private PasteArgsParser pasteArgsParser;
    private String exampleFile;

    @BeforeEach
    void setUp() {
        this.pasteArgsParser = new PasteArgsParser();
        exampleFile = "example.txt";
    }

    @ParameterizedTest
    @ValueSource(strings = {"-s"})
    void parse_allValidFlag_shouldReturnMatchingFlagGiven(String input) throws InvalidArgsException {
        pasteArgsParser.parse(input, exampleFile);
        assertFalse(Collections.disjoint(pasteArgsParser.flags, ALL_FLAGS));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void parse_blankStrings_shouldReturnEmptyFlagsAndSameNonFlags(String input) throws InvalidArgsException, WcException {
        pasteArgsParser.parse(input);
        assertTrue(pasteArgsParser.flags.isEmpty());
        assertTrue(pasteArgsParser.nonFlagArgs.contains(input));
    }

    @Test
    void parse_oneValidArg_shouldReturnMatchingNonFlagGiven() throws InvalidArgsException {
        pasteArgsParser.parse("test");
        assertTrue(pasteArgsParser.nonFlagArgs.contains("test"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-W", "-12", "-!", "-+", "-?", "-|"})
    void parse_invalidFlag_shouldReturnErrorMessage(String input) {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> pasteArgsParser.parse(input));
        String expectedError = "illegal option -- " + input.charAt(1);
        assertEquals(expectedError, thrown.getMessage());
    }

    @Test
    void isSerial_validFlagGiven_shouldReturnTrue() throws InvalidArgsException {
        pasteArgsParser.parse("-s", exampleFile);
        Boolean isCaseSensitive = pasteArgsParser.isSerial();
        assertTrue(isCaseSensitive);
    }

    @Test
    void isSerial_uppercaseFlagGiven_shouldReturnErrorMessage() {
        InvalidArgsException thrown = assertThrows(InvalidArgsException.class, () -> pasteArgsParser.parse("-R"));
        String expectedError = "illegal option -- R";
        assertEquals(expectedError, thrown.getMessage());
    }

    @Test
    void getFiles_oneArg_shouldReturnOneFileName() throws InvalidArgsException {
        pasteArgsParser.parse(exampleFile);
        assertTrue(pasteArgsParser.getFiles().contains(exampleFile));
    }

    @Test
    void getFiles_multipleArgs_shouldReturnMultipleFileNames() throws InvalidArgsException {
        pasteArgsParser.parse("'test'", "example.txt", "haha.java", "folder", "text");
        Set<String> expected = Set.of("'test'", "example.txt", "haha.java", "folder", "text");
        assertTrue(expected.containsAll(pasteArgsParser.getFiles()));
    }
}
