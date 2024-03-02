package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RmApplicationPublicIntegrationTest {
    private static final String TEMP = "temp-rm";
    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static Path tempPath;

    private RmApplication rmApplication;

    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        tempPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
        Files.createDirectory(tempPath);
    }

    @AfterAll
    static void deleteTemp() throws IOException {
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
        Files.delete(tempPath);
    }

    @BeforeEach
    void setUp() {
        rmApplication = new RmApplication();
    }

    private Path createFile(String name) throws IOException {
        return createFile(name, tempPath);
    }

    private Path createDirectory(String folder) throws IOException {
        return createDirectory(folder, tempPath);
    }

    private Path createFile(String name, Path inPath) throws IOException {
        Path path = inPath.resolve(name);
        Files.createFile(path);
        FILES.push(path);
        return path;
    }

    private Path createDirectory(String folder, Path inPath) throws IOException {
        Path path = inPath.resolve(folder);
        Files.createDirectory(path);
        FILES.push(path);
        return path;
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            args.add(Paths.get(TEMP, file).toString());
        }
        return args.toArray(new String[0]);
    }

    @Test
    void run_singleFile_deletesFile() throws Exception {
        Path fileA = createFile("a.txt");
        Path fileB = createFile("bobby");
        rmApplication.run(toArgs("", "a.txt"), System.in, System.out);
        assertTrue(Files.notExists(fileA));
        assertTrue(Files.exists(fileB));
    }

    @Test
    void run_spaceInName_deletesFile() throws Exception {
        Path fileC = createFile("c   c");
        rmApplication.run(toArgs("", "c   c"), System.in, System.out);
        assertTrue(Files.notExists(fileC));
    }

    @Test
    void run_multipleFiles_deletesFiles() throws Exception {
        Path fileD = createFile("d.txt");
        Path fileE = createFile("eerie");
        rmApplication.run(toArgs("", "d.txt", "eerie"), System.in, System.out);
        assertTrue(Files.notExists(fileD));
        assertTrue(Files.notExists(fileE));
    }

    @Test
    void run_emptyDirectory_deletesDirectory() throws Exception {
        Path folder = createDirectory("folder");
        rmApplication.run(toArgs("d", "folder"), System.in, System.out);
        assertTrue(Files.notExists(folder));
    }

    @Test
    void run_multipleFilesEmptyDirectories_deletesAll() throws Exception {
        Path fileG = createFile("g.txt");
        Path fileH = createFile("high");
        Path directoryA = createDirectory("directoryA");
        Path directoryB = createDirectory("directoryB");
        rmApplication.run(toArgs("d", "g.txt", "high", "directoryA", "directoryB"), System.in, System.out);
        assertTrue(Files.notExists(fileG));
        assertTrue(Files.notExists(fileH));
        assertTrue(Files.notExists(directoryA));
        assertTrue(Files.notExists(directoryB));
    }

    @Test
    void run_directoryWithFiles_deletesDirectory() throws Exception {
        Path directory = createDirectory("directory");
        createFile("dwf.txt", directory);
        createFile("dwf2.txt", directory);
        rmApplication.run(toArgs("r", "directory"), System.in, System.out);
        assertTrue(Files.notExists(directory));
    }

    @Test
    void run_directoryInDirectory_deletesDirectory() throws Exception {
        Path directoryC = createDirectory("directoryC");
        createFile("did.txt", directoryC);
        Path directory = createDirectory("directoryDid", directoryC);
        Path inner = createDirectory("directoryDid", directory);
        createFile("did.txt", inner);
        createFile("did2.txt", inner);
        rmApplication.run(toArgs("r", "directoryC"), System.in, System.out);
        assertTrue(Files.notExists(directoryC));
    }

    @Test
    void run_multipleFilesDirectories_deletesAll() throws Exception {
        Path directoryD = createDirectory("directoryD");
        createFile("mfd.txt", directoryD);
        Path directory = createDirectory("directoryMfd", directoryD);
        Path inner = createDirectory("directoryMfd", directory);
        createFile("mfd.txt", inner);
        createFile("mfd2.txt", inner);
        Path empty = createDirectory("empty");
        Path fileI = createFile("ii");
        Path fileJ = createFile("jar");
        rmApplication.run(toArgs("r", "directoryD", "empty", "ii", "jar"), System.in, System.out);
        assertTrue(Files.notExists(directoryD));
        assertTrue(Files.notExists(empty));
        assertTrue(Files.notExists(fileI));
        assertTrue(Files.notExists(fileJ));
    }

    @Test
    void run_absolutePath_deletesDirectory() throws Exception {
        Path directory = createDirectory("directoryAbs");
        createDirectory("innerAbs", directory);
        rmApplication.run(new String[]{"-r", tempPath.resolve("directoryAbs").toString()}, System.in, System.out);
        assertTrue(Files.notExists(directory));
    }

    @Test
    void run_zeroArguments_throws() {
        assertThrows(Exception.class, () -> rmApplication.run(toArgs(""), System.in, System.out));
    }

    @Test
    void run_flagOnly_throws() {
        assertThrows(Exception.class, () -> rmApplication.run(toArgs("d"), System.in, System.out));
    }

    @Test
    void run_unknownFlag_throws() throws IOException {
        Path fileK = createFile("kick");
        assertThrows(Exception.class, () -> rmApplication.run(toArgs("x", "kick"), System.in, System.out));
        assertTrue(Files.exists(fileK));
    }
}
