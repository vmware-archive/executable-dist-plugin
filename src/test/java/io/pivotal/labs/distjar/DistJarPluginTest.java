package io.pivotal.labs.distjar;

import io.pivotal.labs.io.PostOrderVisitor;
import io.pivotal.labs.io.ProcessResult;
import io.pivotal.labs.io.ResourceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.pivotal.labs.io.ProcessResultMatcher.hasOutput;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DistJarPluginTest {

    private byte[] buffer = new byte[1024];
    private Path currentDirectory;
    private Path projectRoot;

    @Before
    public void setUp() throws Exception {
        currentDirectory = Paths.get("");
        projectRoot = Files.createTempDirectory(getClass().getSimpleName());
    }

    @After
    public void tearDown() throws Exception {
        Files.walkFileTree(projectRoot, PostOrderVisitor.with(Files::delete));
    }

    @Test
    public void shouldMakeARunnableDistJar() throws Exception {
        link(projectRoot, "gradle", currentDirectory);
        link(projectRoot, "gradlew", currentDirectory);

        provide(projectRoot, "build.gradle");
        provide(projectRoot, "settings.gradle");

        Path packageRoot = projectRoot.resolve("src/main/java/myapp");
        provide(packageRoot, "Hello.java");

        runGradle("installDist");

        Path startScript = projectRoot.resolve("build/install/myapp/bin/myapp");

        ProcessResult result = run(startScript, false, "one", "two", "three");
        assertThat(result, hasOutput(equalTo("Hello, world!\none\ntwo\nthree\n")));
    }

    private void link(Path dir, String name, Path targetDir) throws IOException {
        Files.createSymbolicLink(dir.resolve(name), targetDir.resolve(name).toAbsolutePath());
    }

    private void provide(Path dir, String name) throws IOException {
        Files.createDirectories(dir);
        Path path = dir.resolve(name);
        try (InputStream in = ResourceUtils.open(getClass(), name)) {
            try (FileOutputStream out = new FileOutputStream(path.toFile())) {
                Main.copy(in, out, buffer);
            }
        }
    }

    private ProcessResult runGradle(String... tasks) throws IOException, InterruptedException {
        return run(projectRoot.resolve("gradlew"), true, tasks);
    }

    private ProcessResult run(Path executable, boolean inheritIo, String... args) throws IOException, InterruptedException {
        List<String> commandLine = new ArrayList<>();
        commandLine.add(executable.toString());
        commandLine.addAll(Arrays.asList(args));

        ProcessBuilder processBuilder = new ProcessBuilder()
                .directory(projectRoot.toFile())
                .command(commandLine);
        if (inheritIo) processBuilder.inheritIO();

        ProcessResult result = ProcessResult.of(processBuilder);

        assertThat(result.getExitValue(), equalTo(0));

        return result;
    }

}
