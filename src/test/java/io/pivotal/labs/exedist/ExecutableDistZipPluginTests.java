package io.pivotal.labs.exedist;

import io.pivotal.labs.io.ProcessResult;
import io.pivotal.labs.io.ResourceUtils;
import io.pivotal.labs.test.GradleProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static io.pivotal.labs.io.ProcessResultMatcher.hasOutput;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ExecutableDistZipPluginTests {

    private GradleProject project;

    @Before
    public void setUp() throws Exception {
        project = new GradleProject(getClass());
        project.installWrapper(Paths.get(""));
        project.setName("myapp");
    }

    @After
    public void tearDown() throws Exception {
        project.close();
    }

    @Test
    public void shouldInstallTheApplication() throws Exception {
        project.provide("build.gradle");
        project.provideLibrary("exedist.jar", pluginClasspathRoots());
        project.provideSource("Hello.java");

        project.runTasks("installDist");

        ProcessResult result = project.run("build/install/myapp/bin/myapp", false, "one", "two", "three");

        assertThat(result, hasOutput(equalTo("Hello, world!\none\ntwo\nthree\n")));
    }

    @Test
    public void shouldBuildAnExecutableDistZip() throws Exception {
        project.provide("build.gradle");
        project.provideLibrary("exedist.jar", pluginClasspathRoots());
        project.provideSource("Hello.java");

        project.runTasks("executableDistZip");

        ProcessResult result = project.run("build/distributions/myapp-exe.zip", false, "one", "two", "three");

        assertThat(result, hasOutput(equalTo("Hello, world!\none\ntwo\nthree\n")));
    }

    private String[] pluginClasspathRoots() {
        return new String[]{ResourceUtils.classFileName(ExecutableDistZipPlugin.class), "io/pivotal/labs/exedist/bootloader.sh"};
    }

}
