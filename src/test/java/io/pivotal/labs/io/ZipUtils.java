package io.pivotal.labs.io;

import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static ZipOutputStream file(Path path) throws IOException {
        FileOutputStream out = new FileOutputStream(path.toFile());
        try {
            return new ZipOutputStream(out);
        } catch (RuntimeException e) {
            out.close();
            throw e;
        }
    }

    public static OutputStream entry(ZipOutputStream zip, String name) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        return new FilterOutputStream(zip) {
            @Override
            public void close() throws IOException {
                zip.closeEntry();
            }
        };
    }

}
