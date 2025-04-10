package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.branch.BranchRule;
import io.github.duckasteroid.git.mvp.cmd.GitCommandLine;
import io.github.duckasteroid.git.mvp.ext.GitVersionExtension;
import io.github.duckasteroid.git.mvp.ext.PatternSet;
import io.github.duckasteroid.git.mvp.version.Version;
import io.github.duckasteroid.git.mvp.version.source.Commit;
import io.github.duckasteroid.git.mvp.version.source.VersionSource;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A wrapper for a Gradle {@link Project} that provides git version utilities
 */
public class GitVersionProjectWrapper {
	// the Gradle project
	private final Project project;
	// a git command line - working in the root project directory
	private final GitCommandLine git;

	/**
	 * Create a wrapper for the given Gradle project
	 * @param project a Gradle project
	 */
	public GitVersionProjectWrapper(final Project project) {
		this.project = project;
		this.git = new GitCommandLine(project.getRootProject().getProjectDir().toPath());
	}

	/**
	 * The root directory of the git repository that contains this project
	 * @return the path of the git repository
	 */
	public Path gitRootDir() {
		return git.getRootDir().orElseThrow();
	}

	/**
	 * Get the extension for this plugin from the project
	 * @return a {@link GitVersionExtension}, if declared
	 */
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
	 * @return a list of version sources in order
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
			// get qualifier from extension
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

	/**
	 * A set of branch naming rules for determining if auto incrementing is applied
	 * @return a list of branch rules
	 */
	public List<BranchRule> branchRules() {
		if (extension().isPresent()) {
			ArrayList<BranchRule> result = new ArrayList<>();
			GitVersionExtension ext = extension().get();
			PatternSet autoIncrementedBranches = ext.getAutoIncrementBranches();
			// to match the branch must be included and not excluded
			// an empty included set matches (includes) all
			// an empty excluded set does not match (exclude) any


			// includes ...
			{
				List<String> includes = autoIncrementedBranches.getIncludes().get();
				BranchRule includeRule;
				if (includes != null && !includes.isEmpty()) {
					includeRule = new BranchRule("Includes: " + includes.stream().collect(Collectors.joining(",", "[", "]")),
									includes::contains);
				} else {
					includeRule = new BranchRule("No includes specified, default include all", s -> true);
				}
				result.add(includeRule);
			}

			// excludes...
			{
				List<String> excludes = autoIncrementedBranches.getExcludes().get();
				BranchRule excludeRule;
				if (excludes != null && !excludes.isEmpty()) {
					excludeRule = new BranchRule("Excludes: " + excludes.stream().collect(Collectors.joining(",", "[", "]")),
									Predicate.not(excludes::contains));
				} else {
					excludeRule = new BranchRule("No excludes specified, default exclude none", s -> true);
				}
				result.add(excludeRule);
			}
			return result;
		}
		else {
			BranchRule defaultBranchRule = new BranchRule("Branch is not 'main' or 'master",
							s -> !(s.equals("main") || s.equals("master")));
			return List.of(defaultBranchRule);
		}
	}

	/**
	 * Use the {@link #branchRules()} to determine if the named branch is auto incremented
	 * @param s the name of a branch (i.e. the current one)
	 * @return true if the version is to be auto-incremented
	 */
	public boolean isAutoIncrementedBranch(String s) {
		// use extension values
		List<BranchRule> rules = branchRules();
		for (BranchRule rule : rules) {
			if (!rule.rule().test(s)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Retrieve the git version for a project.
	 * The following are used in order of preference:
	 * <ol>
	 *     <li>Git version tags in a "folder" matching the project path</li>
	 *     <li>Git version tags (vXXX) with no path (e.g. v1.0.0)</li>
	 *     <li>The short form of the last commit ID on the current branch</li>
	 * </ol>
	 * If the branch is auto-incrementable (default is not "main" or "master"), then the following
	 * version modifiers are applied:
	 * <ol>
	 *   <li>The plugin calculates the number of commits (on the current branch) that "touch" the
	 *   path for this project (or some subpath).</li>
	 *   <li>This number increments the version number by that count</li>
	 *   <li>If the working copy is "dirty" (i.e. Git determines there are changes that would need
	 *   committing).
	 *   The version qualifier is marked with the dirty qualifier (default "-dirty")</li>
	 * </ol>
	 * @return the automatically calculated version string
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

	/**
	 * Primarily intended for unit tests to increment the version
	 */
	public void update() {
		project.setVersion(gitVersion());
	}

	/**
	 * Get the Git used by this to get data from the project
	 * @return a git instance
	 */
	public Git getGit() {
		return git;
	}

	/**
	 * The path of this project directory - relative to the git repository {@link #gitRootDir() root}
	 * @return the path to this project from the root of the repo
	 */
	public Path getGitRelativePath() {
		return gitRootDir().relativize(project.getProjectDir().toPath());
	}
}
