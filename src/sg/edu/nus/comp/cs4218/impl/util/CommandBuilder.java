package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_PIPE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_OUTPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_SEMICOLON;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public final class CommandBuilder {
    /**
     * Regular expression for extracting valid arguments from the command string:
     * (NO_QUOTE | SINGLE_QUOTE | NESTED_BACK_QUOTE | DOUBLE_QUOTE | BACK_QUOTE)+
     * <p>
     * The order matters because it affects the matching priority.
     * <p>
     * NO_QUOTE: [^'\"`|<>;\\s]+
     * SINGLE_QUOTE: '[^']*'
     * NESTED_BACK_QUOTE: \"([^\"`]*`.*?`[^\"`]*)+\"
     * DOUBLE_QUOTE: \"[^\"]*\"
     * BACK_QUOTE: `[^`]*`
     */
    private static final Pattern ARGUMENT_REGEX = Pattern
            .compile("([^'\"`|<>;\\s]+|'[^']*'|\"([^\"`]*`.*?`[^\"`]*)+\"|\"[^\"]*\"|`[^`]*`)+");

    private static final int NO_CHANGE = 0;
    private static final int RESET_TOKEN = 1;
    private static final int RESET_TOKEN_PIPE = 2;

    private CommandBuilder() {
    }

    /**
     * Parses and tokenizes the provided command string into command(s) and arguments.
     * <p>
     * CallCommand takes in a list of tokens, PipeCommand takes in a list of CallCommands,
     * and SequenceCommand takes in a list of CallCommands / PipeCommands.
     *
     * @return Final command to be evaluated.
     * @throws ShellException If the provided command string has an invalid syntax.
     */
    public static Command parseCommand(String commandString, ApplicationRunner appRunner)
            throws ShellException {
        if (StringUtils.isBlank(commandString) || commandString.contains(STRING_NEWLINE)) {
            throw new ShellException(E_SYNTAX);
        }

        List<Command> cmdsForSequence = new LinkedList<>();
        List<CallCommand> callCmdsForPipe = new LinkedList<>();
        List<String> tokens = new LinkedList<>();
        int state = NO_CHANGE;

        String commandSubstring = commandString;
        while (!commandSubstring.isEmpty()) {
            commandSubstring = commandSubstring.trim();
            Matcher matcher = ARGUMENT_REGEX.matcher(commandSubstring);

            // no valid arguments found
            if (!matcher.find()) {
                throw new ShellException(E_SYNTAX);
            }

            // found a valid argument at the start of the command substring
            if (matcher.start() == 0) {
                tokens.add(matcher.group());
                commandSubstring = commandSubstring.substring(matcher.end());
                continue;
            }

            // found a valid argument but not at the start of the command substring
            char firstChar = commandSubstring.charAt(0);
            commandSubstring = commandSubstring.substring(1);

            state = parseSpecialChar(firstChar, tokens, callCmdsForPipe, cmdsForSequence, appRunner);
            if (state == RESET_TOKEN_PIPE) {
                callCmdsForPipe = new LinkedList<>();
            }
            if (state >= RESET_TOKEN) {
                tokens = new LinkedList<>();
            }
        }

        return buildCommand(tokens, callCmdsForPipe, cmdsForSequence, appRunner);
    }

    private static Command buildCommand(List<String> tokens, List<CallCommand> callCmdsForPipe,
                                        List<Command> cmdsForSequence, ApplicationRunner appRunner) {
        Command finalCommand = new CallCommand(tokens, appRunner);
        if (!callCmdsForPipe.isEmpty()) {
            // add CallCommand as part of ongoing PipeCommand
            callCmdsForPipe.add((CallCommand) finalCommand);
            finalCommand = new PipeCommand(callCmdsForPipe);
        }
        if (!cmdsForSequence.isEmpty()) {
            // add CallCommand / PipeCommand as part of ongoing SequenceCommand
            cmdsForSequence.add(finalCommand);
            finalCommand = new SequenceCommand(cmdsForSequence);
        }
        return finalCommand;
    }

    private static int parseSpecialChar(char firstChar, List<String> tokens,
                                        List<CallCommand> callCmdsForPipe, List<Command> cmdsForSequence,
                                        ApplicationRunner appRunner) throws ShellException {
        int state = NO_CHANGE;
        switch (firstChar) {
            case CHAR_REDIR_INPUT:
            case CHAR_REDIR_OUTPUT:
                // add as a separate token on its own
                tokens.add(String.valueOf(firstChar));
                break;
            case CHAR_PIPE:
                if (tokens.isEmpty()) {
                    // cannot start a new command with pipe
                    throw new ShellException(E_SYNTAX);
                } else {
                    // add CallCommand as part of a PipeCommand
                    callCmdsForPipe.add(new CallCommand(tokens, appRunner));
                    state = RESET_TOKEN;
                }
                break;

            case CHAR_SEMICOLON:
                if (tokens.isEmpty()) {
                    // cannot start a new command with semicolon
                    throw new ShellException(E_SYNTAX);
                } else if (callCmdsForPipe.isEmpty()) {
                    // add CallCommand as part of a SequenceCommand
                    cmdsForSequence.add(new CallCommand(tokens, appRunner));
                    state = RESET_TOKEN;
                } else {
                    // add CallCommand as part of ongoing PipeCommand
                    callCmdsForPipe.add(new CallCommand(tokens, appRunner));

                    // add PipeCommand as part of a SequenceCommand
                    cmdsForSequence.add(new PipeCommand(callCmdsForPipe));
                    state = RESET_TOKEN_PIPE;
                }
                break;
            default:
                // encountered a mismatched quote
                throw new ShellException(E_SYNTAX);
        }
        return state;
    }
}
