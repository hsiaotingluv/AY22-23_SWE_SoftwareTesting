package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_CLOSING_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_INVALID_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_STREAM_CLOSED;

public final class IOUtils {
    private IOUtils() {
    }

    /**
     * Open an inputStream based on the file name.
     *
     * @param fileName String containing file name.
     * @return InputStream of file opened.
     * @throws ShellException If file destination is inaccessible.
     */
    public static InputStream openInputStream(String fileName) throws ShellException {
        String resolvedFileName = resolveFilePath(fileName).toString();

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(resolvedFileName);
        } catch (FileNotFoundException e) {
            throw new ShellException(E_FILE_NOT_FOUND, e);
        }

        return fileInputStream;
    }

    /**
     * Open an outputStream based on the file name.
     *
     * @param fileName String containing file name.
     * @return OutputStream of file opened.
     * @throws ShellException If file destination is inaccessible.
     */
    public static OutputStream openOutputStream(String fileName) throws ShellException {
        String resolvedFileName = resolveFilePath(fileName).toString();

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(resolvedFileName);
        } catch (FileNotFoundException e) {
            throw new ShellException(E_FILE_NOT_FOUND, e);
        }

        return fileOutputStream;
    }

    /**
     * Close an inputStream. If inputStream provided is System.in or null, it will be ignored.
     *
     * @param inputStream InputStream to be closed.
     * @throws ShellException If inputStream cannot be closed successfully.
     */
    public static void closeInputStream(InputStream inputStream) throws ShellException {
        if (inputStream == null || inputStream.equals(System.in)) {
            return;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new ShellException(E_CLOSING_STREAMS, e);
        }
    }

    /**
     * Close an outputStream. If outputStream provided is System.out or null, it will be ignored.
     *
     * @param outputStream OutputStream to be closed.
     * @throws ShellException If outputStream cannot be closed successfully.
     */
    public static void closeOutputStream(OutputStream outputStream) throws ShellException {
        if (outputStream == null || outputStream.equals(System.out)) {
            return;
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            throw new ShellException(E_CLOSING_STREAMS, e);
        }
    }

    public static Path resolveFilePath(String fileName) throws ShellException {
        try {
            Path currentDirectory = Paths.get(Environment.getCurrentDirectory());
            return currentDirectory.resolve(fileName);
        } catch (InvalidPathException e) {
            throw new ShellException(E_INVALID_FILE, e);
        }
    }

    public static Path resolveExistFilePath(String fileName) throws ShellException {
        Path currentDirectory = resolveFilePath(fileName);
        if (!Files.exists(currentDirectory)) {
            throw new ShellException(E_FILE_NOT_FOUND);
        }
        return currentDirectory;
    }

    /**
     * Returns a list of lines based on the given InputStream.
     *
     * @param input InputStream containing arguments from System.in or FileInputStream
     * @throws Exception
     */
    public static List<String> getLinesFromInputStream(InputStream input) throws ShellException {
        List<String> output = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
            closeInputStream(input);
        } catch (IOException e) {
            throw new ShellException(E_STREAM_CLOSED, e);
        }
        return output;
    }
}
