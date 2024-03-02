package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_EXISTS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;

public class MvApplication implements MvInterface {
    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws MvException {
        try {
            Path srcPath = IOUtils.resolveExistFilePath(srcFile);
            Path destPath = IOUtils.resolveFilePath(destFile);

            if (!Files.isWritable(srcPath) || (Files.exists(destPath) && !Files.isWritable(destPath))) {
                if (isOverwrite) {
                    throw new MvException(E_NO_PERM);
                }
                return null;
            }

            if (".".equals(destFile)) {
                throw new MvException(E_FILE_EXISTS);
            }

            if (srcPath.getParent().equals(destPath)) {
                throw new MvException(srcPath + " and " + srcPath + " are identical");
            }

            if (Files.isDirectory(destPath)) {
                destPath = Paths.get(destPath.toString(), srcPath.getFileName().toString());
            }

            File target = new File(destPath.toString());

            if (!target.exists() || isOverwrite) {
                Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return null;

        } catch (MvException e) {
            throw e;
        } catch (Exception e) {
            throw new MvException(e.getMessage(), e);
        }
    }

    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileName) throws MvException {
        try {
            Path destPath = IOUtils.resolveExistFilePath(destFolder);

            // throw exception if directory does not have permission
            if ((Files.exists(destPath) && !Files.isWritable(destPath))) {
                if (isOverwrite) {
                    throw new MvException(E_NO_PERM);
                }
                return null;
            }

            for (String srcFile : fileName) {
                Path srcPath = IOUtils.resolveFilePath(srcFile);

                // throw exception if file does not exist
                if (!Files.exists(srcPath)) {
                    throw new MvException("Cannot find '" + srcFile + "'. " + E_FILE_NOT_FOUND + ".");
                }
                // throw exception if file has no permission
                if (!Files.isWritable(srcPath)) {
                    throw new MvException(E_NO_PERM);
                }

                Path targetPath = Paths.get(destPath.toString(), srcPath.getFileName().toString());
                File target = new File(targetPath.toString());

                if (srcPath.equals(targetPath)) {
                    throw new MvException(srcPath + " and " + srcPath + " are identical");
                }

                if (!target.exists() || isOverwrite) {
                    Files.move(srcPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return null;

        } catch (MvException e) {
            throw e;
        } catch (Exception e) {
            throw new MvException(e.getMessage(), e);
        }
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        if (args == null) {
            throw new MvException(E_NULL_ARGS);
        }

        MvArgsParser parser = parseArgs(args);
        Boolean isOverwrite = parser.isOverwrite();
        String[] files = parser.getFiles().toArray(new String[parser.getFiles().size()]);
        String destDir = parser.getDestDir();

        try {
            if (files.length > 1) {
                mvFilesToFolder(isOverwrite, destDir, files);
            } else {
                mvSrcFileToDestFile(isOverwrite, files[0], destDir);
            }
        } catch (MvException e) {
            throw e;
        }
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @throws MvException
     */
    protected MvArgsParser parseArgs(String... args) throws MvException {
        MvArgsParser parser = new MvArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MvException(e.getMessage(), e);
        }
        return parser;
    }
}
