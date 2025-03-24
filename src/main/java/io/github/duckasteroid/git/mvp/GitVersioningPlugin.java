package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.ext.GitVersionExtension;
import io.github.duckasteroid.git.mvp.tasks.ExplainVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class GitVersioningPlugin implements Plugin<Project> {
	@Override
	public void apply(Project target) {
		// our extension for project settings/config
		GitVersionExtension gitVersionExtension = target.getExtensions().create("gitVersion", GitVersionExtension.class, target);
		// Add the explain task
		TaskProvider<ExplainVersion> explainVersion = target.getTasks().register("explainVersion", ExplainVersion.class);
		explainVersion.configure(explain -> explain.setGroup("Versioning"));
	}
}
