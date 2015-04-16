package io.pivotal.labs.io;

import io.pivotal.labs.distjar.Main;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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

    public static InputStream open(Class<?> cl, String name) throws FileNotFoundException {
        return open(cl.getPackage().getName().replace('.', '/') + "/" + name);
    }

    public static URL findResource(String name) throws FileNotFoundException {
        URL url = ResourceUtils.class.getClassLoader().getResource(name);
        if (url == null) throw new FileNotFoundException(name);
        return url;
    }

}
