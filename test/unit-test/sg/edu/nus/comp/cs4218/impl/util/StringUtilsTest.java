package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilsTest {

    @Test
    void isBlank_nonBlankStrings_returnsFalse() {
        assertFalse(StringUtils.isBlank("ThisIsNotABlankString"));
        assertFalse(StringUtils.isBlank("This is a string with whitespace"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "     ", "\t", "\r"})
    void isBlank_blankStrings_returnsTrue(String input) {
        assertTrue(StringUtils.isBlank(input));
    }

    @Test
    @EnabledOnOs(value = OS.WINDOWS)
    void fileSeparator_osWindows_returnCorrectFileSeparator() {
        assertEquals(StringUtils.fileSeparator(), "\\\\");
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void fileSeparator_osNotWindows_returnCorrectFileSeparator() {
        assertEquals(StringUtils.fileSeparator(), "/");
    }

    @Test
    void multiplyChar_normalChar_returnCorrectMultiply() {
        assertEquals(StringUtils.multiplyChar('a', 10), "aaaaaaaaaa");
        assertEquals(StringUtils.multiplyChar('1', 1), "1");
        assertEquals(StringUtils.multiplyChar('\\', 10), "\\\\\\\\\\\\\\\\\\\\");
        assertEquals(StringUtils.multiplyChar('*', 0), "");
        assertEquals(StringUtils.multiplyChar('\'', 5), "'''''");
    }

    @Test
    void multiplyChar_negativeNumber_returnCorrectMultiply() {
        assertEquals(StringUtils.multiplyChar('a', -50), "");
        assertEquals(StringUtils.multiplyChar('b', 2147483647 + 1), "");
    }

    @Test
    void tokenize_validStrings_returnCorrectTokens() {
        assertArrayEquals(StringUtils.tokenize("this is a            test string"),
                new String[]{"this", "is", "a", "test", "string"});
        assertArrayEquals(StringUtils.tokenize("ThisIsATestString"),
                new String[]{"ThisIsATestString"});
        assertArrayEquals(StringUtils.tokenize("\\"),
                new String[]{"\\"});
        assertArrayEquals(StringUtils.tokenize("this is a test string\twith\ttabs"),
                new String[]{"this", "is", "a", "test", "string", "with", "tabs"});
        assertArrayEquals(StringUtils.tokenize("this is\fa\ntest\rstring\rwith weird\r\nspaces"),
                new String[]{"this", "is", "a", "test", "string", "with", "weird", "spaces"});
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\r\n", "     ", "\t", "\r", "\n", "\f"})
    void tokenize_invalidStrings_returnNoTokens(String input) {
        assertArrayEquals(StringUtils.tokenize(input), new String[0]);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-0", "0", "50", "-2147483647", "-2147483648", "2147483648",
            "10000000000000000000000000000000000000000", "-10000000000000000000000000000000000000000",
            "+12345", "+2147483648"})
    void isNumber_anyNumber_returnTrue(String input) {
        assertTrue(StringUtils.isNumber(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"a", "!", "-a", "124ab3", "(1234)", "\\", "0z", "z0", "@123"})
    void isNumber_notNumber_returnFalse(String input) {
        assertFalse(StringUtils.isNumber(input));
    }
}
