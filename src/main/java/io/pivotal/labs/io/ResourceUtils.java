package io.pivotal.labs.io;

import java.io.*;
import java.net.URL;

public class ResourceUtils {

    public static byte[] load(String name) throws IOException {
        try (InputStream in = open(name)) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            copy(in, buf);
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

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static String classFileName(Class<?> cl) {
        return cl.getName().replace('.', '/') + ".class";
    }

}
