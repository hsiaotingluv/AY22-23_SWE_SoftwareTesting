package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.TeeInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class TeeApplication implements TeeInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        // Format: tee [-a] [FILES]...
        if (stdout == null) {
            throw new TeeException(E_NULL_POINTER);
        }
        TeeArgsParser teeArgs = parseArgs(args);
        StringBuilder output = new StringBuilder();
        try {
            output.append(teeFromStdin(teeArgs.isAppendToFile(), stdin, teeArgs.getFiles().toArray(new String[0])));
        } catch (TeeException e) {
            throw e;
        }
        try {
            if (!output.toString().isEmpty()) {
                stdout.write(output.toString().getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new TeeException(E_WRITE_STREAM, e);
        }
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @return TeeArgsParser containing parsed arguments
     * @throws TeeException
     */
    protected TeeArgsParser parseArgs(String... args) throws TeeException {
        TeeArgsParser parser = new TeeArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new TeeException(e.getMessage(), e);
        }
        return parser;
    }

    /**
     * Reads from standard input and write to both the standard output and files
     *
     * @param isAppend Boolean option to append the standard input to the contents of the input files
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names
     * @return String read from standard input
     * @throws Exception
     */
    @Override
    public String teeFromStdin(Boolean isAppend, InputStream stdin, String... fileName) throws TeeException {
        if (stdin == null) {
            throw new TeeException(E_NULL_POINTER);
        }
        List<String> output;
        List<String> errorOutput = new ArrayList<>();
        try {
            output = IOUtils.getLinesFromInputStream(stdin);
        } catch (ShellException e) {
            throw new TeeException(e.getMessage(), e);
        }

        if (fileName != null) {
            for (String file : fileName) {
                try {
                    File node = resolveFile(file);
                    writeToFile(isAppend, output, node);
                } catch (TeeException e) {
                    errorOutput.add(e.getMessage());
                }
            }
        }
        output.addAll(errorOutput);
        return String.join(STRING_NEWLINE, output);
    }

    protected File resolveFile(String file) throws TeeException {
        File node;
        try {
            node = IOUtils.resolveFilePath(file).toFile();
            node.createNewFile();
        } catch (ShellException | IOException e) {
            throw new TeeException(e.getMessage(), e);
        }
        if (!node.exists()) {
            throw new TeeException(node.getName() + ": " + E_FILE_NOT_FOUND);
        }
        if (node.isDirectory()) {
            throw new TeeException(node.getName() + ": " + E_IS_DIR);
        }
        if (!node.canWrite()) {
            throw new TeeException(node.getName() + ": " + E_NO_PERM);
        }
        return node;
    }

    /**
     * Writes the contents of the output argument to the specified file.
     *
     * @param isAppend Boolean option to append the standard input to the contents of the input files
     * @param output   Strings to be written to the file
     * @param node     File         File to be written to
     * @throws Exception
     */
    protected void writeToFile(Boolean isAppend, List<String> output, File node) throws TeeException {
        if (!output.isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(node, isAppend))) {
                for (String str : output) {
                    writer.write(str);
                    writer.newLine();
                }
            } catch (IOException e) {
                throw new TeeException(E_WRITE_STREAM, e);
            }
        }
    }
}
