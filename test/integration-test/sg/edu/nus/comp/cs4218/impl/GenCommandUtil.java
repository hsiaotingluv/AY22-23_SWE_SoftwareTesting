package sg.edu.nus.comp.cs4218.impl;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.Arguments;
import sg.edu.nus.comp.cs4218.CommandTestUtils;
import sg.edu.nus.comp.cs4218.exception.ExitException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class GenCommandUtil {

    private GenCommandUtil() {

    }

    public static Stream<Arguments> validCommandsWithIO() {
        return Stream.of(
                Arguments.of("echo testing > %1$s | cat %1$s", "testing" + System.lineSeparator(), false, true, false),
                Arguments.of("echo testing > %1$s; cat < %1$s", "testing" + System.lineSeparator(), false, true, false),
                Arguments.of("echo testing > %1$s; wc -lw < %1$s > %2$s; cat %2$s", String.format("\t%d\t%d", 1, 1) + System.lineSeparator(), true, true, false),
                Arguments.of("echo < %1$s; rm %1$s", System.lineSeparator(), true, false, false),
                Arguments.of("cat < %s", null, false, false, true),
                Arguments.of("paste < %s", null, false, false, true),
                Arguments.of("grep two - < %s", "It has two lines." + System.lineSeparator(), false, false, true)
        );
    }

    public static Stream<Arguments> validCommandsToFile() {
        return Stream.of(
                Arguments.of("echo testing | tee -a %s", "testing", true),
                Arguments.of("echo testing | tee %s", "testing", false)
        );
    }

    public static Stream<Arguments> validCommandsWithException() {
        return Stream.of(
                Arguments.of("exit", new ExitException("0"))
        );
    }

    public static Stream<Arguments> validEchoCommands() {
        return Stream.of(
                Arguments.of("echo A B C", "A B C"),
                Arguments.of("echo \"A*B*C\"", "A*B*C"),
                Arguments.of("echo ``", ""),
                Arguments.of("echo \"''\"", "''"),
                Arguments.of("echo testing | cut -c 1-5", "testi"),
                Arguments.of("echo testing | cut -b 1-5 -", "testi"),
                Arguments.of("echo \"That 2£ is affordable.\"  | cut -c 1,7 ", "T£"),
                Arguments.of("echo \"That 2£ is affordable.\" | cut -b 1,7 ", "T�"),
                Arguments.of("echo testing | cat", "testing"),
                Arguments.of("echo \"testing\" | cat", "testing"),
                Arguments.of("echo \"testing\" | cat -", "testing"),
                Arguments.of("echo \"testing\" \"testing\" | cat - -", "testing testing"),
                Arguments.of("echo testing ; echo testing", "testing" + System.lineSeparator() + "testing"),
                Arguments.of("echo testing | wc -cl", String.format("\t%d\t%d", 1, File.separatorChar == '/' ? 8 : 9)),
                Arguments.of("echo testing test | wc -clw", String.format("\t%d\t%d\t%d", 1, 2, File.separatorChar == '/' ? 13 : 14)),
                Arguments.of("echo testing test | grep testing", "testing test"),
                Arguments.of("echo TESTING test | grep -i testing", "TESTING test"),
                Arguments.of("echo TESTING -test | grep -- -test", "TESTING -test"),
                Arguments.of("echo testing test | tee", "testing test"),
                Arguments.of("echo \"testing\" \"test\" | tee - -", "testing test"),
                Arguments.of("echo \"This is space:`echo test | grep test`.\"", "This is space:test."),
                Arguments.of("echo \"This is space:' '.\"", "This is space:' '."),
                Arguments.of("echo abcdef | wc -w ; echo testing", String.format("\t%d", 1) + System.lineSeparator() + "testing"),
                Arguments.of("echo `echo \"‘quote is not interpreted as special character’\"`", "‘quote is not interpreted as special character’")
        );
    }

    public static Stream<Arguments> validCommandWithFilePaths(@TempDir Path tempDir) throws IOException {
        List<String> fileNames = Arrays.asList("fileSingleLine.txt",
                "fileMultipleLine.txt",
                "fileNumbersAndChars.txt",
                "fileWithDups.txt");

        List<String> inputs = Arrays.asList("That 2£ is affordable.",
                CommandTestUtils.concatenateLinesWithSeparator("That 2£ is affordable.", "Today is Tuesday."),
                CommandTestUtils.concatenateLinesWithSeparator("10", "apple", "1", "Banana", "2", "DURIAN", "+", "cherry"),
                CommandTestUtils.concatenateLinesWithSeparator("hello", "hello", "world", "hello", "hello", "welcome", "welcome", "welcome"));
        List<Path> filePaths = CommandTestUtils.populateTestFiles(fileNames, inputs, tempDir);
        String fileSingleLine = filePaths.get(0).toString();
        String fileMultipleLine = filePaths.get(1).toString();
        String fileNumberChars = filePaths.get(2).toString();
        String fileWithDups = filePaths.get(3).toString();

        Stream<Arguments> filePathCommands = Stream.of(
                Arguments.of("cut -c 1-7 " + fileSingleLine, "That 2£"),
                Arguments.of("cut -c 1,7 " + fileMultipleLine, CommandTestUtils.concatenateLinesWithSeparator("T£", "Ti")),
                Arguments.of("cut -b 1,7 " + fileSingleLine, "T�"),
                Arguments.of("cut -b 1-7 " + fileMultipleLine, CommandTestUtils.concatenateLinesWithSeparator("That 2�", "Today i")),

                Arguments.of("cat " + fileSingleLine, "That 2£ is affordable."),
                Arguments.of("cat " + fileMultipleLine, CommandTestUtils.concatenateLinesWithSeparator("That 2£ is affordable.", "Today is Tuesday.")),
                Arguments.of("cat " + fileSingleLine + " " + fileMultipleLine, CommandTestUtils.concatenateLinesWithSeparator("That 2£ is affordable.", "That 2£ is affordable.", "Today is Tuesday.")),
                Arguments.of("cat -n " + fileSingleLine, "\t1 That 2£ is affordable."),
                Arguments.of("cat -n " + fileMultipleLine, CommandTestUtils.concatenateLinesWithSeparator("\t1 That 2£ is affordable.", "\t2 Today is Tuesday.")),
                Arguments.of("cat -n " + fileSingleLine + " " + fileMultipleLine, CommandTestUtils.concatenateLinesWithSeparator("\t1 That 2£ is affordable.", "\t1 That 2£ is affordable.", "\t2 Today is Tuesday.")),

                Arguments.of("sort " + fileNumberChars, CommandTestUtils.concatenateLinesWithSeparator("+", "1", "10", "2", "Banana", "DURIAN", "apple", "cherry")),
                Arguments.of("sort -n " + fileNumberChars, CommandTestUtils.concatenateLinesWithSeparator("+", "1", "2", "10", "Banana", "DURIAN", "apple", "cherry")),
                Arguments.of("sort -r " + fileNumberChars, CommandTestUtils.concatenateLinesWithSeparator("cherry", "apple", "DURIAN", "Banana", "2", "10", "1", "+")),
                Arguments.of("sort -f " + fileNumberChars, CommandTestUtils.concatenateLinesWithSeparator("+", "1", "10", "2", "apple", "Banana", "cherry", "DURIAN")),
                Arguments.of("sort -nr " + fileNumberChars, CommandTestUtils.concatenateLinesWithSeparator("cherry", "apple", "DURIAN", "Banana", "10", "2", "1", "+")),
                Arguments.of("sort -nf " + fileNumberChars, CommandTestUtils.concatenateLinesWithSeparator("+", "1", "2", "10", "apple", "Banana", "cherry", "DURIAN")),
                Arguments.of("sort -rf " + fileNumberChars, CommandTestUtils.concatenateLinesWithSeparator("DURIAN", "cherry", "Banana", "apple", "2", "10", "1", "+"))
        );
        Stream<Arguments> commands = Stream.concat(filePathCommands, folderAndDupCommands(fileSingleLine, fileMultipleLine, fileNumberChars, fileWithDups, tempDir));
        return Stream.concat(commands, dupCommands(fileSingleLine, fileMultipleLine, fileWithDups, fileNumberChars));
    }

    static Stream<Arguments> folderAndDupCommands(String fileSingleLine, String fileMultipleLine, String fileNumberChars, String fileWithDups, @TempDir Path tempDir) throws IOException {
        List<String> folderNames = Arrays.asList("folder1",
                "folder2");
        List<Path> folderPaths = CommandTestUtils.populateTestFolders(folderNames, tempDir);

        return Stream.of(
                Arguments.of("wc " + fileSingleLine, CommandTestUtils.concatenateLinesWithTab(String.valueOf(1), String.valueOf(4), String.valueOf(File.separatorChar == '/' ? 24 : 25)) + " " + fileSingleLine),
                Arguments.of("wc " + fileMultipleLine, CommandTestUtils.concatenateLinesWithTab(String.valueOf(2), String.valueOf(7), String.valueOf(File.separatorChar == '/' ? 42 : 44)) + " " + fileMultipleLine),
                Arguments.of("wc -c " + fileSingleLine, CommandTestUtils.concatenateLinesWithTab(String.valueOf(File.separatorChar == '/' ? 24 : 25)) + " " + fileSingleLine),
                Arguments.of("wc -l " + fileSingleLine, CommandTestUtils.concatenateLinesWithTab(String.valueOf(1)) + " " + fileSingleLine),
                Arguments.of("wc -w " + fileSingleLine, CommandTestUtils.concatenateLinesWithTab(String.valueOf(4)) + " " + fileSingleLine),
                Arguments.of("wc -c -l " + fileSingleLine, CommandTestUtils.concatenateLinesWithTab(String.valueOf(1), String.valueOf(File.separatorChar == '/' ? 24 : 25)) + " " + fileSingleLine),
                Arguments.of("wc -lw " + fileMultipleLine, CommandTestUtils.concatenateLinesWithTab(String.valueOf(2), String.valueOf(7)) + " " + fileMultipleLine),
                Arguments.of("wc -w -c " + fileNumberChars, CommandTestUtils.concatenateLinesWithTab(String.valueOf(8), String.valueOf(File.separatorChar == '/' ? 36 : 44)) + " " + fileNumberChars),
                Arguments.of("wc -lwc " + fileNumberChars, CommandTestUtils.concatenateLinesWithTab(String.valueOf(8), String.valueOf(8), String.valueOf(File.separatorChar == '/' ? 36 : 44)) + " " + fileNumberChars),

                Arguments.of("ls " + tempDir, CommandTestUtils.concatenateLinesWithSeparator(".." + File.separator + tempDir.getFileName() + ":",
                        "fileMultipleLine.txt", "fileNumbersAndChars.txt", "fileSingleLine.txt", "fileWithDups.txt", "folder1", "folder2")),
                Arguments.of("ls -R " + tempDir, CommandTestUtils.concatenateLinesWithSeparator(".." + File.separator + tempDir.getFileName() + ":",
                        "fileMultipleLine.txt", "fileNumbersAndChars.txt", "fileSingleLine.txt", "fileWithDups.txt", "folder1", "folder2",
                        "", ".." + File.separator + tempDir.getFileName() + File.separator + folderNames.get(0) + ":",
                        "", ".." + File.separator + tempDir.getFileName() + File.separator + folderNames.get(1) + ":")),

                Arguments.of("uniq " + fileWithDups, CommandTestUtils.concatenateLinesWithSeparator("hello", "world", "hello", "welcome")),
                Arguments.of("uniq -c " + fileWithDups, CommandTestUtils.concatenateLinesWithSeparator("\t2 hello", "\t1 world", "\t2 hello", "\t3 welcome")),
                Arguments.of("uniq -d " + fileWithDups, CommandTestUtils.concatenateLinesWithSeparator("hello", "hello", "welcome")),
                Arguments.of("uniq -D " + fileWithDups, CommandTestUtils.concatenateLinesWithSeparator("hello", "hello", "hello", "hello", "welcome", "welcome", "welcome")),
                Arguments.of("uniq -d -D " + fileWithDups, CommandTestUtils.concatenateLinesWithSeparator("hello", "hello", "hello", "hello", "welcome", "welcome", "welcome")),
                Arguments.of("uniq -c -d " + fileWithDups, CommandTestUtils.concatenateLinesWithSeparator("\t2 hello", "\t2 hello", "\t3 welcome")),
                Arguments.of("uniq -c -D " + fileWithDups, CommandTestUtils.concatenateLinesWithSeparator("\t2 hello", "\t2 hello", "\t2 hello", "\t2 hello", "\t3 welcome", "\t3 welcome", "\t3 welcome")),
                Arguments.of("uniq -c -D -d " + fileWithDups, CommandTestUtils.concatenateLinesWithSeparator("\t2 hello", "\t2 hello", "\t2 hello", "\t2 hello", "\t3 welcome", "\t3 welcome", "\t3 welcome"))
        );
    }

    static Stream<Arguments> dupCommands(String fileSingleLine, String fileMultipleLine, String fileWithDups, String fileNumberChars) {
        return Stream.of(
                Arguments.of("grep -H \"That\" " + fileSingleLine, fileSingleLine + ": That 2£ is affordable."),
                Arguments.of("grep -i \"heLLo\" " + fileWithDups, "hello" + System.lineSeparator() + "hello" + System.lineSeparator() + "hello" + System.lineSeparator() + "hello"),
                Arguments.of("grep -c \"affordable\" " + fileSingleLine, "1"),
                Arguments.of("grep -H -i \"heLLo\" " + fileWithDups, fileWithDups + ": hello" + System.lineSeparator() + fileWithDups + ": hello" + System.lineSeparator() + fileWithDups + ": hello" + System.lineSeparator() + fileWithDups + ": hello"),
                Arguments.of("grep -H -c \"hello\" " + fileWithDups, fileWithDups + ": 4"),
                Arguments.of("grep -i -c \"welcomE\" " + fileWithDups, "3"),
                Arguments.of("grep -H -i -c \"welcomE\" " + fileWithDups, fileWithDups + ": 3"),

                Arguments.of("paste " + fileNumberChars, CommandTestUtils.concatenateLinesWithSeparator("10", "apple", "1", "Banana", "2", "DURIAN", "+", "cherry")),
                Arguments.of("paste -s " + fileNumberChars, "10\tapple\t1\tBanana\t2\tDURIAN\t+\tcherry"),

                Arguments.of("paste -s " + fileMultipleLine + " " + fileNumberChars,
                        "That 2£ is affordable.\tToday is Tuesday." + System.lineSeparator() + "10\tapple\t1\tBanana\t2\tDURIAN\t+\tcherry"
                ),
                Arguments.of("paste " + fileMultipleLine + " " + fileNumberChars,
                        "That 2£ is affordable.\t10" + System.lineSeparator() +
                                "Today is Tuesday.\tapple" + System.lineSeparator() +
                                "\t1" + System.lineSeparator() + "\tBanana" + System.lineSeparator() + "\t2" + System.lineSeparator() + "\tDURIAN" + System.lineSeparator() + "\t+" + System.lineSeparator() + "\tcherry"
                )
        );
    }
}
