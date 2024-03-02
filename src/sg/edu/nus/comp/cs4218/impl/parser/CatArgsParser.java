package sg.edu.nus.comp.cs4218.impl.parser;

public class CatArgsParser extends ArgsParser {
    public static final char FLAG_LINE_NUMBER = 'n';

    public CatArgsParser() {
        super();
        legalFlags.add(FLAG_LINE_NUMBER);
    }

    public Boolean isLineNumber() {
        return flags.contains(FLAG_LINE_NUMBER);
    }

    public String[] getFiles() {
        if (nonFlagArgs.isEmpty()) {
            nonFlagArgs.add("-");
        }
        return nonFlagArgs.toArray(new String[0]);
    }
}
