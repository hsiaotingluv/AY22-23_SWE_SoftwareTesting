package sg.edu.nus.comp.cs4218.impl.app.helper;

import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public final class PasteHelper {
    private PasteHelper() {
    }

    public static String concatenateLines(Boolean isSerial, List<List<String>> totalLines) {
        StringBuilder result = new StringBuilder();

        if (totalLines.isEmpty()) {
            return result.toString();
        }

        if (isSerial) {
            for (int i = 0; i < totalLines.size(); i++) {
                String line = "";
                if (!totalLines.get(i).isEmpty()) {
                    line = String.join("\t", totalLines.get(i));
                    result.append(line);
                }
                if (i < totalLines.size() - 1 && !line.isEmpty()) {
                    result.append(STRING_NEWLINE);
                }
            }
        } else {
            int maxFileLength = totalLines.stream().map(List::size).max(Integer::compareTo).get();

            List<List<String>> totalLst = new ArrayList<>();

            for (int i = 0; i < maxFileLength; i++) {
                List<String> lst = new ArrayList<>();
                for (int j = 0; j < totalLines.size(); j++) {
                    List<String> fileLines = totalLines.get(j);
                    if (i <= fileLines.size() - 1) {
                        lst.add(fileLines.get(i));
                    } else {
                        lst.add("");
                    }
                }
                totalLst.add(lst);
            }
            result = new StringBuilder(concatenateLines(true, totalLst));
        }

        return result.toString();
    }


}
