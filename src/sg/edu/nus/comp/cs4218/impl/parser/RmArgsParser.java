package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.util.List;

public class RmArgsParser extends ArgsParser {
    public static final char FLAG_RECURSIVE = 'r';
    public static final char FLAG_EMPTY_FOLDER = 'd';

    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_RECURSIVE);
        legalFlags.add(FLAG_EMPTY_FOLDER);
    }

    @Override
    public void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        if (nonFlagArgs.size() == 0 || (StringUtils.isBlank(nonFlagArgs.get(0)))) {
            throw new InvalidArgsException("usage: rm [-dr] FILES ...");
        }
    }

    public Boolean isRecursive() {
        return flags.contains(FLAG_RECURSIVE);
    }

    public Boolean isEmptyFolder() {
        return flags.contains(FLAG_EMPTY_FOLDER);
    }

    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
