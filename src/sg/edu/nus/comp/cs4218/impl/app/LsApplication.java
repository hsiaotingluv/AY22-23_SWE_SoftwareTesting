package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.LsInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.app.helper.LsHelper.buildResult;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsHelper.formatContents;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsHelper.getContents;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsHelper.resolvePaths;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_WRITE_STREAM;

public class LsApplication implements LsInterface {


    @Override
    public String listFolderContent(Boolean isRecursive, Boolean isSortByExt,
                                    String... folderName) throws LsException {
        try {
            if (folderName.length == 0 && !isRecursive) {
                return listCwdContent(isSortByExt);
            }

            List<Path> paths;
            List<Path> files = new ArrayList<>();
            if (folderName.length == 0) {
                String[] directories = new String[1];
                directories[0] = Environment.getCurrentDirectory();
                paths = resolvePaths(directories);
            } else {

                List<Path> directories = new ArrayList<>();
                resolvePaths(folderName).forEach(x -> {
                            if (x.toFile().isDirectory()) {
                                directories.add(x);
                            } else {
                                files.add(x);
                            }
                        }
                );
                paths = directories;
            }

            String output = buildResult(paths, isRecursive, isSortByExt);
            if (!files.isEmpty()) {
                output = formatContents(files, isSortByExt) + StringUtils.STRING_NEWLINE + StringUtils.STRING_NEWLINE + output;
            }

            return output;

        } catch (LsException e) {
            throw e;
        } catch (Exception e) {
            throw new LsException(e.getMessage(), e);
        }
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws LsException {
        if (stdout == null) {
            throw new LsException(E_NULL_POINTER);
        }

        LsArgsParser parser = parseArgs(args);

        Boolean recursive = parser.isRecursive();
        Boolean sortByExt = parser.isSortByExt();
        String[] directories = parser.getDirectories()
                .toArray(new String[parser.getDirectories().size()]);
        String result = listFolderContent(recursive, sortByExt, directories).trim();

        try {
            stdout.write(result.getBytes());
            stdout.write(StringUtils.STRING_NEWLINE.getBytes());

        } catch (Exception e) {
            throw new LsException(E_WRITE_STREAM, e);
        }
    }

    /**
     * Parses arguments for further processing.
     *
     * @param args Arguments passed to the application
     * @throws LsException
     */
    protected LsArgsParser parseArgs(String... args) throws LsException {
        LsArgsParser parser = new LsArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new LsException(e.getMessage(), e);
        }
        return parser;
    }

    /**
     * Lists only the current directory's content and RETURNS. This does not account for recursive
     * mode in cwd.
     *
     * @param isSortByExt
     * @return
     */
    protected String listCwdContent(Boolean isSortByExt) throws LsException {
        String cwd = Environment.getCurrentDirectory();
        try {
            return formatContents(getContents(Paths.get(cwd)), isSortByExt);
        } catch (InvalidDirectoryException e) {
            throw new LsException("Unexpected error occurred!", e);
        }
    }


}
