package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_ASTERISK;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_BACK_QUOTE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_DOUBLE_QUOTE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_SINGLE_QUOTE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_SPACE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public final class ArgumentResolver {

    private ArgumentResolver() {
    }

    /**
     * Handle quoting + globing + command substitution for a list of arguments.
     *
     * @param argsList The original list of arguments.
     * @return The list of parsed arguments.
     * @throws ShellException If any of the arguments have an invalid syntax.
     */
    public static List<String> parseArguments(List<String> argsList) throws AbstractApplicationException, ShellException, FileNotFoundException {
        List<String> parsedArgsList = new LinkedList<>();
        for (String arg : argsList) {
            parsedArgsList.addAll(resolveOneArgument(arg));
        }
        return parsedArgsList;
    }

    /**
     * Unwraps single and double quotes from one argument.
     * Performs globing when there are unquoted asterisks.
     * Performs command substitution.
     * <p>
     * Single quotes disable the interpretation of all special characters.
     * Double quotes disable the interpretation of all special characters, except for back quotes.
     *
     * @param arg String containing one argument.
     * @return A list containing one or more parsed args, depending on the outcome of the parsing.
     */
    public static List<String> resolveOneArgument(String arg) throws AbstractApplicationException, ShellException, FileNotFoundException {
        Stack<Character> unmatchedQuotes = new Stack<>();
        LinkedList<RegexArgument> parsedArgsSegment = new LinkedList<>();
        RegexArgument parsedArg = makeRegexArgument();
        StringBuilder subCommand = new StringBuilder();

        for (int i = 0; i < arg.length(); i++) {
            char chr = arg.charAt(i);

            if (chr == CHAR_BACK_QUOTE) {
                boolean resetParseArgs = parseBackQuote(chr, unmatchedQuotes, parsedArgsSegment, parsedArg, subCommand);
                if (resetParseArgs) {
                    parsedArg = makeRegexArgument();
                }
            } else if (chr == CHAR_SINGLE_QUOTE || chr == CHAR_DOUBLE_QUOTE) {
                parseSingleDoubleQuote(chr, unmatchedQuotes, parsedArgsSegment, parsedArg, subCommand);
            } else if (chr == CHAR_ASTERISK) {
                parseAsterisk(chr, unmatchedQuotes, parsedArg, subCommand);
            } else {
                parseOthers(chr, unmatchedQuotes, parsedArg, subCommand);
            }
        }

        if (!parsedArg.isEmpty()) {
            appendParsedArgIntoSegment(parsedArgsSegment, parsedArg);
        }

        // perform globing
        return parsedArgsSegment.stream()
                .flatMap(regexArgument -> regexArgument.globFiles().stream())
                .collect(Collectors.toList());
    }

    private static void parseOthers(char chr, Stack<Character> unmatchedQuotes, RegexArgument parsedArg, StringBuilder subCommand) {
        if (unmatchedQuotes.isEmpty()) {
            // not a special character
            parsedArg.append(chr);
        } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
            // ongoing back quote: add chr to subCommand
            subCommand.append(chr);
        } else {
            // ongoing single/double quote
            parsedArg.append(chr);
        }
    }

    private static void parseAsterisk(char chr, Stack<Character> unmatchedQuotes, RegexArgument parsedArg, StringBuilder subCommand) {
        if (unmatchedQuotes.isEmpty()) {
            // each unquoted * matches a (possibly empty) sequence of non-slash chars
            parsedArg.appendAsterisk();
        } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
            // ongoing back quote: add chr to subCommand
            subCommand.append(chr);
        } else {
            // ongoing single/double quote
            parsedArg.append(chr);
        }
    }

    private static void parseSingleDoubleQuote(char chr, Stack<Character> unmatchedQuotes, LinkedList<RegexArgument> parsedArgsSegment, RegexArgument parsedArg, StringBuilder subCommand) {
        if (unmatchedQuotes.isEmpty()) {
            // start of quote
            unmatchedQuotes.add(chr);
        } else if (unmatchedQuotes.peek() == chr) {
            // end of quote
            unmatchedQuotes.pop();

            // make sure parsedArgsSegment is not empty
            appendParsedArgIntoSegment(parsedArgsSegment, makeRegexArgument());
        } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
            // ongoing back quote: add chr to subCommand
            subCommand.append(chr);
        } else {
            // ongoing single/double quote
            parsedArg.append(chr);
        }
    }

    private static boolean parseBackQuote(char chr, Stack<Character> unmatchedQuotes,
                                          LinkedList<RegexArgument> parsedArgsSegment,
                                          RegexArgument parsedArg, StringBuilder subCommand)
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        boolean resetParsedArgs = false;
        if (unmatchedQuotes.isEmpty() || unmatchedQuotes.peek() == CHAR_DOUBLE_QUOTE) {
            // start of command substitution
            if (!parsedArg.isEmpty()) {
                appendParsedArgIntoSegment(parsedArgsSegment, parsedArg);
                resetParsedArgs = true;
            }

            unmatchedQuotes.add(chr);

        } else if (unmatchedQuotes.peek() == chr) {
            // end of command substitution
            unmatchedQuotes.pop();

            // evaluate subCommand and get the output
            String subCommandOutput = evaluateSubCommand(subCommand.toString());
            subCommand.setLength(0); // Clear the previous subCommand registered

            // check if back quotes are nested
            if (unmatchedQuotes.isEmpty()) {
                List<RegexArgument> subOutputSegment = Stream
                        .of(StringUtils.tokenize(subCommandOutput))
                        .map(str -> makeRegexArgument(str))
                        .collect(Collectors.toList());

                // append the first token to the previous parsedArg
                // e.g. arg: abc`1 2 3`xyz`4 5 6` (contents in `` is after command sub)
                // expected: [abc1, 2, 3xyz4, 5, 6]
                if (!subOutputSegment.isEmpty()) {
                    RegexArgument firstOutputArg = subOutputSegment.remove(0);
                    appendParsedArgIntoSegment(parsedArgsSegment, firstOutputArg);
                }

                parsedArgsSegment.addAll(new ArrayList<>(subOutputSegment));

            } else {
                // don't tokenize subCommand output
                appendParsedArgIntoSegment(parsedArgsSegment,
                        makeRegexArgument(subCommandOutput));
            }
        } else {
            // ongoing single quote
            parsedArg.append(chr);
        }
        return resetParsedArgs;
    }

    public static RegexArgument makeRegexArgument() {
        return new RegexArgument();
    }

    public static RegexArgument makeRegexArgument(String str) {
        return new RegexArgument(str);
    }

    static String evaluateSubCommand(String commandString) throws AbstractApplicationException, ShellException, FileNotFoundException {
        if (StringUtils.isBlank(commandString)) {
            return "";
        }

        OutputStream outputStream = new ByteArrayOutputStream();
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        command.evaluate(System.in, outputStream);
        String output = outputStream.toString();
        if (output.endsWith(STRING_NEWLINE)) {
            output = output.substring(0, output.length() - (STRING_NEWLINE.length()));
        }
        // replace newlines with spaces
        return output.replace(STRING_NEWLINE, String.valueOf(CHAR_SPACE));
    }

    /**
     * Append current parsedArg to the last parsedArg in parsedArgsSegment.
     * If parsedArgsSegment is empty, then just add current parsedArg.
     */
    static void appendParsedArgIntoSegment(LinkedList<RegexArgument> parsedArgsSegment,
                                           RegexArgument parsedArg) {
        if (parsedArgsSegment.isEmpty()) {
            parsedArgsSegment.add(parsedArg);
        } else {
            RegexArgument lastParsedArg = parsedArgsSegment.removeLast();
            lastParsedArg.merge(parsedArg);
            parsedArgsSegment.add(lastParsedArg);
        }
    }
}