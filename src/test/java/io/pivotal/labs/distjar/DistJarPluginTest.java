package io.pivotal.labs.distjar;

import io.pivotal.labs.io.PostOrderVisitor;
import io.pivotal.labs.io.ProcessResult;
import io.pivotal.labs.io.ResourceUtils;
import io.pivotal.labs.io.ZipUtils;
import org.apache.tools.zip.ZipFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipOutputStream;

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
        provideJar(projectRoot, findClasspathRootDir(Main.class), "dist-jar-plugin.jar");
        Path ant = findClasspathRootJar(ZipFile.class);
        link(projectRoot, "ant.jar", ant.getParent(), ant.getFileName().toString());

        Path packageRoot = projectRoot.resolve("src/main/java/myapp");
        provide(packageRoot, "Hello.java");

        runGradle("distJar");

        Path distJar = projectRoot.resolve("build/distributions/myapp.jar");

        ProcessResult result = run("java", false, "-jar", distJar.toString(), "one", "two", "three");
        assertThat(result, hasOutput(equalTo("Hello, world!\none\ntwo\nthree\n")));
    }

    private Path findClasspathRootDir(Class<?> cl) throws IOException {
        String classResourceName = classFileName(cl);
        URL classResourceUrl = ResourceUtils.findResource(classResourceName);
        Path classResourceFile = toPath(classResourceUrl);
        return subtract(classResourceFile, Paths.get(classResourceName));
    }

    private Path subtract(Path absolute, Path relative) throws IOException {
        if (!absolute.endsWith(relative)) {
            throw new IOException(absolute + " is not " + relative);
        }
        return absolute.resolve("/").resolve(absolute.subpath(0, absolute.getNameCount() - relative.getNameCount()));
    }

    private Path findClasspathRootJar(Class<?> cl) throws IOException {
        String classResourceName = classFileName(cl);
        URL classResourceUrl = ResourceUtils.findResource(classResourceName);
        URL jarUrl = enclosingJar(classResourceUrl);
        return toPath(jarUrl);
    }

    private static URL enclosingJar(URL url) throws IOException {
        checkScheme(url, "jar");
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        return connection.getJarFileURL();
    }

    private static void checkScheme(URL url, String scheme) throws IOException {
        if (!url.getProtocol().equals(scheme)) {
            throw new IOException("url does not have required scheme '" + scheme + "': " + url);
        }
    }

    private String classFileName(Class<?> cl) {
        return cl.getName().replace('.', '/') + ".class";
    }

    private Path toPath(URL url) throws IOException {
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private void link(Path dir, String name, Path targetDir) throws IOException {
        link(dir, name, targetDir, name);
    }

    private void link(Path dir, String name, Path targetDir, String targetName) throws IOException {
        Files.createDirectories(dir);
        Path path = dir.resolve(name);
        Files.createSymbolicLink(path, targetDir.resolve(targetName).toAbsolutePath());
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

    private void provideJar(Path dir, Path classpathRootDir, String name) throws IOException {
        Files.createDirectories(dir);
        Path path = dir.resolve(name);
        try (ZipOutputStream zip = ZipUtils.file(path)) {
            Files.walkFileTree(classpathRootDir, PostOrderVisitor.with(resourcePath -> {
                if (!Files.isRegularFile(resourcePath)) return;
                String resourceName = classpathRootDir.relativize(resourcePath).toString();
                byte[] content = ResourceUtils.load(resourceName);
                ZipUtils.entry(zip, resourceName, content);
            }));
        }
    }

    private ProcessResult runGradle(String... tasks) throws IOException, InterruptedException {
        return run("./gradlew", true, tasks);
    }

    private ProcessResult run(String executable, boolean inheritIo, String... args) throws IOException, InterruptedException {
        List<String> commandLine = new ArrayList<>();
        commandLine.add(executable);
        commandLine.addAll(Arrays.asList(args));

        ProcessBuilder processBuilder = new ProcessBuilder()
                .directory(projectRoot.toFile())
                .command(commandLine);
        if (inheritIo) processBuilder.inheritIO();

        ProcessResult result = ProcessResult.of(processBuilder);

        int exitValue = result.getExitValue();
        if (exitValue != 0) assertThat(result.getError(), exitValue, equalTo(0));

        return result;
    }

}
