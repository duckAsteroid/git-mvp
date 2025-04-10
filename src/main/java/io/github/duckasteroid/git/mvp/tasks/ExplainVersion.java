package io.github.duckasteroid.git.mvp.tasks;

import io.github.duckasteroid.git.mvp.GitVersionProjectWrapper;
import io.github.duckasteroid.git.mvp.VersionAmendment;
import io.github.duckasteroid.git.mvp.version.source.VersionSource;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.util.List;



/**
 * A task added to projects by this plugin.
 * It prints out sime
 */
public class ExplainVersion extends DefaultTask {
    public static final String NAME = "explainVersion";

    @TaskAction
    public void printVersion() {
        GitVersionProjectWrapper projectHelper = new GitVersionProjectWrapper(getProject());
        List<VersionSource> versions = projectHelper.candidateVersions();
        String projectRepoPath = getProject().getRootDir().toPath().relativize(getProject().getProjectDir().toPath()).toString();
        System.out.println("Project: '"+getProject().getName()+"'");
        System.out.println("\tpath=" + projectRepoPath + "/");
        System.out.println("\tversion=" + getProject().getVersion());
        if (!versions.isEmpty()) {
            System.out.println("How:");
            System.out.println("\tsource=" + versions.get(0));
            List<VersionAmendment> amendments = projectHelper.amendments(versions.get(0));
            amendments.forEach(amendment -> {
                System.out.println("\tamendment=" + amendment.description());
            });
            System.out.println("Considered the following version sources:");
            versions.forEach((it) -> {
                System.out.println("\t" + it.toString());
            });
        }
        else {
            System.out.println("\tNo version data found");
        }
    }
}


