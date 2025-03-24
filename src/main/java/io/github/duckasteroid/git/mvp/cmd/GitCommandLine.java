package io.github.duckasteroid.git.mvp.cmd;

import io.github.duckasteroid.git.mvp.Git;
import io.github.duckasteroid.git.mvp.GitException;
import io.github.duckasteroid.git.mvp.GitTag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class GitCommandLine implements Git {
	/**
	 * Run a set of git args
	 * @param args the args for git
	 * @return a result if exitCode == 0
	 * @throws RuntimeException If git returns an error
	 */
	public static ProcessResult withGit(List<String> args) {
		var command = new ArrayList<String>();
		command.add("git");
		command.addAll(args);
		ProcessBuilder pb = new ProcessBuilder(command);
		try {
			Process p = pb.start();
			var result = ProcessResult.from(p);
			GitException.check(command, result);
			return result;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
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
		return withGit(args).output().findFirst().orElseThrow();
	}

	@Override
	public int gitCommitCount(String tag, String path) {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("rev-list", "--count", tag + "..HEAD"));
		if (path != null && !path.isBlank()) {
			args.addAll(Arrays.asList("--", path));
		}
		return withGit(args).output().findFirst().map(Integer::parseInt).orElseThrow();
	}

	@Override
	public String branchName() {
		var args = new ArrayList<String>(Arrays.asList("rev-parse", "--abbrev-ref", "HEAD"));
		return withGit(args).output().findFirst().orElseThrow();
	}

	@Override
	public List<GitTag> gitTags(String pattern) {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("for-each-ref", "--sort=-committerdate", "--format=\"" + GitTag.formatString() + "\""));
		if (pattern == null || pattern.isBlank()) {
			args.add("refs/tags");
		} else {
			args.add("refs/tags/" + pattern);
		}
		return withGit(args).output().map(GitTag::parse).toList();
	}

	@Override
	public boolean gitDirty(String pattern) {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("status", "--short", "-z"));
		if (pattern != null && !pattern.isBlank()) {
			args.addAll(Arrays.asList("--", pattern));
		}
		return withGit(args).output().anyMatch(Predicate.not(String::isBlank));
	}
}
