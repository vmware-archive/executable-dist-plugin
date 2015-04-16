package io.pivotal.labs.exedist;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.util.Collections;

public class ExecutableDistZipPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        ExecutableDistZipTask task = createTask(project, "executableDistZip", ExecutableDistZipTask.class);
        task.dependsOn("distZip");
        task.setSourcePath(() -> getDistZip(project).getArchivePath());
        task.setDestinationDir(() -> getDistZip(project).getDestinationDir());
        task.setArchiveName(() -> getDistZip(project).getArchiveName().replaceFirst("\\.zip$", "-exe.zip"));
    }

    private <T> T createTask(Project project, String name, Class<T> type) {
        return type.cast(project.task(Collections.singletonMap("type", type), name));
    }

    private AbstractArchiveTask getDistZip(Project project) {
        return (AbstractArchiveTask) project.getTasks().getByName("distZip");
    }

}
