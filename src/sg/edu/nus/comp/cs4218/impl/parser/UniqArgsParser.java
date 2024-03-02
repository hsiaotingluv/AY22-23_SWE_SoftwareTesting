package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.List;

public class UniqArgsParser extends ArgsParser {
    public static final char FLAG_COUNT = 'c';
    public static final char FLAG_REPEATED = 'd';
    public static final char FLAG_ALL_REPEATED = 'D';

    public UniqArgsParser() {
        super();
        legalFlags.add(FLAG_COUNT);
        legalFlags.add(FLAG_REPEATED);
        legalFlags.add(FLAG_ALL_REPEATED);
    }

    @Override
    public void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        if (nonFlagArgs.size() > 2) {
            throw new InvalidArgsException("usage: uniq [-c | -d | -D] [INPUT [OUTPUT]] ...");
        }
    }

    public Boolean isCount() {
        return flags.contains(FLAG_COUNT);
    }

    public Boolean isRepeated() {
        return flags.contains(FLAG_REPEATED);
    }

    public Boolean isAllRepeated() {
        return flags.contains(FLAG_ALL_REPEATED);
    }

    public List<String> getFiles() {
        return nonFlagArgs;
    }

    public boolean hasOutputFile() {
        return nonFlagArgs.size() == 2;
    }

    public boolean hasInputFile() {
        return nonFlagArgs.size() > 0;
    }
}