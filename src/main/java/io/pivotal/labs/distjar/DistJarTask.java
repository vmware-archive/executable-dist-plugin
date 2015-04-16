package io.pivotal.labs.distjar;

import io.pivotal.labs.io.ManifestUtils;
import io.pivotal.labs.io.ResourceUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.function.Supplier;
import java.util.jar.Manifest;
import java.util.zip.CRC32;

public class DistJarTask extends DefaultTask {

    private Supplier<File> sourcePath;
    private Supplier<File> destinationDir;
    private Supplier<String> archiveName;
    private Supplier<File> archivePath;

    @TaskAction
    public void copy() throws IOException {
        getArchivePath().getParentFile().mkdirs();
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(getArchivePath()))) {
            byte[] buffer = new byte[32 * 1024];
            ZipFile in = new ZipFile(getSourcePath());
            try {
                copy(in, out, buffer);
            } finally {
                in.close();
            }

            Manifest manifest = ManifestUtils.create(Collections.singletonMap("Main-Class", Main.class.getName()));
            byte[] manifestData = ManifestUtils.toByteArray(manifest);
            writeEntry(out, "META-INF/MANIFEST.MF", manifestData);

            String mainClassResourceName = Main.class.getName().replace('.', '/') + ".class";
            byte[] mainClassData = ResourceUtils.load(mainClassResourceName);
            writeEntry(out, mainClassResourceName, mainClassData);
        }
    }

    private void writeEntry(ZipOutputStream out, String name, byte[] content) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(System.currentTimeMillis());
        entry.setUnixMode(0644);
        entry.setSize(content.length);
        entry.setCompressedSize(-1);
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());

        out.putNextEntry(entry);
        out.write(content);
        out.closeEntry();
    }

    private void copy(ZipFile in, ZipOutputStream out, byte[] buffer) throws IOException {
        Enumeration<ZipEntry> entries = in.getEntriesInPhysicalOrder();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            ZipEntry archiveEntry = new ZipEntry(entry);
            archiveEntry.setCompressedSize(-1);
            out.putNextEntry(archiveEntry);
            try (InputStream entryStream = in.getInputStream(entry)) {
                Main.copy(entryStream, out, buffer);
            }
            out.closeEntry();
        }
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
