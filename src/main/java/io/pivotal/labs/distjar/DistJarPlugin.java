package io.pivotal.labs.distjar;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.util.Collections;

public class DistJarPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        DistJarTask distJarTask = createTask(project, "distJar", DistJarTask.class);
        distJarTask.dependsOn("distZip");
        distJarTask.setSourcePath(() -> getArchiveTask(project, "distZip").getArchivePath());
        distJarTask.setDestinationDir(() -> getArchiveTask(project, "distZip").getDestinationDir());
        distJarTask.setArchiveName(() -> getArchiveTask(project, "distZip").getArchiveName().replaceFirst("\\.zip$", ".jar"));
    }

    private <T> T createTask(Project project, String name, Class<T> type) {
        return type.cast(project.task(Collections.singletonMap("type", type), name));
    }

    private AbstractArchiveTask getArchiveTask(Project target, String name) {
        return (AbstractArchiveTask) target.getTasks().getByName(name);
    }

}
