package sg.edu.nus.comp.cs4218.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.CommandTestUtils;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.util.ErrorConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

/**
 * Integration testing top to bottom, from ShellImpl to the various applications
 */

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ShellImplIntegrationTest {
    private static final String FILE_NOT_EXIST = "fileThatDoesNotExist";
    private static final String FILE_IS_DIR = "src";
    private static final String SHELL_ERROR_MSG = "Expected Shell Error Message.";

    static Stream<Arguments> validRemoveCommand(@TempDir Path tempDir) throws IOException {
        List<String> fileNames = Arrays.asList("1.txt", "2.txt", "3.txt", "4.txt", "5.txt");
        List<String> dirNamesEmpty = Arrays.asList("A", "S", "D", "F", "G");
        List<String> dirNamesPopulated = Arrays.asList("Q", "W", "E", "R", "T");
        List<Path> filePaths = new ArrayList<>();
        List<Path> dirEmptyPaths = new ArrayList<>();
        List<Path> dirPopPaths = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            // Create empty directories
            dirEmptyPaths.add(Files.createTempDirectory(tempDir, dirNamesEmpty.get(i)));

            // Create files in that directory
            Path dirPop = Files.createTempDirectory(tempDir, dirNamesPopulated.get(i));
            dirPopPaths.add(dirPop);
            filePaths.add(Files.createFile(dirPop.resolve(fileNames.get(i))));
        }

        return Stream.of(
                Arguments.of(String.format("rm %s", filePaths.get(0))),
                Arguments.of(String.format("rm %s %s", filePaths.get(1), filePaths.get(2))),
                Arguments.of(String.format("rm -r %s", dirPopPaths.get(1))),
                Arguments.of(String.format("rm -r %s %s %s", dirPopPaths.get(2), dirEmptyPaths.get(0), filePaths.get(3))),
                Arguments.of(String.format("rm -d %s", dirEmptyPaths.get(1))),
                Arguments.of(String.format("rm -d %s %s", dirEmptyPaths.get(2), dirEmptyPaths.get(3))),
                Arguments.of(String.format("rm -rd %s %s", dirEmptyPaths.get(4), dirPopPaths.get(3)))
        );
    }

    static Stream<Arguments> validCopyCommand(@TempDir Path tempDir) throws IOException {

        String fileName1 = "A.txt";
        String fileName2 = "B.txt";
        String folderName1 = "folder1";
        String folderName2 = "folder2";

        Path folderPath = tempDir.resolve(folderName1);
        Path src = Files.createDirectory(folderPath);
        Path filePath = src.resolve(fileName1);
        Path file = Files.createFile(filePath);
        Files.write(file, Arrays.asList("ABCD"));

        Path folderPath2 = tempDir.resolve(folderName2);
        Path dest = Files.createDirectory(folderPath2);
        Path filePath2 = dest.resolve(fileName2);
        Path file2 = Files.createFile(filePath2);

        return Stream.of(
                Arguments.of(String.format("cp %s %s", file, file2)),
                Arguments.of(String.format("cp %s %s", file, dest)),
                Arguments.of(String.format("cp -r %s %s", src, dest))
        );
    }

    static Stream<Arguments> invalidCommandsWithException(@TempDir Path tempDir) {
        return Stream.of(
                Arguments.of("echo < " + FILE_NOT_EXIST, new ShellException(ErrorConstants.E_FILE_NOT_FOUND)),
                Arguments.of("cd " + FILE_NOT_EXIST, new CdException(ErrorConstants.E_FILE_NOT_FOUND)),
                Arguments.of("echo testing | cd " + FILE_NOT_EXIST, new CdException(ErrorConstants.E_FILE_NOT_FOUND)),
                Arguments.of("wc -abcd", new WcException(String.format("%s%s", ErrorConstants.E_ILLEGAL_FLAG, "a"))),
                Arguments.of("echo testing test | grep -testing", new GrepException(ErrorConstants.E_SYNTAX)),
                Arguments.of("ls -abcd", new LsException(String.format("%s%s", ErrorConstants.E_ILLEGAL_FLAG, "a"))),
                Arguments.of("cut -a", new CutException(String.format("%s%s", ErrorConstants.E_ILLEGAL_FLAG, "a"))),
                Arguments.of("cut -b -a", new CutException(ErrorConstants.E_SYNTAX)),
                Arguments.of("cut -cb", new CutException(ErrorConstants.E_SYNTAX)),
                Arguments.of("cut -b" + FILE_NOT_EXIST, new CutException(ErrorConstants.E_SYNTAX)),
                Arguments.of("echo \"baz\" | cut -b" + FILE_NOT_EXIST, new CutException(ErrorConstants.E_SYNTAX)),
                Arguments.of("echo \"baz\" | cut -c 1_9", new CutException(ErrorConstants.E_SYNTAX)),
                Arguments.of("cat -n -a", new CatException(String.format("%s%s", ErrorConstants.E_ILLEGAL_FLAG, "a"))),
                Arguments.of("echo \"baz\" | cat -a", new CatException(String.format("%s%s", ErrorConstants.E_ILLEGAL_FLAG, "a"))),
                Arguments.of("tee -a -b", new TeeException(String.format("%s%s", ErrorConstants.E_ILLEGAL_FLAG, "b"))),
                Arguments.of("sort -nrfa", new SortException(String.format("%s%s", ErrorConstants.E_ILLEGAL_FLAG, "a"))),
                Arguments.of("sort " + FILE_NOT_EXIST, new SortException(String.format("%s: %s", FILE_NOT_EXIST, ErrorConstants.E_FILE_NOT_FOUND))),
                Arguments.of("sort " + tempDir, new SortException(String.format("%s: %s", tempDir.getFileName().toString(), ErrorConstants.E_IS_DIR))),
                Arguments.of("rm -da", new RmException(String.format("%s%s", ErrorConstants.E_ILLEGAL_FLAG, "a"))),
                Arguments.of("rm ", new RmException("usage: rm [-dr] FILES ...")),
                Arguments.of("uniq -c -a", new UniqException(String.format("%s%s", ErrorConstants.E_ILLEGAL_FLAG, "a"))),
                Arguments.of("uniq " + FILE_NOT_EXIST + " " + FILE_NOT_EXIST, new UniqException(FILE_NOT_EXIST + ": " + ErrorConstants.E_FILE_NOT_FOUND)),
                Arguments.of("uniq " + FILE_NOT_EXIST + " " + FILE_NOT_EXIST + " " + FILE_NOT_EXIST, new UniqException("usage: uniq [-c | -d | -D] [INPUT [OUTPUT]] ...")),
                Arguments.of("mv -n -a", new MvException(String.format("%s%s", ErrorConstants.E_ILLEGAL_FLAG, "a"))),
                Arguments.of("mv " + FILE_NOT_EXIST, new MvException(String.format(ErrorConstants.E_MISSING_ARG))),
                Arguments.of("mv " + FILE_NOT_EXIST + " " + FILE_NOT_EXIST, new MvException(String.format("shell: %s", ErrorConstants.E_FILE_NOT_FOUND))),
                Arguments.of("ls " + FILE_NOT_EXIST, String.format("ls: %s: %s", "cannot access '" + FILE_NOT_EXIST + "'", ErrorConstants.E_FILE_NOT_FOUND)),
                Arguments.of("paste -d", new PasteException(String.format("%s%s", ErrorConstants.E_ILLEGAL_FLAG, "d")))
        );
    }

    static Stream<Arguments> validCommandsWithOutput(@TempDir Path tempDir) throws IOException {
        Stream<Arguments> echoCommands = GenCommandUtil.validEchoCommands();
        Stream<Arguments> filePathCommands = GenCommandUtil.validCommandWithFilePaths(tempDir);

        return Stream.concat(echoCommands, filePathCommands);
    }

    // When exceptions are concatenated
    static Stream<Arguments> invalidCommandsWithOutput(@TempDir Path tempDir) {
        return Stream.of(
                Arguments.of("cut -b 1,8 " + FILE_NOT_EXIST, String.format("cut: %s: %s", FILE_NOT_EXIST, ErrorConstants.E_FILE_NOT_FOUND)),
                Arguments.of("cut -b 1,8 " + tempDir, String.format("cut: %s: %s", tempDir.getFileName(), ErrorConstants.E_IS_DIR)),
                Arguments.of("cut -b 1,8 " + tempDir + " " + FILE_NOT_EXIST, String.format("cut: %s: %s" + System.lineSeparator() + "cut: %s: %s", tempDir.getFileName(), ErrorConstants.E_IS_DIR, FILE_NOT_EXIST, ErrorConstants.E_FILE_NOT_FOUND)),

                Arguments.of("cat " + FILE_NOT_EXIST, String.format("cat: %s: %s", FILE_NOT_EXIST, ErrorConstants.E_FILE_NOT_FOUND)),
                Arguments.of("cat " + tempDir, String.format("cat: %s: %s", tempDir.getFileName(), ErrorConstants.E_IS_DIR)),
                Arguments.of("cat " + tempDir + " " + FILE_NOT_EXIST, String.format("cat: %s: %s" + System.lineSeparator() + "cat: %s: %s", tempDir.getFileName(), ErrorConstants.E_IS_DIR, FILE_NOT_EXIST, ErrorConstants.E_FILE_NOT_FOUND)),
                Arguments.of("echo testing | tee " + tempDir, String.format("testing%stee: %s: %s", System.lineSeparator(), tempDir.getFileName(), ErrorConstants.E_IS_DIR)),
                Arguments.of("rm " + FILE_NOT_EXIST, String.format("rm: %s: %s", FILE_NOT_EXIST, ErrorConstants.E_FILE_NOT_FOUND)),

                Arguments.of("rm " + tempDir, String.format("rm: %s: %s", tempDir.getFileName(), ErrorConstants.E_IS_DIR)),
                Arguments.of("paste -s " + FILE_NOT_EXIST, String.format("paste: %s: %s", FILE_NOT_EXIST, ErrorConstants.E_FILE_NOT_FOUND)),
                Arguments.of("paste -s " + tempDir, String.format("paste: %s: %s", tempDir.getFileName(), ErrorConstants.E_IS_DIR)),
                Arguments.of("paste -s " + tempDir + " " + FILE_NOT_EXIST, String.format("paste: %s: %s" + System.lineSeparator() + "paste: %s: %s", tempDir.getFileName(),
                        ErrorConstants.E_IS_DIR, FILE_NOT_EXIST, ErrorConstants.E_FILE_NOT_FOUND))

        );
    }

    static Stream<Arguments> invalidCommandsWithoutPermissionException(@TempDir Path tempDir) throws IOException {
        Path fileNoReadPerm = CommandTestUtils.generateFileWithPermission(false, tempDir);
        Path fileNoWritePerm = CommandTestUtils.generateFileWithPermission(true, tempDir);
        Path folderNoReadPerm = CommandTestUtils.generateFolderWithPermission(false, tempDir);

        return Stream.of(
                Arguments.of("cut -b 1,8 " + fileNoReadPerm, String.format("cut: %s: %s", fileNoReadPerm.getFileName(), ErrorConstants.E_NO_PERM)),
                Arguments.of("cat " + fileNoReadPerm, String.format("cat: %s: %s", fileNoReadPerm.getFileName(), ErrorConstants.E_NO_PERM)),
                Arguments.of("echo  testing | tee " + fileNoWritePerm, String.format("testing%stee: %s: %s", System.lineSeparator(), fileNoWritePerm.getFileName(), ErrorConstants.E_NO_PERM)),
                Arguments.of("wc " + fileNoReadPerm, String.format("%s: %s", fileNoReadPerm.getFileName(), ErrorConstants.E_NO_PERM))
        );
    }

    static Stream<Arguments> invalidCommandsWithPermissionException(@TempDir Path tempDir) throws IOException {
        Path fileNoReadPerm = CommandTestUtils.generateFileWithPermission(false, tempDir);

        return Stream.of(
                Arguments.of("sort " + fileNoReadPerm, new SortException(String.format("%s: %s", fileNoReadPerm.getFileName().toString(), ErrorConstants.E_NO_PERM))),
                Arguments.of("wc -a " + fileNoReadPerm, new WcException(String.format("%s: %s", fileNoReadPerm.getFileName().toString(), ErrorConstants.E_NO_PERM))),
                Arguments.of("paste " + fileNoReadPerm, String.format("paste: %s: %s", fileNoReadPerm.getFileName().toString(), ErrorConstants.E_NO_PERM))
        );
    }

    @ParameterizedTest
    @MethodSource("validCopyCommand")
    void parseAndEvaluate_validCommandCopyFile_noError(String testCommandString, @TempDir Path tempDir)
            throws ShellException, AbstractApplicationException, IOException {
        ShellImpl shell = new ShellImpl();
        OutputStream outputStream = new ByteArrayOutputStream();

        try (MockedStatic<Environment> environmentMS = mockStatic(Environment.class)) {
            environmentMS.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
            shell.parseAndEvaluate(testCommandString, outputStream);

            List<String> files = Arrays.asList(testCommandString.split(" "));

            Path src = files.contains("-r") ? Path.of(files.get(2)) : Path.of(files.get(1));
            Path dst = files.contains("-r") ? Path.of(files.get(3)) : Path.of(files.get(2));
            List<String> fileLines = Arrays.asList("ABCD");

            if (isNotDirectory(src, dst)) {
                List<String> srcLines = Files.readAllLines(src);
                List<String> dstLines = Files.readAllLines(dst);
                assertIterableEquals(srcLines, dstLines);
                assertEquals(srcLines, fileLines);
            } else if (Files.exists(src) && !Files.isDirectory(src) && Files.isDirectory(dst)) {
                Path newFile = dst.resolve(src.toString());
                assertTrue(Files.exists(newFile));
                assertEquals(Files.readAllLines(newFile), fileLines);
            } else {

                Path newFolder = dst.resolve(src.toString());
                assertTrue(Files.exists(newFolder));
                assertTrue(Files.isDirectory(newFolder));

                assertTrue(Files.list(newFolder)
                        .anyMatch(x -> x.getFileName().toString().equals("A.txt")));

                Path copiedFile = newFolder.resolve("A.txt");
                assertTrue(Files.exists(copiedFile));
                assertEquals(Files.readAllLines(copiedFile), fileLines);

            }
        }
    }

    private boolean isNotDirectory(Path src, Path dst) {
        return !(Files.isDirectory(src) || Files.isDirectory(dst));
    }

    @Test
    void main_validCommandDoubleEOF_noError() {
        // Buffer size of BufferedReader
        int bufferSize = 8192;
        String echoOutput = "expected to reach here";
        String expectedOutput = String.format("> \t%d%3$s> %s%3$s> ", 5, echoOutput, System.lineSeparator());
        String testCommandStr = "cat - - | wc -w" + System.lineSeparator();
        String catStdin = "test1 test2 test3 test4 test5" + System.lineSeparator();
        String testCommandStr2 = "echo " + echoOutput + System.lineSeparator();

        // Create buffer to ensure that BufferedReader in ShellImpl will not read catStdin
        Stream<Integer> intStream = IntStream.generate(() -> 32).limit(bufferSize - testCommandStr.length()).boxed();

        // two -1 is used to send two EOF to BufferedReader
        Stream<Integer> intStream2 = IntStream.generate(() -> -1).limit(3).boxed();

        // pad remaining stream with EOFs
        Stream<Integer> intStream3 = IntStream.generate(() -> -1).limit(bufferSize).boxed();

        // create inputStream with commands and buffers
        Stream<Integer> start = Stream.concat(testCommandStr.chars().boxed(), intStream);
        start = Stream.concat(start, catStdin.chars().boxed());
        start = Stream.concat(start, intStream2);
        start = Stream.concat(start, testCommandStr2.chars().boxed());
        start = Stream.concat(start, intStream3);

        InputStream inputStream = new ArrayInputStream(start.mapToInt(x -> x));
        OutputStream outputStream = new ByteArrayOutputStream();

        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));

        ShellImpl.main();

        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    void main_exit_noError() {
        String testCommandString = "exit" + System.lineSeparator();

        InputStream inputStream = new ByteArrayInputStream(testCommandString.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        OutputStream errStream = new ByteArrayOutputStream();
        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errStream));
        SecurityManager original = System.getSecurityManager();
        System.setSecurityManager(new NoExitSecurityManager());

        Throwable exception = assertThrows(RuntimeException.class, ShellImpl::main);
        assertEquals(SHELL_ERROR_MSG, exception.getMessage());

        System.setSecurityManager(original);
    }

    @Test
    void main_inputStreamIOException_noError() {
        String testCommandString = "echo testing" + System.lineSeparator();
        String expectedOutput = String.format("> %s%s> ", "testing", System.lineSeparator());
        InputStream inputStream = new ByteArrayInputStream(testCommandString.getBytes());
        InputStream inputStream2 = new InputStream() {
            public int read() throws IOException {
                throw new IOException();
            }
        };
        SequenceInputStream seqInputStream = new SequenceInputStream(inputStream, inputStream2);
        OutputStream outputStream = new ByteArrayOutputStream();
        System.setIn(seqInputStream);
        System.setOut(new PrintStream(outputStream));
        assertDoesNotThrow(() -> ShellImpl.main());
        assertEquals(expectedOutput, outputStream.toString());
    }

    @ParameterizedTest
    @MethodSource("validCommandsWithOutput")
    void main_validSimpleCommand_noError(String testCommandString, String expectedOutput, @TempDir Path tempDir) {
        InputStream inputStream = new ByteArrayInputStream(testCommandString.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));
        try (MockedStatic<Environment> environmentMS = mockStatic(Environment.class)) {
            environmentMS.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
            ShellImpl.main();
        }
        assertEquals(String.format("> %s%s> ", expectedOutput, System.lineSeparator()), outputStream.toString());

    }

    @ParameterizedTest
    @MethodSource("invalidCommandsWithException")
    void main_invalidSimpleCommand_throwsExceptionContinueRunning(String testCommandString, Throwable exception, @TempDir Path tempDir) {
        InputStream inputStream = new ByteArrayInputStream(testCommandString.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        OutputStream errStream = new ByteArrayOutputStream();
        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errStream));
        try (MockedStatic<Environment> environmentMS = mockStatic(Environment.class)) {
            environmentMS.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
            ShellImpl.main();
            assertEquals(exception.getMessage() + System.lineSeparator(), errStream.toString());
        }
    }

    @ParameterizedTest
    @MethodSource({"validCommandsWithOutput", "invalidCommandsWithOutput"})
    void parseAndEvaluate_validSimpleCommand_noError(String testCommandString, String expectedOutput, @TempDir Path tempDir)
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        ShellImpl shell = new ShellImpl();
        OutputStream outputStream = new ByteArrayOutputStream();
        try (MockedStatic<Environment> environmentMS = mockStatic(Environment.class)) {
            environmentMS.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
            shell.parseAndEvaluate(testCommandString, outputStream);
        }
        assertEquals(expectedOutput + System.lineSeparator(), outputStream.toString());

    }

    @ParameterizedTest
    @MethodSource({"invalidCommandsWithException", "sg.edu.nus.comp.cs4218.impl.GenCommandUtil#validCommandsWithException"})
    void parseAndEvaluate_invalidSimpleCommand_throwsException(String testCommandString, Throwable exception, @TempDir Path tempDir) {
        ShellImpl shell = new ShellImpl();
        OutputStream outputStream = new ByteArrayOutputStream();
        try (MockedStatic<Environment> environmentMS = mockStatic(Environment.class)) {
            environmentMS.when(() -> Environment.getCurrentDirectory()).thenReturn(tempDir.toString());
            assertThrows(exception.getClass(), () -> shell.parseAndEvaluate(testCommandString, outputStream));
        }
    }

    @DisabledOnOs(value = OS.WINDOWS)
    @ParameterizedTest
    @MethodSource({"invalidCommandsWithPermissionException"})
    void parseAndEvaluate_invalidPermissionCommand_throwsException(String testCommandString, Throwable exception, @TempDir Path tempDir) throws IOException {
        parseAndEvaluate_invalidSimpleCommand_throwsException(testCommandString, exception, tempDir);
    }

    @DisabledOnOs(value = OS.WINDOWS)
    @ParameterizedTest
    @MethodSource({"invalidCommandsWithoutPermissionException"})
    void parseAndEvaluate_invalidPermissionCommand_noError(String testCommandString, String expectedOutput, @TempDir Path tempDir) throws IOException, AbstractApplicationException, ShellException {
        parseAndEvaluate_validSimpleCommand_noError(testCommandString, expectedOutput, tempDir);
    }

    @ParameterizedTest
    @MethodSource("sg.edu.nus.comp.cs4218.impl.GenCommandUtil#validCommandsWithIO")
    void parseAndEvaluate_validCommandWithIO_noError(String testCommandString, String output, boolean isInput,
                                                     boolean isOutput, boolean isInputWithValue, @TempDir Path tempDir)
            throws IOException, AbstractApplicationException, ShellException {
        ShellImpl shell = new ShellImpl();
        String inputRedir = "parseAndEvaluate_validCommandWithIO_noError.in";
        String outputRedir = "parseAndEvaluate_validCommandWithIO_noError.out";
        Path inputWithValue = Files.createTempFile(tempDir, null, null);
        Files.createFile(tempDir.resolve(inputRedir));
        OutputStream outputStream = new ByteArrayOutputStream();
        String commandString = testCommandString;
        String expectedOutput = output;
        if (isInputWithValue) {
            String testValue = "This is the test value inside the file" + System.lineSeparator() + "It has two lines.";
            FileWriter fileWriter = new FileWriter(inputWithValue.toFile());
            fileWriter.write(testValue);
            fileWriter.close();
            commandString = String.format(commandString, inputWithValue.getFileName());
            if (expectedOutput == null) {
                expectedOutput = testValue + System.lineSeparator();
            }
        } else {
            if (isInput && isOutput) {
                commandString = String.format(commandString, inputRedir, outputRedir);
            } else if (isInput || isOutput) {
                commandString = String.format(commandString, isInput ? inputRedir : outputRedir);
            }
        }


        try (MockedStatic<Environment> environmentMS = mockStatic(Environment.class)) {
            environmentMS.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
            shell.parseAndEvaluate(commandString, outputStream);

            assertEquals(expectedOutput, outputStream.toString());
        }
    }

    @ParameterizedTest
    @MethodSource("sg.edu.nus.comp.cs4218.impl.GenCommandUtil#validCommandsToFile")
    void parseAndEvaluate_validCommandsToFile_fileExists(String testCommandString, String expectedOutput, boolean isAppend, @TempDir Path tempDir) throws IOException, AbstractApplicationException, ShellException {
        ShellImpl shell = new ShellImpl();
        String inputFile = "parseAndEvaluate_validCommandsToFile_fileExists.txt";
        Path inputWithValue = Files.createTempFile(tempDir, null, null);
        Files.createFile(tempDir.resolve(inputFile));
        OutputStream outputStream = new ByteArrayOutputStream();
        String commandString = testCommandString;

        String testValue = "This is the test value inside the file" + System.lineSeparator() + "It has two lines.";
        FileWriter fileWriter = new FileWriter(inputWithValue.toFile());
        fileWriter.write(testValue);
        fileWriter.close();

        String exOutput = expectedOutput;
        if (isAppend) {
            exOutput = testValue + expectedOutput;
        }

        commandString = String.format(commandString, inputWithValue);

        try (MockedStatic<Environment> environmentMS = mockStatic(Environment.class)) {
            environmentMS.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
            shell.parseAndEvaluate(commandString, outputStream);

            String actual = String.join(STRING_NEWLINE, Files.readAllLines(inputWithValue));

            assertEquals(exOutput, actual);
        }
    }

    @ParameterizedTest
    @MethodSource("validRemoveCommand")
    void parseAndEvaluate_validCommandRemoveFile_noError(String testCommandString, @TempDir Path tempDir)
            throws ShellException, AbstractApplicationException, IOException {
        ShellImpl shell = new ShellImpl();
        OutputStream outputStream = new ByteArrayOutputStream();

        try (MockedStatic<Environment> environmentMS = mockStatic(Environment.class)) {
            environmentMS.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
            shell.parseAndEvaluate(testCommandString, outputStream);

            List<String> files = Arrays.asList(testCommandString.split(" "));
            int lastIndex = IntStream.range(0, files.size())
                    .filter(i -> files.get(i).contains("-"))
                    .reduce((a, b) -> b)
                    .orElse(-1);

            if (lastIndex != -1) {
                files.subList(lastIndex, files.size() - 1);

                for (String file : files) {
                    Path filePath = Path.of(file);
                    assertFalse(Files.exists(filePath));
                }
                assertEquals("", outputStream.toString());
            }
        }
    }

    class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {
            // Unused
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new RuntimeException(SHELL_ERROR_MSG);
        }
    }

    class ArrayInputStream extends InputStream {
        private final int[] arr;
        private int counter;

        ArrayInputStream(IntStream stream) {
            arr = stream.toArray();
            counter = 0;
        }

        @Override
        public int read() {
            int index = counter;
            counter++;
            return arr[index];
        }
    }
}
