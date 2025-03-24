package io.github.duckasteroid.git.mvp.tasks;

import io.github.duckasteroid.git.mvp.GitTag;
import io.github.duckasteroid.git.mvp.GitUtils;
import io.github.duckasteroid.git.mvp.Version;
import io.github.duckasteroid.git.mvp.VersionSource;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.util.List;

import static io.github.duckasteroid.git.mvp.GitUtils.*;


/**
 * A task added to projects by this plugin.
 * It prints out sime
 */
public class ExplainVersion extends DefaultTask {
    @TaskAction
    public void printVersion() {
        List<VersionSource> versions = taggedVersions(getProject());
        String projectRepoPath = getProject().getRootDir().toPath().relativize(getProject().getProjectDir().toPath()).toString();
        System.out.println("Project path /" + projectRepoPath + "=" + getProject().getVersion());
        System.out.println("\tversionSource=" + versions.get(0));
        if (versions.get(0) instanceof GitTag) {
            int commits = GitUtils.instance().gitCommitCount(versions.get(0).name(), projectRepoPath);
            System.out.println("\tcommits=" + commits);
            Version v = versions.get(0).asVersion();
            System.out.println("\tnumeric=" + v.hasNumericSegment());
            if (v.hasNumericSegment()) {
                System.out.println("\tupdated=" + v.addToLast(commits));
            }
        }

        System.out.println("\tdirty=" + GitUtils.instance().gitDirty(projectRepoPath));
        System.out.println("Using the following version sources:");
        versions.forEach((it) -> {
            System.out.println("\t" + it.toString());
        });
    }
}


