package io.pivotal.labs.io;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

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

    public static void entry(ZipOutputStream zip, String name, byte[] content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content);
        zip.closeEntry();
    }

}
