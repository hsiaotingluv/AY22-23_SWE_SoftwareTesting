package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.app.helper.UniqHelper;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class UniqApplicationTest {
    private UniqApplication uniqApplication;
    private List<String> inputArrayFileA;
    private String fileAContents;
    private ByteArrayInputStream testInputStream;
    private ByteArrayOutputStream testOutputStream;

    private String[] argsStdInFilesOut;
    private String[] argsFilesAsInOut;
    private String[] argsStdAsInOut;
    private String[] argsFilesInStdOut;

    private String[] files;

    private String expectedOutPrefix;
    private String expOutNoPrefix;
    private String expectedOutDupAll;
    private String expOutDupOnce;

    @BeforeEach
    public void init() {
        uniqApplication = new UniqApplication();

        // Constants
        fileAContents = "Hello World" + System.lineSeparator() + "Hello World" + System.lineSeparator() +
                "Alice" + System.lineSeparator() + "Alice" + System.lineSeparator() +
                "Bob" + System.lineSeparator() + "Alice" + System.lineSeparator() + "Bob";
        inputArrayFileA = Arrays.asList(fileAContents.split(System.lineSeparator()));

        argsStdInFilesOut = new String[]{"-c", "-", "1.txt"};
        argsFilesAsInOut = new String[]{"-c", "1.txt", "2.txt"};
        argsStdAsInOut = new String[]{"-c"};
        argsFilesInStdOut = new String[]{"-c", "1.txt"};

        files = new String[]{"1.txt", "2.txt"};

        testInputStream = new ByteArrayInputStream(fileAContents.getBytes());
        testOutputStream = new ByteArrayOutputStream();

        expOutNoPrefix = "Hello World" + System.lineSeparator() + "Alice" + System.lineSeparator() +
                "Bob" + System.lineSeparator() + "Alice" + System.lineSeparator() + "Bob";
        expectedOutPrefix = "2 Hello World" + System.lineSeparator() + "2 Alice" + System.lineSeparator() +
                "1 Bob" + System.lineSeparator() + "1 Alice" + System.lineSeparator() + "1 Bob"; // -c
        expOutDupOnce = "2 Hello World" + System.lineSeparator() + "2 Alice"; // -d
        expectedOutDupAll = "2 Hello World" + System.lineSeparator() + "2 Hello World" + System.lineSeparator() +
                "2 Alice" + System.lineSeparator() + "2 Alice"; // -D
    }

    @Test
    public void run_emptyStdout_throwsException() {
        UniqException exception = assertThrows(UniqException.class,
                () -> uniqApplication.run(null, null, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void run_oneInputFileSpecified_usesFilesAndWritesToStdoutSuccessfully() throws UniqException {
        System.setOut(new PrintStream(testOutputStream));

        UniqApplication uniqAppSpy = spy(uniqApplication);
        UniqArgsParser mockUniqParser = mock(UniqArgsParser.class);

        doReturn(mockUniqParser)
                .when(uniqAppSpy)
                .parseArgs(argsFilesInStdOut);

        when(mockUniqParser.isCount()).thenReturn(true);
        when(mockUniqParser.isRepeated()).thenReturn(false);
        when(mockUniqParser.hasInputFile()).thenReturn(true);
        when(mockUniqParser.isAllRepeated()).thenReturn(false);
        when(mockUniqParser.getFiles()).thenReturn(Collections.singletonList(files[0]));

        doReturn(expectedOutPrefix)
                .when(uniqAppSpy)
                .uniqFromFile(eq(true), eq(false), eq(false), eq(files[0]), eq(null));

        assertDoesNotThrow(
                () -> uniqAppSpy.run(argsFilesInStdOut, testInputStream, testOutputStream)
        );

        verify(uniqAppSpy).parseArgs(eq(argsFilesInStdOut));
        verify(mockUniqParser).isCount();
        verify(mockUniqParser).isRepeated();
        verify(mockUniqParser).isAllRepeated();
        verify(uniqAppSpy).uniqFromFile(eq(true), eq(false), eq(false), eq(files[0]), eq(null));

        String expected = expectedOutPrefix;

        assertEquals(expected, testOutputStream.toString().trim());
    }

    @Test
    public void run_noFilesSpecified_usesStdinAndWritesToStdoutSuccessfully() throws UniqException {
        System.setOut(new PrintStream(testOutputStream));

        UniqApplication uniqAppSpy = spy(uniqApplication);
        UniqArgsParser mockUniqParser = mock(UniqArgsParser.class);

        doReturn(mockUniqParser)
                .when(uniqAppSpy)
                .parseArgs(argsStdAsInOut);

        when(mockUniqParser.isCount()).thenReturn(true);
        when(mockUniqParser.isRepeated()).thenReturn(false);
        when(mockUniqParser.isAllRepeated()).thenReturn(false);

        doReturn(expectedOutPrefix)
                .when(uniqAppSpy)
                .uniqFromStdin(eq(true), eq(false), eq(false), eq(testInputStream), eq(null));

        assertDoesNotThrow(
                () -> uniqAppSpy.run(argsStdAsInOut, testInputStream, testOutputStream)
        );

        verify(uniqAppSpy).parseArgs(eq(argsStdAsInOut));
        verify(mockUniqParser).isCount();
        verify(mockUniqParser).isRepeated();
        verify(mockUniqParser).isAllRepeated();
        verify(uniqAppSpy).uniqFromStdin(eq(true), eq(false), eq(false), eq(testInputStream), eq(null));

        String expected = expectedOutPrefix;

        assertEquals(expected, testOutputStream.toString().trim());
    }

    @Test
    public void run_oneOutputFileSpecified_usesStdinAndWritesToFileSuccessfully() throws UniqException {
        UniqApplication uniqAppSpy = spy(uniqApplication);
        UniqArgsParser mockUniqParser = mock(UniqArgsParser.class);

        doReturn(mockUniqParser)
                .when(uniqAppSpy)
                .parseArgs(argsStdInFilesOut);

        when(mockUniqParser.isCount()).thenReturn(true);
        when(mockUniqParser.isRepeated()).thenReturn(false);
        when(mockUniqParser.isAllRepeated()).thenReturn(false);

        doReturn(expectedOutPrefix)
                .when(uniqAppSpy)
                .uniqFromStdin(eq(true), eq(false), eq(false), eq(testInputStream), eq(files[1]));

        assertDoesNotThrow(
                () -> uniqAppSpy.run(argsStdInFilesOut, testInputStream, testOutputStream)
        );

        verify(uniqAppSpy).parseArgs(eq(argsStdInFilesOut));
        verify(mockUniqParser).isCount();
        verify(mockUniqParser).isRepeated();
        verify(mockUniqParser).isAllRepeated();
        verify(uniqAppSpy).uniqFromStdin(eq(true), eq(false), eq(false), eq(testInputStream), eq(null));
    }

    @Test
    public void run_twoFilesSpecified_usesFileAndWritesToFileSuccessfully(@TempDir Path tempDir) throws UniqException {
        UniqApplication uniqAppSpy = spy(uniqApplication);
        UniqArgsParser mockUniqParser = mock(UniqArgsParser.class);

        doReturn(mockUniqParser)
                .when(uniqAppSpy)
                .parseArgs(argsFilesAsInOut);

        when(mockUniqParser.isCount()).thenReturn(true);
        when(mockUniqParser.isRepeated()).thenReturn(false);
        when(mockUniqParser.hasInputFile()).thenReturn(true);
        when(mockUniqParser.isAllRepeated()).thenReturn(false);
        when(mockUniqParser.getFiles()).thenReturn(Arrays.asList(files));

        doReturn(expectedOutPrefix)
                .when(uniqAppSpy)
                .uniqFromFile(eq(true), eq(false), eq(false), eq(files[0]), eq(files[1]));

        try (MockedStatic<Environment> mEnv = mockStatic(Environment.class)) {
            mEnv.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
            assertDoesNotThrow(
                    () -> uniqAppSpy.run(argsFilesAsInOut, testInputStream, testOutputStream)
            );
        }

        verify(uniqAppSpy).parseArgs(eq(argsFilesAsInOut));
        verify(mockUniqParser).isCount();
        verify(mockUniqParser).isRepeated();
        verify(mockUniqParser).isAllRepeated();
        verify(uniqAppSpy).uniqFromFile(eq(true), eq(false), eq(false), eq(files[0]), eq(files[1]));
    }

    @Test
    public void run_uniqFromStdinThrowsException_throwsUniqException() throws UniqException {
        UniqApplication uniqAppSpy = spy(uniqApplication);
        UniqArgsParser mockUniqParser = mock(UniqArgsParser.class);

        doReturn(mockUniqParser)
                .when(uniqAppSpy)
                .parseArgs(argsStdAsInOut);

        when(mockUniqParser.isCount()).thenReturn(true);
        when(mockUniqParser.isRepeated()).thenReturn(false);
        when(mockUniqParser.isAllRepeated()).thenReturn(false);

        doThrow(UniqException.class)
                .when(uniqAppSpy)
                .uniqFromStdin(eq(true), eq(false), eq(false), eq(testInputStream), eq(null));

        assertThrows(UniqException.class,
                () -> uniqAppSpy.run(argsStdAsInOut, testInputStream, testOutputStream)
        );

        verify(uniqAppSpy).parseArgs(eq(argsStdAsInOut));
        verify(mockUniqParser).isCount();
        verify(mockUniqParser).isRepeated();
        verify(mockUniqParser).isAllRepeated();
        verify(uniqAppSpy).uniqFromStdin(eq(true), eq(false), eq(false), eq(testInputStream), eq(null));
    }

    @Test
    public void run_uniqFromFileThrowsException_throwsUniqException() throws UniqException {
        UniqApplication uniqAppSpy = spy(uniqApplication);
        UniqArgsParser mockUniqParser = mock(UniqArgsParser.class);

        doReturn(mockUniqParser)
                .when(uniqAppSpy)
                .parseArgs(argsFilesInStdOut);

        when(mockUniqParser.isCount()).thenReturn(true);
        when(mockUniqParser.hasInputFile()).thenReturn(true);
        when(mockUniqParser.isRepeated()).thenReturn(false);
        when(mockUniqParser.isAllRepeated()).thenReturn(false);
        when(mockUniqParser.getFiles()).thenReturn(Collections.singletonList(files[0]));

        doThrow(UniqException.class)
                .when(uniqAppSpy)
                .uniqFromFile(eq(true), eq(false), eq(false), eq(files[0]), eq(null));

        assertThrows(UniqException.class,
                () -> uniqAppSpy.run(argsFilesInStdOut, testInputStream, testOutputStream)
        );

        verify(uniqAppSpy).parseArgs(eq(argsFilesInStdOut));
        verify(mockUniqParser).isCount();
        verify(mockUniqParser).isRepeated();
        verify(mockUniqParser).isAllRepeated();
        verify(mockUniqParser).getFiles();
        verify(uniqAppSpy).uniqFromFile(eq(true), eq(false), eq(false), eq(files[0]), eq(null));
    }

    @Test
    public void run_stdOutWritesThrowsIOException_throwsUniqException() throws UniqException {
        UniqApplication uniqAppSpy = spy(uniqApplication);
        UniqArgsParser mockUniqParser = mock(UniqArgsParser.class);

        OutputStream exOutputStream = new OutputStream() {
            @Override
            public void write(int bytes) throws IOException {
                throw new IOException();
            }

            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };

        doReturn(mockUniqParser)
                .when(uniqAppSpy)
                .parseArgs(argsFilesInStdOut);

        when(mockUniqParser.isCount()).thenReturn(true);
        when(mockUniqParser.hasInputFile()).thenReturn(true);
        when(mockUniqParser.isRepeated()).thenReturn(false);
        when(mockUniqParser.isAllRepeated()).thenReturn(false);
        when(mockUniqParser.getFiles()).thenReturn(Collections.singletonList(files[0]));

        doReturn(expectedOutPrefix)
                .when(uniqAppSpy)
                .uniqFromFile(eq(true), eq(false), eq(false), eq(files[0]), eq(null));

        UniqException exception = assertThrows(UniqException.class,
                () -> uniqAppSpy.run(argsFilesInStdOut, testInputStream, exOutputStream)
        );

        verify(uniqAppSpy).parseArgs(eq(argsFilesInStdOut));
        verify(mockUniqParser).isCount();
        verify(mockUniqParser).isRepeated();
        verify(mockUniqParser).hasInputFile();
        verify(mockUniqParser).isAllRepeated();
        verify(mockUniqParser).getFiles();
        verify(uniqAppSpy).uniqFromFile(eq(true), eq(false), eq(false), eq(files[0]), eq(null));

        assertTrue(exception.getMessage().contains(E_WRITE_STREAM));
    }

    @Test
    public void uniqFromFile_validFileInput_returnsOutputWithDuplicateLinesRemoved(@TempDir Path tempDir) throws UniqException, IOException {
        UniqApplication uniqAppSpy = spy(uniqApplication);
        String fileName = "uniqFromFile_validFileInput_returnsOutputWithDuplicateLinesRemoved.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, inputArrayFileA);
        FileInputStream exFileInputStream = new FileInputStream(filePath.toString());

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.openInputStream(fileName))
                    .thenReturn(exFileInputStream);

            try (MockedStatic<UniqHelper> mUniqHelper = mockStatic(UniqHelper.class, CALLS_REAL_METHODS)) {
                mUniqHelper.when(() -> UniqHelper.removeDuplicateLines(eq(exFileInputStream), eq(true))).thenReturn(Arrays.asList(expectedOutPrefix.split(System.lineSeparator())));
                mUniqHelper.when(() -> UniqHelper.validatePath(any())).thenAnswer(i -> null);

                ioUtils.when(() -> IOUtils.closeInputStream(exFileInputStream))
                        .thenAnswer(i -> null);

                String actual = assertDoesNotThrow(
                        () -> uniqAppSpy.uniqFromFile(false, false, false, fileName, files[1])
                );

                ioUtils.verify(() -> IOUtils.openInputStream(fileName));
                mUniqHelper.verify(() -> UniqHelper.removeDuplicateLines(eq(exFileInputStream), eq(true)));
                ioUtils.verify(() -> IOUtils.closeInputStream(exFileInputStream));

                String expected = expOutNoPrefix + System.lineSeparator();

                assertTrue(Files.exists(filePath));
                assertEquals(expected, actual);
            }
        }

        exFileInputStream.close();
    }

    @Test
    public void uniqFromFile_nullFileName_throwsUniqException() {
        UniqException exception = assertThrows(UniqException.class,
                () -> uniqApplication.run(null, null, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void uniqFromFile_invalidFileName_throwsUniqException(@TempDir Path tempDir) {
        UniqApplication uniqAppSpy = spy(uniqApplication);
        String fileName = "uniqFromFile_invalidFileName_throwsUniqException.txt";
        Path filePath = tempDir.resolve(fileName);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            try (MockedStatic<Environment> mEnv = mockStatic(Environment.class)) {
                try (MockedStatic<UniqHelper> unused = mockStatic(UniqHelper.class)) {
                    mEnv.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
                    ioUtils.when(() -> IOUtils.openInputStream(fileName))
                            .thenThrow(new ShellException(E_FILE_NOT_FOUND));

                    UniqException exception = assertThrows(UniqException.class, () ->
                            uniqAppSpy.uniqFromFile(false, false, false, fileName, files[1])
                    );

                    ioUtils.verify(() -> IOUtils.openInputStream(fileName));

                    assertTrue(exception.getMessage().contains(E_FILE_NOT_FOUND));
                }
            }
        }
    }

    @Test
    public void uniqFromStdin_validStdinInput_returnsOutputWithDuplicateLinesRemoved(@TempDir Path tempDir) {
        UniqApplication uniqAppSpy = spy(uniqApplication);

        try (MockedStatic<UniqHelper> mUniqHelper = mockStatic(UniqHelper.class, CALLS_REAL_METHODS)) {
            mUniqHelper.when(() -> UniqHelper.removeDuplicateLines(eq(testInputStream), eq(true))).thenReturn(Arrays.asList(expectedOutPrefix.split(System.lineSeparator())));

            try (MockedStatic<Environment> mEnv = mockStatic(Environment.class)) {
                mEnv.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());
                String output = assertDoesNotThrow(
                        () -> uniqAppSpy.uniqFromStdin(false, false, false, testInputStream, files[1])
                ).trim();

                mUniqHelper.verify(() -> UniqHelper.removeDuplicateLines(eq(testInputStream), eq(true)));

                assertEquals(expOutNoPrefix, output);
            }
        }
    }

    @Test
    public void uniqFromStdin_emptyStdin_throwsUniqException() {
        UniqException exception = assertThrows(UniqException.class,
                () -> uniqApplication.run(null, null, null)
        );

        assertTrue(exception.getMessage().contains(E_NULL_POINTER));
    }

    @Test
    public void removeDuplicateLines_validInputStreamAndCount_returnsCorrectOutput() {

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArrayFileA);

            List<String> actual = assertDoesNotThrow(
                    () -> UniqHelper.removeDuplicateLines(testInputStream, true)
            );

            String expected = expectedOutPrefix;
            List<String> expectedList = Arrays.asList(expected.split(System.lineSeparator()));

            assertEquals(expectedList, actual);
        }
    }

    @Test
    public void removeDuplicateLines_validInputStreamWithoutCount_returnsCorrectOutput() {
        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArrayFileA);

            List<String> actual = assertDoesNotThrow(
                    () -> UniqHelper.removeDuplicateLines(testInputStream, false)
            );

            List<String> expectedList = Arrays.asList(expOutNoPrefix.split(System.lineSeparator()));
            assertEquals(expectedList, actual);
        }
    }

    @Test
    public void removeDuplicateLines_invalidInputStream_throwsShellException() {
        assertThrowsExactly(UniqException.class, () -> {
            InputStream inputStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }
            };
            UniqHelper.removeDuplicateLines(inputStream, false);
        });
    }

    @Test
    public void getDuplicateLines_validInputStreamRepeatOnce_returnsCorrectOutput() throws UniqException {
        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArrayFileA);
            List<String> result = UniqHelper.removeDuplicateLines(testInputStream, true);
            List<String> actual = assertDoesNotThrow(
                    () -> UniqHelper.getDuplicateLines(result, true, false)
            );

            List<String> expectedList = Arrays.asList(expOutDupOnce.split(System.lineSeparator()));
            assertEquals(expectedList, actual);
        }
    }

    @Test
    public void getDuplicateLines_validInputStreamRepeatAll_returnsCorrectOutput() throws UniqException {
        UniqApplication uniqAppSpy = spy(uniqApplication);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.getLinesFromInputStream(testInputStream))
                    .thenReturn(inputArrayFileA);
            List<String> result = UniqHelper.removeDuplicateLines(testInputStream, true);
            List<String> actual = assertDoesNotThrow(
                    () -> UniqHelper.getDuplicateLines(result, false, true)
            );

            List<String> expectedList = Arrays.asList(expectedOutDupAll.split(System.lineSeparator()));
            assertEquals(expectedList, actual);
        }
    }

    @Test
    public void getDuplicateLines_invalidInputStream_throwsShellException() {
        assertThrowsExactly(UniqException.class, () -> {
            InputStream inputStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }
            };
            List<String> result = UniqHelper.removeDuplicateLines(inputStream, false);
            UniqHelper.getDuplicateLines(result, false, false);
        });
    }

    @Test
    public void writeToFile_validInputs_writesSuccessfully(@TempDir Path tempDir) throws IOException {
        String fileName = "writeToFile_validInputs_writesSuccessfully.txt";
        Path filePath = tempDir.resolve(fileName);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(filePath.toString()))
                    .thenReturn(filePath);

            assertDoesNotThrow(
                    () -> UniqHelper.writeToFile(fileAContents, filePath.toString())
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(filePath.toString()));

            List<String> actual = Files.readAllLines(filePath);
            assertTrue(Files.exists(filePath));
            assertEquals(inputArrayFileA, actual);
        }
    }

    @Test
    public void writeToFile_fileIsADirectory_throwsUniqException(@TempDir Path tempDir) {
        String fileName = "writeToFile_fileIsADirectory_throwsUniqException.txt";

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(tempDir);

            UniqException exception = assertThrows(UniqException.class,
                    () -> UniqHelper.writeToFile(fileAContents, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));

            assertTrue(exception.getMessage().contains(E_IS_DIR));
        }
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    public void writeToFile_fileCannotBeWrittenTo_throwsUniqException(@TempDir Path tempDir) throws IOException {
        UniqApplication uniqAppSpy = spy(uniqApplication);
        String fileName = "writeToFile_fileIsADirectory_throwsUniqException.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.createFile(filePath);
        Files.setPosixFilePermissions(filePath, new HashSet<>(Collections.singletonList(PosixFilePermission.OWNER_READ)));

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {
            ioUtils.when(() -> IOUtils.resolveFilePath(fileName))
                    .thenReturn(filePath);

            UniqException exception = assertThrows(UniqException.class,
                    () -> UniqHelper.writeToFile(fileAContents, fileName)
            );

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName));

            assertTrue(exception.getMessage().contains(E_NO_PERM));
        }
    }
}