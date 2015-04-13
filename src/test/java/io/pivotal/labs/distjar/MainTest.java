package io.pivotal.labs.distjar;

import io.pivotal.labs.io.*;
import io.pivotal.labs.util.StreamUtils;
import org.apache.tools.zip.ZipOutputStream;
import org.junit.After;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static io.pivotal.labs.io.ProcessResultMatcher.*;
import static io.pivotal.labs.test.MatcherUtils.allOf;
import static io.pivotal.labs.test.PathMatchers.exists;
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
            ZipUtils.entry(zip, "META-INF/MANIFEST.MF", ManifestUtils.toByteArray(ManifestUtils.create(Collections.singletonMap("Main-Class", Main.class.getName()))));
            ZipUtils.entry(zip, mainClassFilePath, ResourceUtils.load(mainClassFilePath));
            ZipUtils.entry(zip, "hello.txt", "Hello, world!\n".getBytes());
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

}
