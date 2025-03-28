package io.github.duckasteroid.git.mvp;

import java.util.List;

/**
 * The interface between this plugin and the underlying Git VCS.
 */
public interface Git {
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
	 * Get the git tags in the repo that match a given pattern
	 *
	 * @param pattern the pattern of the tag (or null)
	 * @return a list of tags most recent commit first
	 */
	List<GitTag> gitTags(String pattern);
	/**
	 * Is the repository dirty?
	 * Optionally, check only the given path pattern.
	 *
	 * @param pattern a pattern to restrict the check to, or null
	 * @return true if dirty (e.g. uncommited stuff in the repo for the given path)
	 */
	boolean gitDirty(String pattern);
}
