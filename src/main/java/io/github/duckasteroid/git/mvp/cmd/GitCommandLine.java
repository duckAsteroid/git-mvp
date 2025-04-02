package io.github.duckasteroid.git.mvp.cmd;

import io.github.duckasteroid.git.mvp.Git;
import io.github.duckasteroid.git.mvp.GitException;
import io.github.duckasteroid.git.mvp.GitTag;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;


import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitCommandLine implements Git {

	private static final Logger log = Logging.getLogger(GitCommandLine.class);

	private final Path rootDir;

	public GitCommandLine(Path ctx) {
		this.rootDir = gitRootDir(ctx.toFile()).orElseThrow(() -> new IllegalArgumentException("No git repo"));
	}

	@Override
	public Path getRootDir() {
		return rootDir;
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
		return withGit(args, rootDir, true).output().findFirst().orElseThrow();
	}

	@Override
	public int gitCommitCount(String tag, String path) {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("rev-list", "--count", tag + "..HEAD"));
		if (path != null && !path.isBlank()) {
			args.addAll(Arrays.asList("--", path));
		}
		return withGit(args, rootDir, true).output().findFirst().map(Integer::parseInt).orElseThrow();
	}

	@Override
	public String branchName() {
		var args = new ArrayList<String>(Arrays.asList("rev-parse", "--abbrev-ref", "HEAD"));
		return withGit(args, rootDir, true).output().findFirst().orElseThrow();
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
		return withGit(args, rootDir, true).output().map(tag -> GitTag.parse(explanation, tag)).toList();
	}

	@Override
	public boolean gitDirty(String pattern) {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("status", "--short", "-z"));
		if (pattern != null && !pattern.isBlank()) {
			args.addAll(Arrays.asList("--", pattern));
		}
		return withGit(args, rootDir, true).output().anyMatch(Predicate.not(String::isBlank));
	}

	/**
	 * Run a set of git args
	 *
	 * @param args         the args for git
	 * @param workingDir   (optional) a working directory for the git process
	 * @param throwOnError
	 * @return a result if exitCode == 0
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
			System.out.println(">> "+String.join(" ", command));
			Process p = pb.start();
			var result = ProcessResult.from(p);
			System.out.printf("Git command result: %s\n%s\n------------\n", result.getExitCode(), result);
			if (throwOnError) {
				GitException.check(command, result);
			}
			return result;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	//region Testing only methods

	public String commit(String message) {
		ProcessResult commitResult = withGit(List.of("commit","-a","-m",message), rootDir, true);

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

	public void lightTag(String tagName) {
		GitCommandLine.withGit(List.of("tag", tagName), rootDir, true);
	}

	public void annotatedTag(String tagName, String message) {
		GitCommandLine.withGit(List.of("tag", "-a", tagName, "-m", message), rootDir, true);
	}

	public void add(String path) {
		GitCommandLine.withGit(List.of("add",path), rootDir, true);
	}

	public void newBranch(String branchName) {
		GitCommandLine.withGit(List.of("checkout", "-b", branchName), rootDir, true);
	}

	//endregion

}
