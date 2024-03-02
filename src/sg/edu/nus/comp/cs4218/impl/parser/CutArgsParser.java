package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_ILLEGAL_FLAG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_SYNTAX;

public class CutArgsParser extends ArgsParser {
    private final static char FLAG_IS_BYTE = 'b';
    private final static char FLAG_IS_CHARACTER = 'c';
    private final List<int[]> ranges = new ArrayList<>();

    public CutArgsParser() {
        super();
        legalFlags.add(FLAG_IS_BYTE);
        legalFlags.add(FLAG_IS_CHARACTER);
    }

    @Override
    public void parse(String... args) throws InvalidArgsException {
        boolean isOption = true, isList = true;
        for (String arg : args) {
            if (isOption) {
                if (arg.length() == 2) {
                    flags.add(arg.charAt(1));
                    isOption = false;
                    continue;
                }
                throw new InvalidArgsException(E_SYNTAX);
            } else if (isList) {
                createRangeArr(arg);
                isList = false;
            } else {
                nonFlagArgs.add(arg);
            }
        }
        validateArgs();
    }

    @Override
    public void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        if (flags.size() <= 0) {
            throw new InvalidArgsException(E_SYNTAX);
        } else if (notValidFlags()) {
            throw new InvalidArgsException(E_ILLEGAL_FLAG);
        } else if (getRanges().isEmpty()) {
            throw new InvalidArgsException(E_SYNTAX);
        }
    }

    public void createRangeArr(String arg) throws InvalidArgsException {
        if (!StringUtils.isBlank(arg) && arg.matches("^[0-9](.*[0-9])?$")) {
            int dashPos = arg.indexOf('-');
            int commaPos = arg.indexOf(',');
            if (StringUtils.isNumber(arg) && Integer.parseInt(arg) != 0) {
                ranges.add(new int[]{Integer.parseInt(arg), Integer.parseInt(arg)});
            } else if (dashPos != -1 && arg.length() > 1) {
                int start = Integer.parseInt(arg.substring(0, dashPos));
                int end = Integer.parseInt(arg.substring(dashPos + 1));
                if (((start < end || start == end) && (start != 0 && end != 0))) {
                    ranges.add(new int[]{start, end});
                }
            } else if (commaPos != -1) {
                int pos = 0;
                int nextCommaPos;
                while (true) {
                    String currDigit = arg.substring(pos, commaPos);
                    pos = commaPos + 1;
                    if (!StringUtils.isNumber(currDigit) || Integer.parseInt(currDigit) == 0) {
                        throw new InvalidArgsException(E_SYNTAX);
                    }
                    int currDigitToInt = Integer.parseInt(currDigit);
                    ranges.add(new int[]{currDigitToInt, currDigitToInt});
                    nextCommaPos = arg.indexOf(',', commaPos + 1);
                    if (nextCommaPos < 0) {
                        break;
                    }
                    commaPos = nextCommaPos;
                }
                int lastDigitToInt = Integer.parseInt(arg.substring(commaPos + 1));
                if (lastDigitToInt == 0) {
                    throw new InvalidArgsException(E_SYNTAX);
                }
                ranges.add(new int[]{lastDigitToInt, lastDigitToInt});
            }
        } else {
            throw new InvalidArgsException(E_SYNTAX);
        }
    }

    public Boolean notValidFlags() {
        return !isBytePo() && !isCharPo();
    }

    public Boolean isBytePo() {
        return flags.contains(FLAG_IS_BYTE);
    }

    public Boolean isCharPo() {
        return flags.contains(FLAG_IS_CHARACTER);
    }

    public List<int[]> getRanges() {
        return ranges;
    }

    public String[] getFiles() {
        return nonFlagArgs.toArray(new String[0]);
    }

    public boolean isReadingFromStdin() {
        return nonFlagArgs.contains("-") || nonFlagArgs.isEmpty();
    }
}
