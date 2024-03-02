package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.support.ReflectionSupport;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.Environment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.generateTestFiles;

public class RegexArgumentTest {

    private static final List<String> EXPECTED_STRS = List.of("TestRegexString1", "TestRegexString2", "TestRegexString3",
            "TestRegexString4", "TestRegexString5");
    private static final String FILE_SEPARATOR = File.separatorChar == '\\' ? "\\" + File.separator : File.separator;

    @Test
    void merge_validStringArgument_correctRegexString() {
        RegexArgument regexArgs = new RegexArgument();
        assertTrue(regexArgs.isEmpty());
        for (String str : EXPECTED_STRS) {
            regexArgs.merge(str);
        }
        assertEquals(String.join("", EXPECTED_STRS), regexArgs.toString());
        assertFalse(regexArgs.isEmpty());
    }

    @Test
    void merge_validRegexArgument_correctRegexString() {
        RegexArgument regexArgs1 = new RegexArgument(EXPECTED_STRS.get(0));
        RegexArgument regexArgs2 = new RegexArgument(EXPECTED_STRS.get(2));
        RegexArgument regexArgs3 = new RegexArgument(EXPECTED_STRS.get(4));
        regexArgs1.merge(EXPECTED_STRS.get(1));
        regexArgs2.merge(EXPECTED_STRS.get(3));
        regexArgs1.merge(regexArgs2);
        regexArgs1.merge(regexArgs3);
        assertEquals(String.join("", EXPECTED_STRS.subList(0, 5)), regexArgs1.toString());
    }

    @Test
    void appendAsterisk_trivial_correctRegexArgument() {

        RegexArgument regexArgs = new RegexArgument();
        validateRegexArgument(regexArgs, "", "", false);

        regexArgs.appendAsterisk();
        validateRegexArgument(regexArgs, "*", String.format("[^%s]*", FILE_SEPARATOR), true);
    }

    @Test
    void append_validCharacter_correctRegexArgument() {

        RegexArgument regexArgs = new RegexArgument();
        validateRegexArgument(regexArgs, "", "", false);

        regexArgs.append('A');
        validateRegexArgument(regexArgs, "A", String.format("%s", Pattern.quote("A")), false);
    }

    @Test
    void constructor_validInputArguments_correctRegexArgument() {

        RegexArgument regexArgs = new RegexArgument(EXPECTED_STRS.get(0) + "*", EXPECTED_STRS.get(1), true);
        StringBuilder expectedRegex = new StringBuilder();
        expectedRegex.append(".*");
        for (char chr : EXPECTED_STRS.get(0).toCharArray()) {
            expectedRegex.append(Pattern.quote(String.valueOf(chr)));
        }
        expectedRegex.append(String.format("[^%s]*", FILE_SEPARATOR));
        validateRegexArgument(regexArgs, EXPECTED_STRS.get(1), expectedRegex.toString(), true);
    }

