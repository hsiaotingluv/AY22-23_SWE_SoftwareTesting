package sg.edu.nus.comp.cs4218.impl.util;

public class ErrorConstants {

    // Streams related
    public static final String E_WRITE_STREAM = "Could not write to output stream";
    public static final String E_NULL_POINTER = "Null Pointer Exception";
    public static final String E_CLOSING_STREAMS = "Unable to close streams";
    public static final String E_MUL_STREAMS = "Multiple streams provided";
    public static final String E_STREAM_CLOSED = "Stream is closed";
    public static final String E_NO_OSTREAM = "OutputStream not provided";
    public static final String E_NO_ISTREAM = "InputStream not provided";
    public static final String E_NO_INPUT = "No InputStream and no filenames";
    public static final String E_NO_FILE_ARGS = "No files provided";

    // Arguments related
    public static final String E_MISSING_ARG = "Missing Argument";
    public static final String E_NO_ARGS = "Insufficient arguments";
    public static final String E_NULL_ARGS = "Null arguments";
    public static final String E_TOO_MANY_ARGS = "Too many arguments";
    public static final String E_INVALID_FLAG = "Invalid flag option supplied";
    public static final String E_BAD_REGEX = "Invalid pattern";

    // Files and folders related
    public static final String E_FILE_NOT_FOUND = "No such file or directory";
    public static final String E_READING_FILE = "Could not read file";
    public static final String E_IS_DIR = "This is a directory";
    public static final String E_IS_NOT_DIR = "Not a directory";
    public static final String E_NO_PERM = "Permission denied";

    public static final String E_MISSING_FIELD = "Invalid format";

    // `find` related
    public static final String E_INVALID_FILE = "Invalid Filename";
    public static final String E_NAME_FLAG = "Paths must precede -name";

    // `sed` related
    public static final String E_NO_REP_RULE = "No replacement rule supplied";
    public static final String E_INVAL_REP_RULE = "Invalid replacement rule";
    public static final String E_INVALID_REP_X = "X needs to be a number greater than 0";
    public static final String E_INVALID_REGEX = "Invalid regular expression supplied";
    public static final String E_EMPTY_PATTERN = "Pattern should not be empty.";

    // `grep` related
    public static final String E_NO_REGEX = "No regular expression supplied";

    // `mkdir` related
    public static final String E_NO_FOLDERS = "No folder names are supplied";
    public static final String E_FILE_EXISTS = "File or directory already exists";
    public static final String E_TOP_LVL_MISSING = "Top level folders do not exist";

    // General constants
    public static final String E_INVALID_APP = "Invalid app";
    public static final String E_NOT_SUPPORTED = "Not supported yet";
    public static final String E_SYNTAX = "Invalid syntax";
    public static final String E_GENERAL = "Exception Caught";
    public static final String E_IO_EXCEPTION = "IOException";
    public static final String E_ILLEGAL_FLAG = "illegal option -- ";

}
