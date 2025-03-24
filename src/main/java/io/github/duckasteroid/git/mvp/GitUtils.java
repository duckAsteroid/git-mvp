package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.cmd.GitCommandLine;
import org.gradle.api.Project;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GitUtils {
	private static final Git SINGLETON = new GitCommandLine();

	public static Git instance() {
		return SINGLETON;
	}


	/**
	 * Retrieve the git version for a project.
	 * The following are used in order of preference:
	 * <ol>
	 *     <li>Git version tags in a "folder" matching the project path</li>
	 *     <li>Git version tags (vXXX) with no path (e.g. v1.0.0)</li>
	 *     <li>The short form of the last commit ID on the current branch</li>
	 * </ol>
	 *
	 * @param project
	 * @return
	 */
	public static List<VersionSource> taggedVersions(Project project) {
		// the fallback if we can't find something more specific...
		VersionSource commitId = new Commit(SINGLETON.gitCommitID(true, path(project).toString()));

		// path is preceded by ':'
		String path = project.getPath().substring(1).trim();
		List<VersionSource> tags = new ArrayList<>(SINGLETON.gitTags("v*"));
		if (!path.isBlank()) {
			path += "/";
			// lets try to find some project tags
			tags.addAll(0, SINGLETON.gitTags(path));
		}
		// add the commit id
		tags.add(0, commitId);

		return tags;
	}

	public static Path path(Project project) {
		return project.getProjectDir().toPath();
	}

	/**
	 * Retrieve the git version for a project.
	 * The following are used in order of preference:
	 * <ol>
	 *     <li>Git version tags in a "folder" matching the project path</li>
	 *     <li>Git version tags (vXXX) with no path (e.g. v1.0.0)</li>
	 *     <li>The short form of the last commit ID on the current branch</li>
	 * </ol>
	 * @param project
	 * @return
	 */
	public static String gitVersion(Project project) {
		List<VersionSource> candidates = taggedVersions(project);
		VersionSource versionSource = candidates.stream().filter(VersionSource::hasVersion).findFirst().orElseThrow();
		String projectRepoPath = project.getRootDir().toPath().relativize(project.getProjectDir().toPath()).toString();

		String versionString = versionSource.version();

		if (versionSource instanceof GitTag) {
			// are there changes on this branch, in that project, since that tag?
			//def changes = gitDiff(versionSource.name(), projectRepoPath)
			// how many commits
			int commits = SINGLETON.gitCommitCount(versionSource.name(), projectRepoPath);
			// the basis version for computing updates
			Version starter = versionSource.asVersion();
			if (starter.hasNumericSegment() && commits > 0) {
				Version updated = starter.addToLast(commits);
				versionString = updated.toString();
			} else if (commits > 0) {
				versionString += "+" + commits;
			}

		}


		boolean dirty = SINGLETON.gitDirty(projectRepoPath);
		if (dirty) {
			versionString += "-dirty";
		}

		return versionString;
	}
}
