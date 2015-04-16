package io.pivotal.labs.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static ZipOutputStream file(Path path) throws IOException {
        FileOutputStream out = new FileOutputStream(path.toFile());
        return file(out);
    }

    public static ZipOutputStream file(OutputStream out) throws IOException {
        try {
            return new ZipOutputStream(out);
        } catch (RuntimeException e) {
            out.close();
            throw e;
        }
    }

    public static void entry(ZipOutputStream zip, String name, byte[] content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content);
        zip.closeEntry();
    }

}
