package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CpInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.CpArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static sg.edu.nus.comp.cs4218.impl.app.helper.CpHelper.checkRunInputs;
import static sg.edu.nus.comp.cs4218.impl.app.helper.CpHelper.copyFile;
import static sg.edu.nus.comp.cs4218.impl.app.helper.CpHelper.copyRecursivelyFromSrc;
import static sg.edu.nus.comp.cs4218.impl.app.helper.CpHelper.copyRecursivelyFromSrcToItself;
import static sg.edu.nus.comp.cs4218.impl.app.helper.CpHelper.handleCpExceptions;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_NOT_DIR;


public class CpApplication implements CpInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {

        checkRunInputs(args, stdin, stdout);

        CpArgsParser parser = parseArgs(args);
        Boolean isRecursive = parser.isRecursive();

        int argsLength = args.length;
        String destFile = args[argsLength - 1];

        ArrayList<String> srcFiles = new ArrayList<>();

        for (int i = isRecursive ? 1 : 0; i < argsLength - 1; i++) {
            srcFiles.add(args[i]);
        }

        try {
            File dest = IOUtils.resolveFilePath(destFile).toFile();

            if (dest.exists() && dest.isDirectory()) {
                cpFilesToFolder(isRecursive, destFile, srcFiles.toArray(String[]::new));
            } else {
                if (srcFiles.size() > 1) {
                    throw new Exception(dest.getName() + ": " + E_IS_NOT_DIR);
                } else if (srcFiles.size() == 1) {
                    String srcFile = srcFiles.get(0);
                    cpSrcFileToDestFile(isRecursive, srcFile, destFile);
                }
            }
        } catch (Exception e) {
            throw new CpException(e.getMessage(), e);
        }
    }


    @Override
    public void cpSrcFileToDestFile(Boolean isRecursive, String srcFile, String destFile) throws Exception {

        Path src = IOUtils.resolveFilePath(srcFile);
        Path dst = IOUtils.resolveFilePath(destFile);

        if (!src.toFile().exists()) {
            throw new Exception(src.getFileName() + ": " + E_FILE_NOT_FOUND);
        }

        if (src.getFileName().equals(dst.getFileName())) {
            throw new Exception(String.format("%s and %s are identical (not copied).", destFile, srcFile));
        }

        if (src.toFile().isDirectory() && dst.toFile().exists() && !dst.toFile().isDirectory() && isRecursive) {
            throw new Exception(dst.getFileName() + ": " + E_IS_NOT_DIR);
        }

        if (src.toFile().isDirectory() && isRecursive) {
            try (Stream<Path> stream = Files.walk(src)) {
                stream.forEach(s -> {
                    try {
                        Path newDstPath = dst.resolve(src.relativize(s));
                        Files.copy(s, newDstPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                    }
                });
            } catch (IOException e) {
                throw new Exception(E_IO_EXCEPTION, e);
            }

        } else if (src.toFile().isDirectory()) {
            throw new Exception(src.getFileName() + ": " + E_IS_DIR);
        } else {
            try {
                Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new Exception(E_IO_EXCEPTION, e);
            }
        }
    }

    @Override
    public void cpFilesToFolder(Boolean isRecursive, String destFolder, String... fileName) throws Exception {

        Path dstPath = IOUtils.resolveFilePath(destFolder);
        ArrayList<String> cpExceptions = new ArrayList<>();
        List<String> fileNames = Arrays.asList(fileName);

        if (!dstPath.toFile().isDirectory() && dstPath.toFile().exists()) {
            cpExceptions.add(dstPath.getFileName() + ": " + E_IS_NOT_DIR);
            handleCpExceptions(cpExceptions);
            return;
        }

        ArrayList<Path> srcPaths = new ArrayList<>();

        for (String fn : fileNames) {
            Path srcPath = IOUtils.resolveFilePath(fn);
            srcPaths.add(srcPath);
        }

        for (Path srcPath : srcPaths) {
            if (!srcPath.toFile().exists()) {
                cpExceptions.add(srcPath.getFileName() + ": " + E_FILE_NOT_FOUND);
                continue;
            }

            if (srcPath.toFile().isDirectory() && !isRecursive) {
                cpExceptions.add(srcPath.getFileName() + ": " + E_IS_DIR);
                continue;
            }

            if (srcPath.toFile().isDirectory() && isRecursive) {
                if (srcPath.toString().equals(dstPath.toString())) {
                    copyRecursivelyFromSrcToItself(srcPath, dstPath, cpExceptions);
                } else {
                    copyRecursivelyFromSrc(srcPath, dstPath, cpExceptions);
                }
                continue;
            }

            if (!srcPath.toFile().isDirectory()) {
                copyFile(srcPath, dstPath, cpExceptions);
            }
        }

        handleCpExceptions(cpExceptions);
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @throws CpException
     */
    protected CpArgsParser parseArgs(String... args) throws CpException {
        CpArgsParser parser = new CpArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CpException(e.getMessage(), e);
        }
        return parser;
    }

}
