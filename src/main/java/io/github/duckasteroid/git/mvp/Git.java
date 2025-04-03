package io.github.duckasteroid.git.mvp;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * The interface between this plugin and the underlying Git VCS.
 */
public interface Git {
	char NULL_CHAR = '\0';
	/**
	 * The path to the root of the git repo being used
	 * @return a filesystem path
	 */
	Optional<Path> getRootDir();

	/**
	 * The path to the working directory of the git process (often the same as {@link #getRootDir()})
	 * @return a filesystem path
	 */
	Path getWorkingDir();

	/**
	 * Get the last commit ID (short or long) that touched the given path
	 * If no path is specified, it's simply the last commit in the repo (HEAD)
	 *
	 * @param shortVersion short or long commit ID
	 * @param path An optional path to filter for commit (or null)
	 * @return the commit ID
	 */
	String gitCommitID(boolean shortVersion, String path);
	/**
	 * Get the number of commits (that "touched" an optional path) since a given tag
	 *
	 * @param tag  the tag to start from
	 * @param path the path to check
	 * @return the number of commits
	 */
	int gitCommitCount(String tag, String path);

	/**
	 * Gets the current branch name
	 *
	 * @return the branch
	 */
	String branchName();
	/**
	 * Get the git tags in the repo, that optionally match a given pattern
	 *
	 * @param pattern (optional) the pattern of the tag (or null)
	 * @return a list of tags most recent commit first
	 */
	List<GitTag> gitTags(@Nullable String pattern);
	/**
	 * Is the repository dirty?
	 * Optionally, check only the given path pattern.
	 *
	 * @param pattern (optional) a pattern to restrict the check to, or null
	 * @return true if dirty (e.g. uncommited stuff in the repo for the given path)
	 */
	boolean gitDirty(@Nullable String pattern);

	/**
	 * Pending changes in the git repo (from git status)
	 * @param pattern (optional) a pattern to restrict the check to, or null
	 * @return a list of changes (maybe empty, never null)
	 */
	List<Change> status(@Nullable String pattern);
}
