package sg.edu.nus.comp.cs4218.exception;

public class InvalidDirectoryException extends LsException {
    public InvalidDirectoryException(String directory) {
        super(String.format("cannot access '%s': No such file or directory", directory));
    }
}
