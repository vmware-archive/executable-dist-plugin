package io.pivotal.labs.distjar;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {

    public static final int BUFFER_SIZE = 32 * 1024;

    public static void main(String... args) throws Exception {
        URL classUrl = findClassFile(Main.class);
        URL jarUrl = enclosingJar(classUrl);
        String jarPath = pathForFileUrl(jarUrl);
        Path unpackDir = Files.createTempDirectory("distjar");
        unpackJarFile(jarPath, unpackDir);
    }

    private static URL findClassFile(Class<?> cl) throws FileNotFoundException {
        URL url = cl.getResource(cl.getSimpleName() + ".class");
        if (url == null) {
            throw new FileNotFoundException("unable to locate class file for " + cl.getName());
        }
        return url;
    }

    private static URL enclosingJar(URL url) throws IOException {
        checkScheme(url, "jar");
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        return connection.getJarFileURL();
    }

    private static String pathForFileUrl(URL url) throws IOException {
        checkScheme(url, "file");
        try {
            return url.toURI().getPath();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private static void checkScheme(URL url, String scheme) throws IOException {
        if (!url.getProtocol().equals(scheme)) {
            throw new IOException("url does not have required scheme '" + scheme + "': " + url);
        }
    }

    private static void unpackJarFile(String path, Path unpackDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(path)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            byte[] buffer = new byte[BUFFER_SIZE];
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String relativePath = entry.getName();
                Path absolutePath = unpackDir.resolve(relativePath);
                if (entry.isDirectory()) {
                    Files.createDirectories(absolutePath);
                } else {
                    Files.createDirectories(absolutePath.getParent());
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        try (FileOutputStream out = new FileOutputStream(absolutePath.toFile())) {
                            copy(in, out, buffer);
                        }
                    }
                }
            }
        }
    }

    public static void copy(InputStream in, OutputStream out, byte[] buffer) throws IOException {
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}
