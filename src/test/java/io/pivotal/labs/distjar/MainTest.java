package io.pivotal.labs.distjar;

import io.pivotal.labs.io.*;
import io.pivotal.labs.util.StreamUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.zip.ZipOutputStream;

import static io.pivotal.labs.io.ProcessResultMatcher.*;
import static io.pivotal.labs.test.MatcherUtils.allOf;
import static io.pivotal.labs.test.PathMatchers.exists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;

public class MainTest {

    private Path zipFile;
    private Path tempDirectory;

    @Before
    public void setUp() throws Exception {
        zipFile = Files.createTempFile(getClass().getSimpleName(), ".jar");
        tempDirectory = Files.createTempDirectory(getClass().getSimpleName());
    }

    @After
    public void tearDown() throws Exception {
        Files.delete(zipFile);
        Files.walkFileTree(tempDirectory, PostOrderVisitor.with(Files::delete));
    }

    @Test
    public void unpacksTheEnclosingJar() throws Exception {
        String mainClassFilePath = pathForClass(Main.class);

        try (ZipOutputStream zip = ZipUtils.file(zipFile)) {
            ZipUtils.entry(zip, "META-INF/MANIFEST.MF", ManifestUtils.toByteArray(ManifestUtils.create(Collections.singletonMap("Main-Class", Main.class.getName()))));
            ZipUtils.entry(zip, mainClassFilePath, ResourceUtils.load(mainClassFilePath));
            ZipUtils.entry(zip, "hello.txt", "Hello, world!\n".getBytes());
        }

        ProcessResult result = ProcessResult.of(
                "java",
                "-Djava.io.tmpdir=" + tempDirectory,
                "-jar", zipFile.toString());

        assertThat(result, allOf(
                hasExitValue(equalTo(0)),
                hasOutput(isEmptyString()),
                hasError(isEmptyString())));

        assertThat(unpackDirectory().resolve("hello.txt"), exists(equalTo(true)));
    }

    private String pathForClass(Class<?> cl) {
        return cl.getName().replace('.', '/') + ".class";
    }

    private Path unpackDirectory() throws IOException {
        return Files.list(tempDirectory).collect(StreamUtils.only());
    }

}
