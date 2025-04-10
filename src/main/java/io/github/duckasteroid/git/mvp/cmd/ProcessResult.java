package io.github.duckasteroid.git.mvp.cmd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The result of running a git process
 */
public class ProcessResult {
	private final List<String> output;
	private final String errorOutput;
	private final int exitCode;

	/**
	 * Create from the result of running a process
	 * @param output the lines of output
	 * @param errorOutput the error output (if any)
	 * @param exitCode the exit code of the process
	 */
	public ProcessResult(List<String> output, String errorOutput, int exitCode) {
		this.output = output;
		this.errorOutput = errorOutput;
		this.exitCode = exitCode;
	}

	/**
	 * Create an instance from a running or run process.
	 * This waits for the process to finish if necessary.
	 * @param p the process
	 * @return a process result
	 */
	public static ProcessResult from(Process p) {
		try {
			var output = new BufferedReader(new InputStreamReader(p.getInputStream())).lines().toList();
			String errorOutput = new BufferedReader(new InputStreamReader(p.getErrorStream())).lines().collect((Collector<? super String, ?, String>) Collectors.joining("\n"));
			int exitCode = p.waitFor();
			return new ProcessResult(output, errorOutput, exitCode);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * The standard output
	 * @return output
	 */
	public Stream<String> output() {
		return output.stream();
	}

	/**
	 * The standard error output
	 * @return error output
	 */
	public final String getErrorOutput() {
		return errorOutput;
	}

	/**
	 * The return/exit code of the process
	 * @return the exit code
	 */
	public final int getExitCode() {
		return exitCode;
	}

	@Override
	public String toString() {
		return String.join("\n", output);
	}
}
