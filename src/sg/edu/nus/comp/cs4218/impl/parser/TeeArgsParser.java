package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class TeeArgsParser extends ArgsParser {
    public static final char FLAG_APPEND = 'a';

    public TeeArgsParser() {
        super();
        legalFlags.add(FLAG_APPEND);
    }

    public boolean isAppendToFile() {
        return flags.contains(FLAG_APPEND);
    }

    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
