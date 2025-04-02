package io.github.duckasteroid.git.mvp.cmd;

import io.github.duckasteroid.git.mvp.Git;
import io.github.duckasteroid.git.mvp.GitTag;
import io.github.duckasteroid.git.mvp.version.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests our command line driven impl of the Git interface
 */
class GitCommandLineTest {
	@TempDir
	Path gitRepositoryPath;

	GitCommandLine git;
	private String commitId;

	@BeforeEach
	void setUp() throws IOException {
		ProcessResult initResult = GitCommandLine.withGit(List.of("init"), gitRepositoryPath, true);
		System.out.println(initResult);

		git = new GitCommandLine(gitRepositoryPath);

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

		// add all these to git
		git.add("test");
		// commit
		this.commitId = git.commit("Initial commit");
	}



	@Test
	void gitRootDirOutsideGit(@TempDir Path notGitDir) {
		Optional<Path> file = GitCommandLine.gitRootDir(notGitDir.toFile());
		assertNotNull(file);
		assertFalse(file.isPresent());
	}

	@Test
	void gitRootDirInsideGit() {
		Optional<Path> file = GitCommandLine.gitRootDir(gitRepositoryPath.toFile());
		assertNotNull(file);
		assertTrue(file.isPresent());

		file = GitCommandLine.gitRootDir(gitRepositoryPath.resolve("test/example").toFile());
		assertNotNull(file);
		assertTrue(file.isPresent());
	}

	@Test
	void gitCommitIDWithNoPath() {
		String longCommitId = git.gitCommitID(false, null);
		System.out.println(longCommitId);
		assertNotNull(longCommitId);
		assertFalse(longCommitId.isEmpty());
		assertTrue(longCommitId.contains(commitId));

		String shortCommitId = git.gitCommitID(true, null);
		System.out.println(shortCommitId);
		assertNotNull(shortCommitId);
		assertFalse(shortCommitId.isEmpty());
		assertEquals(shortCommitId, commitId);
	}

	@Test
	void gitCommitIDWithPath() throws IOException {
		// no change so same commit as before
		String shortCommitId = git.gitCommitID(true, "test/example");
		System.out.println(shortCommitId);
		assertNotNull(shortCommitId);
		assertEquals(shortCommitId, commitId);

		// now lets add a commit on that path
		Path modFile = gitRepositoryPath.resolve("test/example/file.txt");
		Files.writeString(modFile, "\nAddition", StandardOpenOption.APPEND);

		// commit
		final String newCommitID = git.commit( "Additional commit");

		shortCommitId = git.gitCommitID(true, "test/example");
		System.out.println(shortCommitId);
		assertNotNull(shortCommitId);
		assertNotEquals(shortCommitId, commitId);
		assertTrue(newCommitID.contains(shortCommitId));

		// now check the other (unaffected) path
		shortCommitId = git.gitCommitID(true, "test/other");
		assertNotNull(shortCommitId);
		assertNotEquals(shortCommitId, newCommitID);
		assertTrue(commitId.contains(shortCommitId));

		// lastly the root (also affected)
		shortCommitId = git.gitCommitID(true, null);
		assertNotNull(shortCommitId);
		assertTrue(newCommitID.contains(shortCommitId));

	}

	@Test
	void gitDirty() throws IOException {
		// to start things are not dirty
		assertFalse(git.gitDirty(null));
		// now let's add a modification on test/example/file.txt
		Path modFile = gitRepositoryPath.resolve("test/example/file.txt");
		Files.writeString(modFile, "\nAddition", StandardOpenOption.APPEND);

		// the repo in general is dirty
		assertTrue(git.gitDirty(null));

		// the modded path is dirty
		assertTrue(git.gitDirty("test/example"));

		// the other path is not dirty
		assertFalse(git.gitDirty("test/other"));
	}

	@Test
	void gitBranchName() {
		assertEquals("master", git.branchName());

		// new branch
		ProcessResult branchResult = GitCommandLine.withGit(List.of("checkout", "-b", "feature/unit-test"), gitRepositoryPath, true);

		assertEquals("feature/unit-test", git.branchName());
	}

	@Test
	void gitTagsLightweight() {
		List<GitTag> noTags = git.gitTags(null);
		assertNotNull(noTags);
		assertTrue(noTags.isEmpty());
		// now add a single light tag
		git.lightTag("v1.0.0");

		List<GitTag> singleTag = git.gitTags(null);
		assertNotNull(singleTag);
		assertEquals(1, singleTag.size());
		GitTag theTag = singleTag.get(0);
		assertNotNull(theTag);
		assertEquals("v1.0.0", theTag.value());
		Version version = theTag.version();
		assertNotNull(version);

	}

}