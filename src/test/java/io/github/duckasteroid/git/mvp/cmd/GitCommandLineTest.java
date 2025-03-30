package io.github.duckasteroid.git.mvp.cmd;
import io.github.duckasteroid.git.mvp.GitException;
import org.gradle.internal.impldep.org.eclipse.jgit.api.GitCommand;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class GitCommandLineTest {
	@TempDir
	Path gitRepositoryPath;

	GitCommandLine git = new GitCommandLine();

	@BeforeEach
	void setUp() throws IOException {
		GitCommandLine.withGit(List.of("init"), gitRepositoryPath);

		// add a test directory structure
		// test/file.txt
		// test/example/file.txt
		// test/other/file.txt
		Path test = gitRepositoryPath.resolve("test");
		Files.createDirectory(test);

		Path fileTxt = test.resolve("file.txt");
		Files.writeString(fileTxt, "Hello World 1", StandardCharsets.UTF_8, StandardOpenOption.CREATE);

		Path example = test.resolve("example");
		Files.createDirectory(example);

		Path file = example.resolve("file.txt");
		Files.writeString(file, "Hello World 2", StandardCharsets.UTF_8, StandardOpenOption.CREATE);

		Path other = test.resolve("other");
		Files.createDirectory(other);

		Path file2 = other.resolve("file.txt");
		Files.writeString(file2, "Hello World 3", StandardCharsets.UTF_8, StandardOpenOption.CREATE);

		// commit all these to git

		ProcessResult result = GitCommandLine.withGit(List.of("commit","-a","-m","Your commit message here"));
		System.out.println(result);

	}

	@Test
	void gitRootDir(@TempDir Path tempDir) {

	}
}