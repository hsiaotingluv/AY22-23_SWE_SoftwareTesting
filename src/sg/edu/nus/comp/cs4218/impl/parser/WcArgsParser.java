package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.List;

public class WcArgsParser extends ArgsParser {

    public static final char FLAG_BYTES_OPTION = 'c';
    public static final char FLAG_LINES_OPTION = 'l';
    public static final char FLAG_WORDS_OPTION = 'w';
    private boolean lines, words, bytes;

    public WcArgsParser() {
        super();
        legalFlags.add(FLAG_BYTES_OPTION);
        legalFlags.add(FLAG_LINES_OPTION);
        legalFlags.add(FLAG_WORDS_OPTION);
    }

    @Override
    public void parse(String... args) throws InvalidArgsException {
        super.parse(args);
        if (flags.isEmpty()) {
            flags.add('l');
            flags.add('c');
            flags.add('w');
        }
    }

    public boolean isLines() {
        return flags.contains(FLAG_LINES_OPTION);
    }

    public boolean isWords() {
        return flags.contains(FLAG_WORDS_OPTION);
    }

    public boolean isBytes() {
        return flags.contains(FLAG_BYTES_OPTION);
    }

    public List<String> getFiles() {
        return nonFlagArgs;
    }

    public boolean isReadingFromStdinOnly() {
        return nonFlagArgs.size() == 0;
    }
}
