package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

/**
 * Integration testing with RegexArgument
 */
public class ArgumentResolverIntegrationTest {

    static Stream<Arguments> argsWithQuotes() {
        return Stream.of(
                Arguments.of("\"This is space:`echo \" \"`.\"", "This is space: ."),
                Arguments.of("'This is space:`echo \" \"`.'", "This is space:`echo \" \"`."),
                Arguments.of("\"This is space:' '.\"", "This is space:' '."),
                Arguments.of("\"'This is space `echo \" \"`'\"", "'This is space  '"),
                Arguments.of("'\"This is space `echo \" \"`\"'", "\"This is space `echo \" \"`\""),
                Arguments.of("'Travel time Singapore -> Paris is 13h and 15`'", "Travel time Singapore -> Paris is 13h and 15`"),
                Arguments.of("\"'abcdef'\"", "'abcdef'")
        );
    }

    @Test
    void resolveOneArgument_validArguments_returnSegmentedList()
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        String args = "`echo \"‘quote is not interpreted as special character’\"`";
        List<String> expectedArgsList = Arrays.asList("‘quote is not interpreted as special character’".split(" "));
        List<String> actualArgsList = ArgumentResolver.resolveOneArgument(args);
        assertIterableEquals(expectedArgsList, actualArgsList);
    }

    @ParameterizedTest
    @MethodSource("argsWithQuotes")
    void resolveOneArgument_validArguments_returnListWithOneArgs(String args, String expectedOutput)
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> actualArgsList = ArgumentResolver.resolveOneArgument(args);
        assertIterableEquals(List.of(expectedOutput), actualArgsList);
    }
}