    @Test
    void globFiles_regexTrue_returnTestFiles(@TempDir Path tempDir) throws IOException {
        List<String> fileNames = List.of("test1.txt", "test2.txt", "test3.txt", "test4.txt", "notValid.txt", "test5.notValid");
        List<String> expectedFiles = fileNames.subList(0, 4);

        String testDirName = String.format("testDir%stestDir2%s", FILE_SEPARATOR, FILE_SEPARATOR);
        expectedFiles = expectedFiles.stream().map(x -> testDirName + x).collect(Collectors.toList());
        Path testDir = tempDir.resolve(testDirName);
        generateTestFiles(testDir, fileNames);

        try (MockedStatic<Environment> envMockStatic = mockStatic(Environment.class)) {
            envMockStatic.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());

            RegexArgument regexArgs = spy(new RegexArgument());
            when(regexArgs.isRegex()).thenReturn(true);
            List<String> plaintext = (List<String>) assertDoesNotThrow(() -> ReflectionSupport.tryToReadFieldValue(regexArgs.getClass().getDeclaredField("plaintext"), regexArgs).get());
            List<String> regex = (List<String>) assertDoesNotThrow(() -> ReflectionSupport.tryToReadFieldValue(regexArgs.getClass().getDeclaredField("regex"), regexArgs).get());


            for (char chr : testDirName.toCharArray()) {
                plaintext.add(String.valueOf(chr));
                regex.add(Pattern.quote(String.valueOf(chr)));
            }
            String globParameters = "test*.txt";
            for (char chr : globParameters.toCharArray()) {
                if (chr == '*') {
                    regex.add(String.format("[^\\%s]*", File.separator));
                    plaintext.add("*");
                    continue;
                }
                regex.add(Pattern.quote(String.valueOf(chr)));
                plaintext.add(String.valueOf(chr));
            }

            List<String> actualFiles = regexArgs.globFiles();
            assertIterableEquals(expectedFiles, actualFiles);
        }
    }

    @Test
    void globFiles_regexFalse_returnTestFiles() {
        RegexArgument regexArgs = new RegexArgument();
        List<String> plaintext = (List<String>) assertDoesNotThrow(() -> ReflectionSupport.tryToReadFieldValue(regexArgs.getClass().getDeclaredField("plaintext"), regexArgs).get());
        plaintext.add(EXPECTED_STRS.get(0));
        List<String> actualFiles = regexArgs.globFiles();
        assertIterableEquals(List.of(EXPECTED_STRS.get(0)), actualFiles);
    }

    @Test
    void traverseAndFilter_noAbsoluteNoDir_returnFilteredList(@TempDir Path tempDir) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<String> fileNames = List.of("test1.txt", "test2.txt", "test3.txt", "test4.txt", "notValid.txt", "test5.notValid");
        List<String> expectedFiles = fileNames.subList(0, 4);

        generateTestFiles(tempDir, fileNames);

        RegexArgument regexArgument = new RegexArgument();
        Method traverseAndFilter = regexArgument.getClass().getDeclaredMethod("traverseAndFilter", Pattern.class, File.class, boolean.class, boolean.class);
        traverseAndFilter.setAccessible(true);

        try (MockedStatic<Environment> envMockStatic = mockStatic(Environment.class)) {
            envMockStatic.when(Environment::getCurrentDirectory).thenReturn(tempDir.toString());

            List<?> actualFiles = (List<?>) traverseAndFilter.invoke(regexArgument, Pattern.compile("test[\\d].txt"), tempDir.toFile(), false, false);
            actualFiles = actualFiles.stream().map(x -> Paths.get((String) x).getFileName().toString()).collect(Collectors.toList());

            assertFalse(actualFiles.isEmpty());

            actualFiles.forEach(x -> assertTrue(expectedFiles.contains(x)));
        }
    }

    @Test
    void traverseAndFilter_absoluteWithDir_returnFilteredList(@TempDir Path tempDir)
            throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<String> fileNamesWithDir = List.of("test/test1.txt", "test/test2.txt", "test2/test3.txt", "test2/test4.txt", "test/notValid.txt", "test2/test5.notValid");
        List<String> expectedDir = List.of("test", "test2");
        Files.createDirectories(tempDir.resolve("test"));
        Files.createDirectories(tempDir.resolve("test2"));
        Files.createDirectories(tempDir.resolve("NotValid"));

        generateTestFiles(tempDir, fileNamesWithDir);
        RegexArgument regexArgument = new RegexArgument();
        Method traverseAndFilter = regexArgument.getClass().getDeclaredMethod("traverseAndFilter", Pattern.class, File.class, boolean.class, boolean.class);
        traverseAndFilter.setAccessible(true);

        List<?> actualFiles2 = (List<?>) traverseAndFilter.invoke(regexArgument, Pattern.compile(".*test[\\d]" + FILE_SEPARATOR), tempDir.toFile(), true, true);
        actualFiles2 = actualFiles2.stream().map(x -> Paths.get((String) x).getFileName().toString()).collect(Collectors.toList());
        assertFalse(actualFiles2.isEmpty());

        actualFiles2.forEach(x -> assertTrue(expectedDir.contains(x)));

    }

    private void validateRegexArgument(RegexArgument regexArgs, String expectedPlainText, String expectedRegex, boolean expectedIsRegex) {
        List<String> plaintext = (List<String>) assertDoesNotThrow(() -> ReflectionSupport.tryToReadFieldValue(regexArgs.getClass().getDeclaredField("plaintext"), regexArgs).get());
        List<String> regex = (List<String>) assertDoesNotThrow(() -> ReflectionSupport.tryToReadFieldValue(regexArgs.getClass().getDeclaredField("regex"), regexArgs).get());
        boolean isRegex = (boolean) assertDoesNotThrow(() -> ReflectionSupport.tryToReadFieldValue(regexArgs.getClass().getDeclaredField("hasWildcard"), regexArgs).get());

        assertEquals(expectedPlainText, String.join("", plaintext));
        assertEquals(expectedRegex, String.join("", regex));
        assertEquals(expectedIsRegex, isRegex);
    }
}
