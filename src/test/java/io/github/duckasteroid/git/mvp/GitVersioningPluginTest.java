package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.cmd.GitCommandLine;
import io.github.duckasteroid.git.mvp.cmd.ProcessResult;
import io.github.duckasteroid.git.mvp.ext.GitVersionExtension;
import io.github.duckasteroid.git.mvp.tasks.ExplainVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.impldep.com.google.common.base.Predicates;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class GitVersioningPluginTest {
	@TempDir
	Path gitRepo;

	GitCommandLine git;

	Project parent;

	private String initialCommitID;

	@BeforeEach
	void setUp() throws IOException {
		ProcessResult initResult = GitCommandLine.withGit(List.of("init"), gitRepo, true);
		System.out.println(initResult);

		git = new GitCommandLine(gitRepo);

		ProjectBuilder builder = ProjectBuilder.builder();
		createTestFile(gitRepo, "root.txt", "Test file for the root project");
		this.parent = builder.withName("test-parent").withProjectDir(gitRepo.toFile()).build();

		Path child1Path = gitRepo.resolve("child1");
		Files.createDirectories(child1Path);
		createTestFile(child1Path, "test.txt", "Test file for child1");
		Project child1 = builder.withName("child1").withProjectDir(child1Path.toFile()).withParent(parent).build();

		Path child2Path = gitRepo.resolve("some/deep/path/child2");
		Files.createDirectories(child2Path);
		createTestFile(child2Path, "test.txt", "Test file for child2");
		Project child2 = builder.withName("child2").withProjectDir(child2Path.toFile()).withParent(parent).build();

		git.add(".");
		this.initialCommitID = git.commit("Initial commit");
	}

	private static void createTestFile(Path path, String filename, String content) throws IOException {
		Files.writeString(path.resolve(filename), content, StandardOpenOption.CREATE);
	}

	private void applyPluginToAllProjects() {
		applyPluginToProjects(Predicates.alwaysTrue());
	}

	private void applyPluginToProjects(Predicate<Project> matching) {
		parent.getAllprojects().forEach(project -> {
			if (matching.test(project)) {
				project.getPluginManager().apply(GitVersioningPlugin.class);
			}
		});
	}

	@Test
	void verifyPluginAndTasks() {
		applyPluginToAllProjects();

		Set<Project> allProjects = parent.getAllprojects();
		for (Project project : allProjects) {
			assertTrue(project.getPluginManager().hasPlugin(GitVersioningPlugin.ID));
			Task explainVersion = project.getTasks().getByName(ExplainVersion.NAME); // will throw ex if not found...
			assertNotNull(explainVersion);
			assertTrue(explainVersion instanceof ExplainVersion);
		}
	}

	@Test
	void verifyCommitIDVersions() {
		applyPluginToAllProjects();

		// since we have not defined any tags - the default is the commit ID
		Set<Project> allProjects = parent.getAllprojects();
		for (Project project : allProjects) {
			String version = parent.getVersion().toString();
			assertNotNull(version);
			assertTrue(initialCommitID.contains(version));
		}
	}

	@Test
	void verifySubprojectLightTags() {
		// first lets define some tags matching the project structures
		git.lightTag("v1.0.0-ROOT");
		git.lightTag("child1/v2.0.0-CHILD1");
		git.lightTag("child2/v3.0.0-CHILD2");

		applyPluginToAllProjects();
		assertEquals("1.0.0-ROOT", parent.getVersion().toString());
		assertEquals("2.0.0-CHILD1", parent.getChildProjects().get("child1").getVersion().toString());
		assertEquals("3.0.0-CHILD2", parent.getChildProjects().get("child2").getVersion().toString());
	}

	@Test
	void verifySubprojectHeavyTags() {
		// first lets define some tags matching the project structures
		git.annotatedTag("v1.0.0-ROOT", "My root project tag");
		git.annotatedTag("child1/v2.0.0-CHILD1", "Child 1 needs a tag");
		git.annotatedTag("child2/v3.0.0-CHILD2", "How could I forget child 2");

		applyPluginToAllProjects();
		assertEquals("1.0.0-ROOT", parent.getVersion().toString());
		assertEquals("2.0.0-CHILD1", parent.getChildProjects().get("child1").getVersion().toString());
		assertEquals("3.0.0-CHILD2", parent.getChildProjects().get("child2").getVersion().toString());
	}

	@Test
	void verifyDirtyQualifierOnMaster() throws IOException {
		// make child2 files dirty
		Path child2dir = gitRepo.resolve("some/deep/path/child2");
		Path dirtyFile = child2dir.resolve("dirty.txt");
		Files.writeString(dirtyFile, "This file makes the repo dirty", StandardOpenOption.CREATE);

		applyPluginToAllProjects();
		parent.getAllprojects().stream()
						.map(p -> p.getExtensions().findByType(GitVersionExtension.class))
						.filter(Objects::nonNull)
						.forEach(ext -> {
							var inc = ext.getAutoIncrementBranches();
							inc.include("master");
							inc.getExcludes().set(Collections.emptyList());
							ext.getDirtyQualifier().set("TEST");
							ext.update();
						});


		String child2version = parent.getChildProjects().get("child2").getVersion().toString();
		System.out.println(child2version);
		assertTrue(child2version.endsWith("-TEST"));
	}

	@Test
	void verifyDirtyQualifierOnAutoIncrementedBranch() throws IOException {
		// switch branch
		git.newBranch("test");
		// make child2 files dirty
		Path child2dir = gitRepo.resolve("some/deep/path/child2");
		Path dirtyFile = child2dir.resolve("dirty.txt");
		Files.writeString(dirtyFile, "This file makes the repo dirty", StandardOpenOption.CREATE);

		applyPluginToAllProjects();

		String child2version = parent.getChildProjects().get("child2").getVersion().toString();
		assertTrue(child2version.endsWith("-dirty"));
	}

}