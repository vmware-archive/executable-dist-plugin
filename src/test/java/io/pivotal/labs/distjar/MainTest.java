package io.pivotal.labs.distjar;

import org.junit.After;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipOutputStream;

import static io.pivotal.labs.distjar.MatcherUtils.allOf;
import static io.pivotal.labs.distjar.PathMatchers.exists;
import static io.pivotal.labs.distjar.ProcessResultMatcher.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;

public class MainTest {

    private Path zipFile;
    private Path tempDirectory;

    @After
    public void tearDown() throws Exception {
        Files.delete(zipFile);
        Files.walkFileTree(tempDirectory, PostOrderVisitor.with(Files::delete));
    }

    @Test
    public void unpacksTheEnclosingJar() throws Exception {
        zipFile = Files.createTempFile(getClass().getSimpleName(), ".jar");

        String mainClassFilePath = pathForClass(Main.class);

        try (ZipOutputStream zip = ZipUtils.file(zipFile)) {
            try (OutputStream entry = ZipUtils.entry(zip, "META-INF/MANIFEST.MF")) {
                Manifest manifest = createManifest(Collections.singletonMap("Main-Class", Main.class.getName()));
                manifest.write(entry);
            }
            try (OutputStream entry = ZipUtils.entry(zip, mainClassFilePath)) {
                try (InputStream resourceAsStream = openResource("/" + mainClassFilePath)) {
                    Main.copy(resourceAsStream, entry, new byte[1024]);
                }
            }
            try (OutputStream entry = ZipUtils.entry(zip, "hello.txt")) {
                entry.write("Hello, world!\n".getBytes());
            }
        }

        tempDirectory = Files.createTempDirectory(getClass().getSimpleName());

        ProcessResult result = ProcessResult.of(
                "java",
                "-Djava.io.tmpdir=" + tempDirectory,
                "-jar", zipFile.toString());

        assertThat(result, allOf(
                hasExitValue(equalTo(0)),
                hasOutput(isEmptyString()),
                hasError(isEmptyString())));
        Path unpackDirectory = Files.list(tempDirectory).collect(StreamUtils.only());
        assertThat(unpackDirectory.resolve("hello.txt"), exists(equalTo(true)));
    }

    private String pathForClass(Class<?> cl) {
        return cl.getName().replace('.', '/') + ".class";
    }

    private Manifest createManifest(Map<String, String> attributes) {
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.forEach(mainAttributes::putValue);
        return manifest;
    }

    private InputStream openResource(String name) throws FileNotFoundException {
        InputStream in = getClass().getResourceAsStream(name);
        if (in == null) throw new FileNotFoundException(name);
        return in;
    }

}
