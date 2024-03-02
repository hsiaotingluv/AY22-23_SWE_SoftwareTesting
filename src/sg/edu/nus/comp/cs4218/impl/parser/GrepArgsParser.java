package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.util.ErrorConstants;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class GrepArgsParser extends ArgsParser {
    private final static char FLAG_IS_INSEN = 'i';

    private final static char FLAG_IS_COUNT = 'c';

    private final static char FLAG_IS_PRINT = 'H';
    private final static int INDEX_PATTERN = 0;
    private final static int INDEX_FILES = 1;

    public GrepArgsParser() {
        super();
        legalFlags.add(FLAG_IS_COUNT);
        legalFlags.add(FLAG_IS_INSEN);
        legalFlags.add(FLAG_IS_PRINT);
    }

    @Override
    public void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        try {
            String userInputPattern = getPattern();
            Pattern.compile(userInputPattern);
        } catch (InvalidArgsException e) {
            throw new InvalidArgsException(e.getMessage(), e);
        } catch (PatternSyntaxException exception) {
            throw new InvalidArgsException(ErrorConstants.E_BAD_REGEX
                    + ": " + exception.getDescription(), exception);
        }
    }

    public Boolean isCaseInsensitive() {
        return flags.contains(FLAG_IS_INSEN);
    }

    public Boolean isCount() {
        return flags.contains(FLAG_IS_COUNT);
    }

    public Boolean isPrintFileName() {
        return flags.contains(FLAG_IS_PRINT);
    }

    public String getPattern() throws InvalidArgsException {
        try {
            if (StringUtils.isBlank(nonFlagArgs.get(0))) {
                throw new InvalidArgsException(ErrorConstants.E_NO_REGEX);
            } else {
                return nonFlagArgs.get(INDEX_PATTERN);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidArgsException(ErrorConstants.E_NO_REGEX, e);
        }
    }

    public String[] getFileNames() {
        return nonFlagArgs.size() <= 1 ? new String[]{"-"} : nonFlagArgs.subList(INDEX_FILES, nonFlagArgs.size())
                .toArray(new String[0]);
    }
}
