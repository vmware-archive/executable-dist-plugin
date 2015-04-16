package io.pivotal.labs.test;

import io.pivotal.labs.io.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GradleProject implements Closeable {

    private final Class<?> owner;
    private final Path projectRoot;

    public GradleProject(Class<?> owner) throws IOException {
        this.owner = owner;
        projectRoot = Files.createTempDirectory(owner.getSimpleName());
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public void setName(String name) throws IOException {
        provide("settings.gradle", bytes("rootProject.name = '" + name + "'\n"));
    }

    private ThrowingSupplier<InputStream, IOException> bytes(String content) {
        return () -> new ByteArrayInputStream(content.getBytes());
    }

    public void installWrapper(Path originDir) throws IOException {
        link("gradle", originDir);
        link("gradlew", originDir);
    }

    public void link(String name, Path originDir) throws IOException {
        link(name, originDir, name);
    }

    public void link(String name, Path originDir, String originName) throws IOException {
        Files.createSymbolicLink(destination(name), originDir.resolve(originName).toAbsolutePath());
    }

    public void provideSource(String name) throws IOException {
        provide("src/main/java/" + name, () -> ResourceUtils.open(owner, name));
    }

    public void provide(String name) throws IOException {
        provide(name, () -> ResourceUtils.open(owner, name));
    }

    public void provideLibrary(String name, String... exemplarResourceNames) throws IOException {
        Set<Path> classpathRootDirs = new HashSet<>();
        for (String exemplarResourceName : exemplarResourceNames) {
            classpathRootDirs.add(findClasspathRootDir(exemplarResourceName));
        }

        provide("lib/" + name, out -> {
            try (ZipOutputStream zip = ZipUtils.file(out)) {
                for (Path classpathRootDir : classpathRootDirs) {
                    Files.walkFileTree(classpathRootDir, PostOrderVisitor.with(resourcePath -> {
                        if (!Files.isRegularFile(resourcePath)) return;
                        String resourceName = classpathRootDir.relativize(resourcePath).toString();
                        byte[] content = ResourceUtils.load(resourceName);
                        ZipUtils.entry(zip, resourceName, content);
                    }));
                }
            }
        });
    }

    private Path findClasspathRootDir(String resourceName) throws IOException {
        URL resourceUrl = ResourceUtils.findResource(resourceName);
        Path resourceFile = toPath(resourceUrl);
        return subtract(resourceFile, Paths.get(resourceName));
    }

    private Path toPath(URL url) throws IOException {
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private Path subtract(Path absolute, Path relative) throws IOException {
        if (!absolute.endsWith(relative)) {
            throw new IOException(absolute + " is not " + relative);
        }
        return absolute.resolve("/").resolve(absolute.subpath(0, absolute.getNameCount() - relative.getNameCount()));
    }

    public void provide(String name, ThrowingSupplier<InputStream, IOException> content) throws IOException {
        provide(name, out -> {
            try (InputStream in = content.get()) {
                ResourceUtils.copy(in, out);
            }
        });
    }

    private void provide(String name, ThrowingConsumer<OutputStream, IOException> content) throws IOException {
        try (FileOutputStream out = new FileOutputStream(destination(name).toFile())) {
            content.accept(out);
        }
    }

    private Path destination(String name) throws IOException {
        Path path = projectRoot.resolve(name);
        Files.createDirectories(path.getParent());
        return path;
    }

    public ProcessResult runTasks(String... tasks) throws IOException, InterruptedException {
        return run("./gradlew", true, tasks);
    }

    public ProcessResult run(String executable, boolean inheritIo, String... args) throws IOException, InterruptedException {
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

    @Override
    public void close() throws IOException {
        Files.walkFileTree(projectRoot, PostOrderVisitor.with(Files::delete));
    }

}
