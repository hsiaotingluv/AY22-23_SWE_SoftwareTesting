package sg.edu.nus.comp.cs4218;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.nio.file.Files;

public final class Environment {

    /**
     * Java VM does not support changing the current working directory.
     * For this reason, we use Environment.currentDirectory instead.
     */
    public static volatile String currentDirectory = System.getProperty("user.dir");

    private Environment() {
    }

    /**
     * Returns the absolute path of the current working directory.
     *
     * @return string of absolute path
     */
    public static String getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * Sets the current working directory.
     *
     * @param string of valid absolute path
     */
    public static void setCurrentDirectory(String path) throws ShellException {
        if (Files.isDirectory(IOUtils.resolveFilePath(path))) {
            currentDirectory = IOUtils.resolveFilePath(path).toString();
        }
    }

}
