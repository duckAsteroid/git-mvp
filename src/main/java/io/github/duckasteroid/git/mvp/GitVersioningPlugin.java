package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.cmd.GitCommandLine;
import io.github.duckasteroid.git.mvp.ext.GitVersionExtension;
import io.github.duckasteroid.git.mvp.tasks.ExplainVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.Optional;

/**
 * The root class for our git versioning plugin.
 * When applied it:
 * <ul>
 *   <li>Adds the {@link ExplainVersion} task</li>
 *   <li>Adds the {@link GitVersionExtension} extension</li>
 *   <li>If the {@link Project#getVersion()} is not set, sets it to {@link GitVersionProjectWrapper#gitVersion()}</li>
 * </ul>
 */
public class GitVersioningPlugin implements Plugin<Project> {
	/**
	 * The plugin ID
	 */
	public static final String ID = "io.github.duckasteroid.git-mvp";
	/**
	 * The task group for our {@link ExplainVersion} task
	 */
	public static final String GROUP = "versioning";

	@Override
	public void apply(Project target) {
		var log = target.getLogger();
		if (GitCommandLine.gitRootDir(target.getProjectDir()).isPresent()) {
			GitVersionProjectWrapper projectHelper = new GitVersionProjectWrapper(target);
			// our extension for project settings/config
			GitVersionExtension gitVersionExtension = target.getExtensions().create(GitVersionExtension.NAME, GitVersionExtension.class, target);
			// Add the explain task
			TaskProvider<ExplainVersion> explainVersion = target.getTasks().register(ExplainVersion.NAME, ExplainVersion.class);
			explainVersion.configure(explain -> explain.setGroup(GROUP));

			// is the version specified already?
			var version = target.getVersion().toString();
			if (version.equals(Project.DEFAULT_VERSION) || version.isEmpty()) {
				// if not specified - use our plugin to determine it
				target.setVersion(projectHelper.gitVersion());
			}
		}
		else {
			log.error("No git repository found for project @ {}, skipping", target.getProjectDir());
		}
	}
}
