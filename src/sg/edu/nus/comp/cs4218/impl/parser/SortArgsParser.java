package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class SortArgsParser extends ArgsParser {
    public static final char FLAG_FIRST_W_NUM = 'n';
    public static final char FLAG_REV_ORDER = 'r';
    public static final char FLAG_CASE_IGNORE = 'f';

    public SortArgsParser() {
        super();
        legalFlags.add(FLAG_FIRST_W_NUM);
        legalFlags.add(FLAG_REV_ORDER);
        legalFlags.add(FLAG_CASE_IGNORE);
    }

    public List<String> getFiles() {
        return nonFlagArgs;
    }

    public boolean isFirstWordNumber() {
        return flags.contains(FLAG_FIRST_W_NUM);
    }

    public boolean isReverseOrder() {
        return flags.contains(FLAG_REV_ORDER);
    }

    public boolean isCaseIndependent() {
        return flags.contains(FLAG_CASE_IGNORE);
    }

    public boolean isReadingFromStdin() {
        return nonFlagArgs.contains("-") || nonFlagArgs.isEmpty();
    }
}
