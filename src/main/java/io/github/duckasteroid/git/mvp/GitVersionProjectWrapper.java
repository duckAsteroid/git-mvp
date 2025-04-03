package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.cmd.GitCommandLine;
import io.github.duckasteroid.git.mvp.ext.GitVersionExtension;
import io.github.duckasteroid.git.mvp.version.Version;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A wrapper for a Gradle {@link Project} that provides git version utilities
 */
public class GitVersionProjectWrapper {
	// the Gradle project
	private final Project project;
	// a git command line - working in the root project directory
	private final GitCommandLine git;

	public GitVersionProjectWrapper(final Project project) {
		this.project = project;
		this.git = new GitCommandLine(project.getRootProject().getProjectDir().toPath());
	}

	public Path gitRootDir() {
		return git.getRootDir().orElseThrow();
	}

	public Optional<GitVersionExtension> extension() {
		GitVersionExtension ext = project.getExtensions().findByType(GitVersionExtension.class);
		return Optional.ofNullable(ext);
	}

	/**
	 * Retrieve the candidate sources of git based version information for a project.
	 * The following are returned in order of preference:
	 * <ol>
	 *     <li>Git version tags in a "folder" matching the project path</li>
	 *     <li>Git version tags (vXXX) with no path (e.g. v1.0.0)</li>
	 *     <li>The short form of the last commit ID on the current branch</li>
	 * </ol>
	 *
	 * @return
	 */
	public List<VersionSource> candidateVersions() {
		// the fallback if we can't find something more specific...
		// the latest commit ID that affects the project path
		Path projectGitPath = getGitRelativePath();
		VersionSource commitId = new Commit(() -> "Commit ID on project @ " + projectGitPath, git.gitCommitID(true, projectGitPath.toString()));

		// path is preceded by ':'
		String path = project.getPath().substring(1).trim();
		// get the generic tags (these will come after project specific ones in the result)
		List<VersionSource> tags = new ArrayList<>(git.gitTags("v*"));
		if (!path.isBlank()) {
			path += "/";
			// lets try to find some project tags and add them to the front of the list
			tags.addAll(0, git.gitTags(path));
		}
		// add the commit id to the end of the list
		tags.add(tags.size(), commitId);

		return tags;
	}

	/**
	 * Get a set of proposed amendments to a candidate version based on the state of the repo
	 * @param versionSource the source of versions to amend
	 * @return a list of amendments
	 */
	public List<VersionAmendment> amendments(VersionSource versionSource) {
		List<VersionAmendment> amendments = new ArrayList<>(2);
		String projectRepoPath = getGitRelativePath().toString();
		final String branchName = git.branchName();
		if (isAutoIncrementedBranch(branchName)) {
			// if the source of the last version is a tag
			if (versionSource.type() == VersionSource.Type.TAG) {
				// FIXME are there changes on this branch, in that project, since that tag?
				//def changes = gitDiff(versionSource.name(), projectRepoPath)
				// and the tag is incrementable (i.e. has numbers)
				if (versionSource.version().isIncrementable()) {
					// how many commits since that tag
					final int commits = git.gitCommitCount(versionSource.value(), projectRepoPath);
					if (commits > 0) {
						Function<Version,Version> amender = (input) -> input.increment(commits);
						amendments.add(new VersionAmendment(
										"Increment version, found " + commits + " commits in path "+projectRepoPath+
														", on an incrementable branch '"+branchName+"' ",
										amender));
					}
				}
			}
		}
		boolean dirty = git.gitDirty(projectRepoPath);
		if (dirty) {
			// FIXME get from extension
			final String qualifier = extension()
							.map(GitVersionExtension::getDirtyQualifier)
							.map(Property::get)
							.orElse("dirty");
			amendments.add(new VersionAmendment(
							"Add '"+qualifier+"' qualifier, as repository is dirty",
							(input) -> input.withQualifier(qualifier)));
		}
		return amendments;
	}

	public boolean isAutoIncrementedBranch(String s) {
		// FIXME use extension values
		return !(s.equals("main") || s.equals("master"));
	}

	/**
	 * Retrieve the git version for a project.
	 * The following are used in order of preference:
	 * <ol>
	 *     <li>Git version tags in a "folder" matching the project path</li>
	 *     <li>Git version tags (vXXX) with no path (e.g. v1.0.0)</li>
	 *     <li>The short form of the last commit ID on the current branch</li>
	 * </ol>
	 * @return
	 */
	public String gitVersion() {
		List<VersionSource> candidates = candidateVersions();
		// first source is the one we choose
		VersionSource selectedSource = candidates.stream().findFirst().orElseThrow();
		// any amendments required for that version?
		List<VersionAmendment> amendments = amendments(selectedSource);

		Version version = selectedSource.version();
		for (VersionAmendment amendment : amendments) {
			version = amendment.amended(version);
		}

		return version.toString();
	}

	public Git getGit() {
		return git;
	}

	public Path getProjectIdPath() {
		return Path.of(project.getPath().replace(':', '/'));
	}

	/**
	 * The path of this project directory - relative to the git repository root
	 * @return the path to this project from the root of the repo
	 */
	public Path getGitRelativePath() {
		return gitRootDir().relativize(project.getProjectDir().toPath());
	}
}
