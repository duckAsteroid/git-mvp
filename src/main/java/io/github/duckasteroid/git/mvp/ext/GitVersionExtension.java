package io.github.duckasteroid.git.mvp.ext;

import io.github.duckasteroid.git.mvp.GitUtils;
import org.gradle.api.Project;

public class GitVersionExtension {
	private final Project project;
	/**
	 * A prefix used when searching for git tags for this project (and children)
	 * Default: no prefix
	 */
	public String tagPrefix = "";

	public GitVersionExtension(Project project) {
		this.project = project;
	}

	public String version() {
		return GitUtils.gitVersion(project);
	}
}
