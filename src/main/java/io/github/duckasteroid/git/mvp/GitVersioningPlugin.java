package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.ext.GitVersionExtension;
import io.github.duckasteroid.git.mvp.tasks.ExplainVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitVersioningPlugin implements Plugin<Project> {


	@Override
	public void apply(Project target) {
		var log = target.getLogger();
		// our extension for project settings/config
		GitVersionExtension gitVersionExtension = target.getExtensions().create("gitVersion", GitVersionExtension.class, target);
		// Add the explain task
		TaskProvider<ExplainVersion> explainVersion = target.getTasks().register("explainVersion", ExplainVersion.class);
		explainVersion.configure(explain -> explain.setGroup("Versioning"));

		// use this plugin to determine version if not specified already
		var version = target.getVersion().toString();
		if (version.equals( "unspecified") || version.isEmpty()) {
			target.setVersion(GitUtils.gitVersion(target));
		}
	}
}
