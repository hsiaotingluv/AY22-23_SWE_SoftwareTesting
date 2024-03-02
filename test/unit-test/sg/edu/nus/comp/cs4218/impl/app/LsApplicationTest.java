package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.app.helper.LsHelper;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;

/**
 * Provides unit tests of LsApplication
 * <p>
 * Positive test cases:
 * - ls list all contents recursively and sorted
 * - ls list all contents non recursively with no folder
 * - ls list all contents recursively with no folder
 * - ls list all contents in current working directory
 * - ls list all contents with buildResult
 * - ls list all contents with formatContents
 * - ls resolve paths from List of String to List of Path
 * - ls resolve path from String to Path
 * - ls get relative path to current directory
 * <p>
 * Negative test cases:
 * - ls with null arguments (throws null argument exception)
 * - ls with exception thrown by listFolderContent (throws Ls exception)
 * - ls with invalid directory and no recursive for buildResult (throws InvalidDirectoryException)
 */
class LsApplicationTest {
    private static final Path BASE_PATH = Paths.get(Environment.currentDirectory);
    private static final String TEST_PATH = "lsFolder";
    private static final String FOLDER_NAME = "TestFolder";
    private static final String FILE_NAME = "TestFile";
    private static LsApplication lsApplication;
    private static ByteArrayInputStream testInputStream;
    private static ByteArrayOutputStream testOutputStream;
    private static Path testPath; // lsFolder
    private static Path testFolderPath; // lsFolder/TestFolder
    private static Path testFilePath; // lsFolder/TestFile
    private static Path nestedFolderPath; // lsFolder/TestFolder/TestFolder
    private static Path nestedFilePath; // lsFolder/TestFolder/TestFile
    private static String[] argsRecurSort;
    private String[] expectedDir;
    private String[] args;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        lsApplication = new LsApplication();

        testPath = Files.createTempDirectory(BASE_PATH, TEST_PATH);
        testFolderPath = Files.createTempDirectory(testPath, FOLDER_NAME);
        testFilePath = Files.createTempFile(testPath, FILE_NAME, "");
        nestedFolderPath = Files.createTempDirectory(testFolderPath, FOLDER_NAME);
        nestedFilePath = Files.createTempFile(testFolderPath, FILE_NAME, "");

        testPath.toFile().deleteOnExit();
        testFolderPath.toFile().deleteOnExit();
        testFilePath.toFile().deleteOnExit();
        nestedFolderPath.toFile().deleteOnExit();
        nestedFilePath.toFile().deleteOnExit();

        argsRecurSort = new String[]{"-R -X", TEST_PATH};

        testInputStream = new ByteArrayInputStream(TEST_PATH.getBytes());
        testOutputStream = new ByteArrayOutputStream();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        Environment.currentDirectory = BASE_PATH.toString();

