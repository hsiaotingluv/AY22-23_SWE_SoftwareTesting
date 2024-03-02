package sg.edu.nus.comp.cs4218.impl.app.helper;

import sg.edu.nus.comp.cs4218.exception.WcException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;

public final class WcHelper {
    private static final int LINES_INDEX = 0;
    private static final int WORDS_INDEX = 1;
    private static final int BYTES_INDEX = 2;

    private WcHelper() {
    }

    /**
     * Returns string containing the number of lines, words, and bytes based on data in input.
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param count    long array containing the count of lines, words, bytes
     * @param filename String of filename if available
     * @throws WcException
     */
    public static String getCountLine(Boolean isLines, Boolean isWords, Boolean isBytes, long[] count, String... filename) throws WcException {
        try {
            // Format all output: " %7d %7d %7d %s"
            // Output in the following order: lines words bytes filename
            StringBuilder result = new StringBuilder();
            if (isLines) {
                result.append(CHAR_TAB);
                result.append(count[0]);
            }
            if (isWords) {
                result.append(CHAR_TAB);
                result.append(count[1]);
            }
            if (isBytes) {
                result.append(CHAR_TAB);
                result.append(count[2]);
            }
            if (filename.length > 0) {
                result.append(String.format(" %s", filename));
            }

            return result.toString();

        } catch (Exception e) {
            throw new WcException(e.getMessage(), e);
        }
    }

    /**
     * Returns string containing the number of lines, words, and bytes based on data in input.
     *
     * @param splitList List of String of counted results
     * @param isBytes   Boolean option to count the number of Bytes
     * @param isLines   Boolean option to count the number of lines
     * @param isWords   Boolean option to count the number of words
     * @param total     long array containing the count of lines, words, bytes
     * @throws WcException
     */
    public static long[] updateCountTotal(List<String> splitList, Boolean isLines, Boolean isWords, Boolean isBytes, long... total) throws WcException {
        try {
            if (splitList.size() == 1) {
                if (isLines) {
                    total[0] += Integer.parseInt(splitList.get(0));
                }
                if (isWords) {
                    total[1] += Integer.parseInt(splitList.get(0));
                }
                if (isBytes) {
                    total[2] += Integer.parseInt(splitList.get(0));
                }
            } else if (splitList.size() == 2) {
                if (isLines && isWords) {
                    total[0] += Integer.parseInt(splitList.get(0));
                    total[1] += Integer.parseInt(splitList.get(1));
                } else if (isLines && isBytes) {
                    total[0] += Integer.parseInt(splitList.get(0));
                    total[2] += Integer.parseInt(splitList.get(1));
                } else {
                    total[1] += Integer.parseInt(splitList.get(0));
                    total[2] += Integer.parseInt(splitList.get(1));
                }
            } else {
                total[0] += Integer.parseInt(splitList.get(0));
                total[1] += Integer.parseInt(splitList.get(1));
                total[2] += Integer.parseInt(splitList.get(2));
            }
            return total;

        } catch (Exception e) {
            throw new WcException(e.getMessage(), e);
        }
    }

    /**
     * Returns array containing the number of lines, words, and bytes based on data in InputStream.
     *
     * @param input An InputStream
     * @throws IOException
     */
    public static long[] getCountReport(InputStream input) throws WcException {
        if (input == null) {
            throw new WcException(E_NULL_POINTER);
        }

        try {
            long[] result = new long[3]; // lines, words, bytes
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int inRead = 0;
            boolean inWord = false;
            while ((inRead = input.read(data, 0, data.length)) != -1) {
                for (int i = 0; i < inRead; ++i) {
                    if (Character.isWhitespace(data[i])) {
                        // Use <newline> character here. (Ref: UNIX)
                        if (data[i] == '\n') {
                            ++result[LINES_INDEX];
                        }
                        if (inWord) {
                            ++result[WORDS_INDEX];
                        }

                        inWord = false;
                    } else {
                        inWord = true;
                    }
                }
                result[BYTES_INDEX] += inRead;
                buffer.write(data, 0, inRead);
            }
            buffer.flush();
            if (inWord) {
                ++result[WORDS_INDEX]; // To handle last word
            }

            return result;

        } catch (Exception e) {
            throw new WcException(e.getMessage(), e);
        }
    }

    public static boolean invalidNode(File node, List<String> result) {
        if (!node.exists()) {
            result.add(node.getName() + ": " + E_FILE_NOT_FOUND);
            return true;
        }
        if (node.isDirectory()) {
            result.add(node.getName() + ": " + E_IS_DIR);
            return true;
        }
        if (!node.canRead()) {
            result.add(node.getName() + ": " + E_NO_PERM);
            return true;
        }
        return false;
    }

}
