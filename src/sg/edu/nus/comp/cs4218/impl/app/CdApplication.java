package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CdInterface;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_TOO_MANY_ARGS;

public class CdApplication implements CdInterface {


    @Override
    public void changeToDirectory(String path) throws CdException {
        try {
            if ("..".equals(path) || "../".equals(path)) {
                Path curr = IOUtils.resolveFilePath(Environment.currentDirectory);
                Path parent = curr.getParent();
                Environment.currentDirectory = parent.toString();
            } else {
                Environment.currentDirectory = getNormalizedAbsolutePath(path);
            }
        } catch (CdException e) {
            throw e;
        } catch (Exception e) {
            throw new CdException(e.getMessage(), e);
        }
    }

    /**
     * Runs the cd application with the specified arguments.
     * Assumption: The application must take in one arg. (cd without args is not supported)
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws CdException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CdException {
        if (args == null) {
            throw new CdException(E_NULL_ARGS);
        }
        if (stdin == null) {
            throw new CdException(E_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new CdException(E_NO_OSTREAM);
        }
        // return to home directory
        if (args.length == 0 || (args.length == 1 && args[0].equals(""))) {
            Environment.currentDirectory = System.getProperty("user.dir").trim();
            return;
        }
        if (args.length > 1) {
            throw new CdException(E_TOO_MANY_ARGS);
        }

        try {
            changeToDirectory(args[0]);

        } catch (CdException e) {
            throw e;
        }
    }

    private String getNormalizedAbsolutePath(String pathStr) throws CdException {
        // if pathString is an empty string
        if (StringUtils.isBlank(pathStr)) {
            throw new CdException(E_NO_ARGS);
        }

        Path path = new File(pathStr).toPath();
        if (!path.isAbsolute()) {
            path = Paths.get(Environment.getCurrentDirectory(), pathStr);
        }

        if (!Files.exists(path)) {
            throw new CdException(String.format(E_FILE_NOT_FOUND, pathStr));
        }

        if (!Files.isDirectory(path)) {
            throw new CdException(String.format(E_IS_NOT_DIR, pathStr));
        }

        if (!Files.isExecutable(path)) {
            throw new CdException(String.format(E_NO_PERM, pathStr));
        }

        return path.normalize().toString();
    }
}
