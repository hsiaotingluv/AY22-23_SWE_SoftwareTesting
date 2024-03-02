package sg.edu.nus.comp.cs4218.impl.app.helper;

import sg.edu.nus.comp.cs4218.exception.CpException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public final class CpHelper {

    private CpHelper() {
    }

    public static void checkRunInputs(String[] args, InputStream stdin, OutputStream stdout) throws CpException {
        if (args == null) {
            throw new CpException(E_NULL_ARGS);
        }

        if (stdin == null && stdout == null) {
            throw new CpException(E_NULL_POINTER);
        }

        if (stdin == null) {
            throw new CpException(E_NO_ISTREAM);
        }

        if (stdout == null) {
            throw new CpException(E_NO_OSTREAM);
        }

        if (args.length == 0) {
            throw new CpException(E_NO_ARGS);
        }
    }

    public static void copyFile(Path srcPath, Path dstPath, List<String> cpExceptions) {
        try {
            Files.copy(srcPath, dstPath.resolve(srcPath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            cpExceptions.add(E_IO_EXCEPTION);
        }
    }

    public static void copyRecursivelyFromSrc(Path srcPath, Path dstPath, List<String> cpExceptions) {
        try (Stream<Path> stream = Files.walk(srcPath)) {
            stream.forEach(src -> {
                try {
                    Path newDstPath = dstPath.resolve(srcPath.getFileName());
                    Files.copy(src, newDstPath.resolve(srcPath.relativize(src)), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                }
            });
        } catch (IOException e) {
            cpExceptions.add(E_IO_EXCEPTION);
        }
    }

    public static void copyRecursivelyFromSrcToItself(Path srcPath, Path dstPath, List<String> cpExceptions) {
        try {
            Path tempPath = Files.createTempDirectory(null);
            copyRecursivelyFromSrc(srcPath, tempPath, cpExceptions);
            Stream<Path> stream = Files.walk(tempPath);
            stream.forEach(src -> {
                try {
                    Files.copy(src, dstPath.resolve(tempPath.relativize(src)),
                            StandardCopyOption.REPLACE_EXISTING);

                } catch (Exception e) {
                }
            });
            Files.walk(tempPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            cpExceptions.add(E_IO_EXCEPTION);
        }
    }

    public static void handleCpExceptions(List<String> cpExceptions) throws Exception {
        String totalExceptions = "";

        if (cpExceptions.size() > 1) {
            for (int i = 0; i < cpExceptions.size(); i++) {
                String cpException = cpExceptions.get(i);
                if (i == 0) {
                    totalExceptions += cpException + STRING_NEWLINE;
                } else if (i < cpExceptions.size() - 1) {
                    totalExceptions += "cp: " + cpException + STRING_NEWLINE;
                } else {
                    totalExceptions += "cp: " + cpException;
                }
            }
        } else if (cpExceptions.size() == 1) {
            totalExceptions += cpExceptions.get(0);
        }

        if (!totalExceptions.isEmpty()) {
            throw new Exception(totalExceptions);
        }
    }

}
