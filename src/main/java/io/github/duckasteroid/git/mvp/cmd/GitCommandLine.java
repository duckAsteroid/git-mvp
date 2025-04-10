package io.github.duckasteroid.git.mvp.cmd;

import io.github.duckasteroid.git.mvp.Change;
import io.github.duckasteroid.git.mvp.Git;
import io.github.duckasteroid.git.mvp.GitException;
import io.github.duckasteroid.git.mvp.version.source.GitTag;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;


import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An implementation of the {@link Git} API that uses the git executable and command line switches.
 */
public class GitCommandLine implements Git {

	private static final Logger log = Logging.getLogger(GitCommandLine.class);

	private final Path workingDir;

	/**
	 * Construct to operate in the given working directory. If null this operates in the default
	 * working directory of this process.
	 * @param workingDirectory the working directory
	 */
	public GitCommandLine(Path workingDirectory) {
		this.workingDir = workingDirectory;
	}

	@Override
	public Optional<Path> getRootDir() {
		return gitRootDir(workingDir.toFile());
	}

	@Override
	public Path getWorkingDir() {
		return workingDir;
	}

	/**
	 * For a given file, find the root of the git repository that contains it
	 * @param somewhere the file or folder that might be in a git repository
	 * @return the root of the git repository (if any)
	 */
	public static Optional<Path> gitRootDir(File somewhere) {
		//git rev-parse --show-toplevel
		ProcessResult rootCommand = withGit(List.of("rev-parse", "--show-toplevel"), somewhere.toPath(), false);
		if (rootCommand.getExitCode() != 0) {
			return Optional.empty();
		}
		return rootCommand.output().findFirst().map(Paths::get);
	}

	@Override
	public String gitCommitID(boolean shortVersion, String path) {
		ArrayList<String> args = new ArrayList<String>();
		if (path == null || path.isBlank()) {
			args.addAll(Arrays.asList("rev-parse", "HEAD"));
			if (shortVersion) {
				args.add(1, "--short");
			}
		}
		else {
			args.addAll(Arrays.asList("log", "-1"));
			if (shortVersion) {
				args.add("--pretty=%h");
			}
			else {
				args.add("--pretty=%H");
			}
			args.add(path);
		}
		return withGit(args, workingDir, true).output().findFirst().orElseThrow();
	}

	@Override
	public int gitCommitCount(String tag, String path) {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("rev-list", "--count", tag + "..HEAD"));
		if (path != null && !path.isBlank()) {
			args.addAll(Arrays.asList("--", path));
		}
		return withGit(args, workingDir, true).output().findFirst().map(Integer::parseInt).orElseThrow();
	}

	@Override
	public String branchName() {
		var args = new ArrayList<String>(Arrays.asList("rev-parse", "--abbrev-ref", "HEAD"));
		return withGit(args, workingDir, true).output().findFirst().orElseThrow();
	}

	@Override
	public List<GitTag> gitTags(String pattern) {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("for-each-ref", "--sort=-committerdate", "--format=\"" + GitTag.formatString() + "\""));
		if (pattern == null || pattern.isBlank()) {
			args.add("refs/tags");
		} else {
			args.add("refs/tags/" + pattern);
		}
		Supplier<String> explanation = () -> "git tags for "+ args.get(args.size() - 1);
		return withGit(args, workingDir, true).output().map(tag -> GitTag.parse(explanation, tag)).toList();
	}

	@Override
	public boolean gitDirty(String pattern) {
		return !status(pattern).isEmpty();
	}

	@Override
	public List<Change> status(String pattern) {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("status", "--short", "-z"));
		if (pattern != null && !pattern.isBlank()) {
			args.addAll(Arrays.asList("--", pattern));
		}
		String output = withGit(args, workingDir, true).output().collect(Collectors.joining("\n"));
		if (!output.isEmpty()) {
			String[] changes = output.split(String.valueOf(Git.NULL_CHAR));
			if (changes.length > 0) {
				return Arrays.stream(changes)
								.map(String::trim)
								.filter(Predicate.not(String::isBlank))
								.map(Change::from)
								.toList();
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Run a set of git args
	 *
	 * @param args         the args for git
	 * @param workingDir   (optional) a working directory for the git process
	 * @param throwOnError should we throw @{@link GitException} if the return code != 0
	 * @return the result of running the git process
	 * @throws RuntimeException If git returns an error
	 */
	public static ProcessResult withGit(List<String> args, @Nullable Path workingDir, boolean throwOnError) {
		var command = new ArrayList<String>();
		command.add("git");
		command.addAll(args);
		ProcessBuilder pb = new ProcessBuilder(command);
		try {
			if (workingDir != null) {
				pb.directory(workingDir.toFile());
			}
			//System.out.println(">> "+String.join(" ", command));
			Process p = pb.start();
			var result = ProcessResult.from(p);
			//System.out.printf("Git command result: %s\n%s\n------------\n", result.getExitCode(), result);
			if (throwOnError) {
				GitException.check(command, result);
			}
			return result;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	//region Testing only methods

	/**
	 * Commit the current stage with a message
	 * @param message Message for the commit
	 * @return The commit ID
	 */
	public String commit(String message) {
		ProcessResult commitResult = withGit(List.of("commit","-a","-m",message), workingDir, true);

		String line1 = commitResult.output().findFirst().orElseThrow();
		var regex = Pattern.compile("\\[.*?\\s([a-f0-9]+)\\]");
		Matcher matcher = regex.matcher(line1);
		if (matcher.find()) {
			final String commitId = matcher.group(1);
			System.out.println("commited ID=" + commitId);
			return commitId;
		}
		else throw new IllegalStateException("No commit id found");
	}

	/**
	 * Create a lightweight tag with given name at the current HEAD
	 * @param tagName the tage name
	 */
	public void lightTag(String tagName) {
		GitCommandLine.withGit(List.of("tag", tagName), workingDir, true);
	}

	/**
	 * Create an annotated tag with given name at the current HEAD
	 * @param tagName the tage name
	 * @param message the message or annotation
	 */
	public void annotatedTag(String tagName, String message) {
		GitCommandLine.withGit(List.of("tag", "-a", tagName, "-m", message), workingDir, true);
	}

	/**
	 * Add the contents of the given path to the repo
	 * @param path the path to add
	 */
	public void add(String path) {
		GitCommandLine.withGit(List.of("add",path), workingDir, true);
	}

	/**
	 * Create and check out a new branch
	 * @param branchName the name of the new branch
	 */
	public void newBranch(String branchName) {
		GitCommandLine.withGit(List.of("checkout", "-b", branchName), workingDir, true);
	}

	//endregion

}
