package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.util.ErrorConstants;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Integer.MAX_VALUE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_SYNTAX;

/**
 * Every application's parser should extend this class to encapsulate their own parsing details and
 * information.
 */
public class ArgsParser {

    protected Set<Character> flags;
    protected Set<Character> legalFlags;
    protected List<String> nonFlagArgs;

    protected ArgsParser() {
        flags = new HashSet<>();
        legalFlags = new HashSet<>();
        nonFlagArgs = new ArrayList<>();
    }

    /**
     * Separates command flags from non-flag arguments given a tokenized command.
     *
     * @param args
     */
    public void parse(String... args) throws InvalidArgsException {
        boolean isOption = true, isEndOfOption = false;
        int maxFlag, dash = MAX_VALUE;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--") && !isEndOfOption) {
                isEndOfOption = true;
                if (args[i].length() == 2) {
                    continue;
                }
                nonFlagArgs.add(args[i].substring(2));
            } else {
                if (isOption && !isEndOfOption) {
                    if ((StringUtils.isBlank(args[i]) || args[i].charAt(0) != '-')) {
                        isOption = false;
                        nonFlagArgs.add(args[i]);
                    } else {
                        if (args[i].length() <= 1) {
                            dash = i;
                            nonFlagArgs.add(args[i]);
                        }
                        for (int j = 1; j < args[i].length(); j++) {
                            flags.add(args[i].charAt(j));
                        }
                        maxFlag = i;
                        if (dash < maxFlag) {
                            throw new InvalidArgsException(E_SYNTAX);
                        }
                    }
                } else {
                    if (!isEndOfOption && (args[i].charAt(0) == '-' && args[i].length() > 1)) {
                        throw new InvalidArgsException(E_SYNTAX);
                    }
                    nonFlagArgs.add(args[i]);
                }
            }
        }

        validateArgs();
    }

    /**
     * Checks for the existence of illegal flags. Presence of any illegal flags would result in a
     * non-empty set after subtracting the set of legal flags from the set of parsed flags.
     * <p>
     * Note on usage: Do not call this method directly in any application.
     *
     * @throws InvalidArgsException
     */
    protected void validateArgs() throws InvalidArgsException {
        Set<Character> illegalFlags = new HashSet<>(flags);
        illegalFlags.removeAll(legalFlags);

        // construct exception message with the first illegal flag encountered
        for (Character flag : illegalFlags) {
            String exceptionMessage = ErrorConstants.E_ILLEGAL_FLAG + flag;
            throw new InvalidArgsException(exceptionMessage);
        }
    }
}
