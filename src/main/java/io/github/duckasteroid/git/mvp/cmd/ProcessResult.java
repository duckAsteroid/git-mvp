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

	public ProcessResult(List<String> output, String errorOutput, int exitCode) {
		this.output = output;
		this.errorOutput = errorOutput;
		this.exitCode = exitCode;
	}

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

    public Stream<String> output() {
      return output.stream();
    }

    public final String getErrorOutput() {
        return errorOutput;
    }

    public final int getExitCode() {
        return exitCode;
    }
}
