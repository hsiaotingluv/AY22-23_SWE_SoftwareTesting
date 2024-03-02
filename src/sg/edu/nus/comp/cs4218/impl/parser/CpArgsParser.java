package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_MISSING_ARG;

public class CpArgsParser extends ArgsParser {
    public static final char FLAG_RECURSIVE_1 = 'r';
    public static final char FLAG_RECURSIVE_2 = 'R';

    public CpArgsParser() {
        super();
        legalFlags.add(FLAG_RECURSIVE_1);
        legalFlags.add(FLAG_RECURSIVE_2);
    }

    @Override
    public void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        try {
            if (isRecursive() && nonFlagArgs.size() < 2) {
                throw new InvalidArgsException(E_MISSING_ARG);
            }

            if (!isRecursive() && nonFlagArgs.size() < 2) {
                throw new InvalidArgsException(E_MISSING_ARG);
            }
        } catch (InvalidArgsException e) {
            throw new InvalidArgsException(e.getMessage(), e);
        }
    }

    public boolean isRecursive() {
        return flags.contains(FLAG_RECURSIVE_1) || flags.contains(FLAG_RECURSIVE_2);
    }

    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
