package io.pivotal.labs.exedist;

import io.pivotal.labs.io.ResourceUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

public class ExecutableDistZipTask extends DefaultTask {

    private Supplier<File> sourcePath;
    private Supplier<File> destinationDir;
    private Supplier<String> archiveName;
    private Supplier<File> archivePath;

    @TaskAction
    public void copy() throws IOException {
        getArchivePath().getParentFile().mkdirs();
        try (OutputStream out = new FileOutputStream(getArchivePath())) {
            try (InputStream script = ResourceUtils.open(getClass(), "bootloader.sh")) {
                ResourceUtils.copy(script, out);
            }
            try (InputStream zip = new FileInputStream(getSourcePath())) {
                ResourceUtils.copy(zip, out);
            }
        }
        makeExecutable(getArchivePath().toPath());
    }

    private static void makeExecutable(Path path) throws IOException {
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
        permissions.addAll(EnumSet.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_EXECUTE));
        Files.setPosixFilePermissions(path, permissions);
    }

    @InputFile
    public File getSourcePath() {
        return sourcePath.get();
    }

    public void setSourcePath(File sourcePath) {
        this.sourcePath = () -> sourcePath;
    }

    public void setSourcePath(Supplier<File> input) {
        this.sourcePath = input;
    }

    public File getDestinationDir() {
        return destinationDir.get();
    }

    public void setDestinationDir(File destinationDir) {
        this.destinationDir = () -> destinationDir;
    }

    public void setDestinationDir(Supplier<File> destinationDir) {
        this.destinationDir = destinationDir;
    }

    public String getArchiveName() {
        return archiveName.get();
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = () -> archiveName;
    }

    public void setArchiveName(Supplier<String> archiveName) {
        this.archiveName = archiveName;
    }

    @OutputFile
    public File getArchivePath() {
        return new File(getDestinationDir(), getArchiveName());
    }

    public void setArchivePath(Supplier<File> archivePath) {
        this.archivePath = archivePath;
    }

    public void setArchivePath(File archivePath) {
        setDestinationDir(archivePath.getParentFile());
        setArchiveName(archivePath.getName());
    }

}
