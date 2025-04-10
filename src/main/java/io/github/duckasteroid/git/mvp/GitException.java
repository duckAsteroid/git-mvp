package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.cmd.ProcessResult;

import java.util.List;

/**
 * An exception thrown by {@link io.github.duckasteroid.git.mvp.cmd.GitCommandLine}
 */
public class GitException extends RuntimeException {
	/**
	 * The exit code
	 */
	private final int code;
	/**
	 * The git command used
	 */
	private final List<String> command;

	/**
	 * Create an exception for the given result
	 * @param command the command that generated the result
	 * @param code the error code returned from git
	 * @param message the message returned from git
	 */
	public GitException(List<String> command, int code, String message) {
		super(String.join(" ", command) + ":" + code+" ->" + message);
		this.code = code;
		this.command = command;
	}

	/**
	 * Check the process result and throw an instance of this exception if required
	 * @param command the command that generated the result
	 * @param result the result to check
	 */
	public static void check(List<String> command, ProcessResult result) {
		if (result.getExitCode() != 0) {
			throw new GitException(command, result.getExitCode(), result.getErrorOutput());
		}
	}

	/**
	 * Get the return code from git
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Get the git command that generated the code
	 * @return the command
	 */
	public List<String> getCommand() {
		return command;
	}

}
