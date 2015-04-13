package io.pivotal.labs.io;

import io.pivotal.labs.distjar.Main;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ResourceUtils {

    public static byte[] load(String name) throws IOException {
        try (InputStream in = open(name)) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            Main.copy(in, buf, new byte[1024]);
            return buf.toByteArray();
        }
    }

    public static InputStream open(String name) throws FileNotFoundException {
        InputStream in = ResourceUtils.class.getClassLoader().getResourceAsStream(name);
        if (in == null) throw new FileNotFoundException(name);
        return in;
    }

}
