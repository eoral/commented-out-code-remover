package com.eoral.deletecharsfromfilebyposition;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

public class Utils {

    public static Path createTempDirectory() {
        try {
            return Files.createTempDirectory(Constants.TEMP_DIR_PREFIX);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Path createTempFile() {
        try {
            return Files.createTempFile(Constants.TEMP_FILE_PREFIX, Constants.TEMP_FILE_SUFFIX);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void deleteRecursively(Path path) {
        deleteRecursively(path.toFile());
    }

    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteRecursively(f);
            }
        }
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void moveFileReplaceExisting(Path sourceFilePath, Path targetFilePath) {
        try {
            Files.move(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void copyFileReplaceExisting(Path sourceFilePath, Path targetFilePath) {
        try {
            Files.copy(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean areFileContentsIdentical(Path path1, Path path2, Charset charset) {
        try {
            String content1 = Files.readString(path1, charset);
            String content2 = Files.readString(path2, charset);
            return content1.equals(content2);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Set<Integer> getColumns(int start, int end) {
        Set<Integer> columns = new HashSet<>();
        for (int i = start; i <= end; i++) {
            columns.add(i);
        }
        return columns;
    }
}