        Files.walk(testPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * build expected result for test cases
     */
    private String buildExpectedResult(Boolean isRecursive, Boolean hasFolder) {
        StringBuilder result = new StringBuilder();

        if (hasFolder) {
            result.append(FILE_NAME);
            result.append(StringUtils.STRING_NEWLINE);
            result.append(FOLDER_NAME);
            result.append(StringUtils.STRING_NEWLINE);

            if (isRecursive) {
                result.append(StringUtils.STRING_NEWLINE);
                result.append("." + File.separator + FOLDER_NAME + ":" + System.lineSeparator());
                result.append(FILE_NAME);
                result.append(StringUtils.STRING_NEWLINE);
                result.append(FOLDER_NAME);
            }
        } else {
            String line = "." + File.separator + ":" + System.lineSeparator();
            result.append(line);
            result.append(FILE_NAME);
        }
        return result.toString().strip();
    }

    @BeforeEach
    public void init() throws Exception {
        lsApplication = new LsApplication();

        expectedDir = new String[]{"folder"};
        args = new String[]{"-R", "-X", ""};

        testInputStream = new ByteArrayInputStream("".getBytes());
        testOutputStream = new ByteArrayOutputStream();

        Environment.setCurrentDirectory(testPath.toString());
    }

    @AfterEach
    void reset() throws ShellException {
        Environment.setCurrentDirectory(BASE_PATH.toString());
    }

    /**
     * ls with null arguments (throws null argument exception)
     */
    @Test
    public void run_emptyStdout_throwsException() {
        LsException exception = assertThrows(LsException.class,
                () -> lsApplication.run(null, null, null)
        );

        String expected = E_NULL_POINTER;
        String actual = exception.getMessage();

        assertTrue(actual.contains(expected));
    }

    /**
     * ls list all contents recursively and sorted
     */
    @Test
    public void run_listAllRecursiveSuccessfully_throwsNoException() throws Exception {
        LsApplication lsAppSpy = spy(lsApplication);
        LsArgsParser mockLsArgsParser = mock(LsArgsParser.class);

        doReturn(mockLsArgsParser)
                .when(lsAppSpy)
                .parseArgs(args);

        when(mockLsArgsParser.isSortByExt()).thenReturn(true);
        when(mockLsArgsParser.isRecursive()).thenReturn(true);
        when(mockLsArgsParser.getDirectories()).thenReturn(Arrays.asList(expectedDir));

        doReturn(buildExpectedResult(true, true))
                .when(lsAppSpy)
                .listFolderContent(eq(true), eq(true), eq(String.join("", expectedDir)));

        assertDoesNotThrow(
                () -> lsAppSpy.run(args, testInputStream, testOutputStream)
        );

        verify(lsAppSpy).parseArgs(eq(args));
        verify(mockLsArgsParser, times(1)).isSortByExt();
        verify(mockLsArgsParser, times(1)).isRecursive();
        verify(mockLsArgsParser, times(2)).getDirectories();
        verify(lsAppSpy).listFolderContent(eq(true), eq(true), eq(String.join("", expectedDir)));

        String expected = buildExpectedResult(true, true);
        expected = expected.concat(System.lineSeparator());

        assertEquals(expected, testOutputStream.toString());
    }

    /**
     * ls with exception thrown by listFolderContent (throws Ls exception)
     */
    @Test
    public void run_listThrowsException_throwsLsException() throws Exception {
        LsApplication lsAppSpy = spy(lsApplication);
        LsArgsParser mockLsArgsParser = mock(LsArgsParser.class);

        doReturn(mockLsArgsParser)
                .when(lsAppSpy)
                .parseArgs(args);

        when(mockLsArgsParser.isSortByExt()).thenReturn(true);
        when(mockLsArgsParser.isRecursive()).thenReturn(true);
        when(mockLsArgsParser.getDirectories()).thenReturn(Arrays.asList(expectedDir));

        doThrow(LsException.class)
                .when(lsAppSpy)
                .listFolderContent(eq(true), eq(true), eq(String.join("", expectedDir)));

        assertThrows(LsException.class,
                () -> lsAppSpy.run(args, testInputStream, testOutputStream)
        );

        verify(lsAppSpy).parseArgs(eq(args));
        verify(mockLsArgsParser, times(1)).isSortByExt();
        verify(mockLsArgsParser, times(1)).isRecursive();
        verify(mockLsArgsParser, times(2)).getDirectories();
        verify(lsAppSpy).listFolderContent(eq(true), eq(true), eq(String.join("", expectedDir)));
    }

    /**
     * ls list all contents non recursively with no folder
     */
    @Test
    public void listFolderContent_noRecursiveAndNoFolder_throwsNoException() throws Exception {
        LsApplication lsAppSpy = spy(lsApplication);

        doReturn(buildExpectedResult(false, false))
                .when(lsAppSpy)
                .listCwdContent(eq(true));

        String[] arr = {};
        String output = assertDoesNotThrow(
                () -> lsAppSpy.listFolderContent(false, true, arr)
        );

        verify(lsAppSpy).listFolderContent(false, true, arr);
        verify(lsAppSpy).listCwdContent(eq(true));

        String expected = buildExpectedResult(false, false);

        assertEquals(expected, output);
    }

    /**
     * ls list all contents recursively with no folder
     */
    @Test
    public void listFolderContent_withRecursiveAndNoFolder_throwsNoException() {

        try (MockedStatic<LsHelper> mLsHelper = mockStatic(LsHelper.class, CALLS_REAL_METHODS)) {
            mLsHelper.when(() -> LsHelper.resolvePaths(testPath.toString())).thenReturn(List.of(nestedFilePath));
            mLsHelper.when(() -> LsHelper.buildResult(List.of(nestedFilePath), true, true)).thenReturn(buildExpectedResult(true, false));

            String[] arr = {};
            String output = assertDoesNotThrow(
                    () -> lsApplication.listFolderContent(true, true, arr)
            );

            mLsHelper.verify(() -> LsHelper.resolvePaths(testPath.toString()), times(2));
            mLsHelper.verify(() -> LsHelper.buildResult(List.of(nestedFilePath), true, true), times(2));

            String expected = buildExpectedResult(true, false);

            assertEquals(expected, output);
        }

    }

    /**
     * ls list all contents in current working directory
     */
    @Test
    public void listCwdContent_listContentsSuccessfully_throwsNoException() {
        List<Path> expectedPaths = List.of(testFilePath, testFolderPath);
        try (MockedStatic<LsHelper> mLsHelper = mockStatic(LsHelper.class, CALLS_REAL_METHODS)) {
            mLsHelper.when(() -> LsHelper.formatContents(expectedPaths, true)).thenReturn(buildExpectedResult(false, true));

            String output = assertDoesNotThrow(
                    () -> lsApplication.listCwdContent(true)
            );

            mLsHelper.verify(() -> LsHelper.formatContents(expectedPaths, true));

            String expected = buildExpectedResult(false, true);

            assertEquals(expected, output.replaceAll("\\d", ""));
        }
    }

    /**
     * ls list all contents with buildResult
     */
    @Test
    public void buildResult_listAllContentsSuccessfully_throwsNoException() {
        List<Path> contents = List.of(nestedFilePath, nestedFolderPath);
        String formatted = FILE_NAME + System.lineSeparator() + FOLDER_NAME;
        try (MockedStatic<LsHelper> mLsHelper = mockStatic(LsHelper.class, CALLS_REAL_METHODS)) {
            mLsHelper.when(() -> LsHelper.getContents(testFolderPath)).thenReturn(contents);
            mLsHelper.when(() -> LsHelper.formatContents(contents, true)).thenReturn(formatted);
            mLsHelper.when(() -> LsHelper.getRelativeToCwd(testFolderPath)).thenReturn(Path.of(""));


            String output = assertDoesNotThrow(
                    () -> LsHelper.buildResult(List.of(testFolderPath), false, true)
            );

            mLsHelper.verify(() -> LsHelper.getContents(testFolderPath));
            mLsHelper.verify(() -> LsHelper.formatContents(contents, true));
            mLsHelper.verify(() -> LsHelper.getRelativeToCwd(testFolderPath));

            String expected = "." + File.separator + ":" + System.lineSeparator();
            expected += buildExpectedResult(false, true);
            expected += StringUtils.STRING_NEWLINE;
            expected += StringUtils.STRING_NEWLINE;

            assertEquals(expected, output);
        }
    }

    /**
     * ls with invalid directory and no recursive for buildResult
     */
    @Test
    public void buildResult_notRecursiveInvalidDirectory_throwsException() {

        try (MockedStatic<LsHelper> mLsHelper = mockStatic(LsHelper.class, CALLS_REAL_METHODS)) {
            mLsHelper.when(() -> LsHelper.getContents(testFolderPath)).thenThrow(InvalidDirectoryException.class);

            String exception = assertDoesNotThrow(
                    () -> LsHelper.buildResult(List.of(testFolderPath), false, true)
            );

            mLsHelper.verify(() -> LsHelper.getContents(testFolderPath));

            assertEquals("null" + StringUtils.STRING_NEWLINE, exception);
        }
    }

    /**
     * ls list all contents with formatContents
     */
    @Test
    public void formatContents_listAllContentsSuccessfully_throwsNoException() {
        List<Path> contents = List.of(nestedFilePath, nestedFolderPath);

        String output = assertDoesNotThrow(
                () -> LsHelper.formatContents(contents, true)
        );

        String expected = buildExpectedResult(false, true);

        assertEquals(expected, output.replaceAll("\\d", ""));
    }

    /**
     * ls resolve paths from List of String to List of Path
     */
    @Test
    public void resolvePaths_withListOfDirectory_returnsListOfPath() {
        StringBuilder contents = new StringBuilder();
        contents.append(nestedFilePath.toString());

        List<Path> output = assertDoesNotThrow(
                () -> LsHelper.resolvePaths(contents.toString())
        );

        List<Path> expected = List.of(nestedFilePath);

        assertEquals(expected, output);
    }

    /**
     * ls resolve path from String to Path
     */
    @Test
    public void resolvePath_withStringDirectory_returnsPath() {
        Path output = assertDoesNotThrow(
                () -> LsHelper.resolvePath(nestedFilePath.toString())
        );

        assertEquals(nestedFilePath, output);
    }

    /**
     * ls get relative path to current directory
     */
    @Test
    public void getRelativeToCwd_withPath_returnsPath() {
        Path output = assertDoesNotThrow(
                () -> LsHelper.getRelativeToCwd(nestedFilePath) // lsFolder/TestFolder/TestFile
        );

        String expected = FOLDER_NAME + File.separator + FILE_NAME; //TestFolder
        assertEquals(expected, output.toString().replaceAll("\\d", ""));
    }

}
