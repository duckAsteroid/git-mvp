package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.cmd.ProcessResult;

import java.util.List;

public class GitException extends RuntimeException {
	private final int code;
	private final List<String> command;
	public GitException(List<String> command, int code, String message) {
		super(String.join(" ", command) + ":" + code+" ->" + message);
		this.code = code;
		this.command = command;
	}

	public static void check(List<String> command, ProcessResult result) {
		if (result.getExitCode() != 0) {
			throw new GitException(command, result.getExitCode(), result.getErrorOutput());
		}
	}

	public int getCode() {
		return code;
	}

	public List<String> getCommand() {
		return command;
	}

}
