package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_MISSING_ARG;

public class MvArgsParser extends ArgsParser {

    public static final char FLAG_NO_OVERWRITE = 'n';

    public MvArgsParser() {
        super();
        legalFlags.add(FLAG_NO_OVERWRITE);
    }

    @Override
    public void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        try {
            if (nonFlagArgs.size() < 2) {
                throw new InvalidArgsException(E_MISSING_ARG);
            }
        } catch (InvalidArgsException e) {
            throw new InvalidArgsException(e.getMessage(), e);
        }
    }

    public boolean isOverwrite() {
        return !flags.contains(FLAG_NO_OVERWRITE);
    }

    public List<String> getFiles() {
        return nonFlagArgs.subList(0, nonFlagArgs.size() - 1);
    }

    public String getDestDir() {
        return nonFlagArgs.get(nonFlagArgs.size() - 1);
    }

}
