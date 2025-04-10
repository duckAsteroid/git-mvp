package io.github.duckasteroid.git.mvp.ext;

import io.github.duckasteroid.git.mvp.GitVersionProjectWrapper;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * A custom project extension for holding configuration data for the
 * {@link io.github.duckasteroid.git.mvp.GitVersioningPlugin}
 */
public class GitVersionExtension {
	/**
	 * The name of the extension in the project
	 */
	public static final String NAME = "versioning";
	// Properties to hold the file patterns
	private final PatternSet autoIncrementBranches;

	private final Property<String> dirtyQualifier;

	private final GitVersionProjectWrapper projectWrapper;

	/**
	 * Construct with a project and a Gradle object factory
	 * @param project the project
	 * @param objects a Gradle object factory
	 */
	@Inject // Inject ObjectFactory to create property instances
	public GitVersionExtension(Project project, ObjectFactory objects) {
		// Use ObjectFactory to create instances of Gradle's property types
		// convention is to include all
		List<String> includeConvention = Collections.emptyList();
		List<String> excludeConvention = List.of("main", "master");
		// convention is develop and any feature branch is auto incremented
		this.autoIncrementBranches = objects.newInstance(PatternSet.class, objects, includeConvention, excludeConvention);

		this.dirtyQualifier = objects.property(String.class).convention("dirty");

		this.projectWrapper = new GitVersionProjectWrapper(project);
	}


	// Getter methods for the properties (required by Gradle)

	/**
	 * A set of regular expressions for branch names. If matched these branches will
	 * have automatic version number increments applied.
	 * @return the pattern set for this
	 */
	@Nested
	public PatternSet getAutoIncrementBranches() {
		return autoIncrementBranches;
	}

	/**
	 * Used by Gradle
	 * @param action the child patterns
	 */
	public void autoIncrementBranches(Action<? super PatternSet> action) {
		action.execute(autoIncrementBranches);
	}

	/**
	 * The string to append to the version as a qualifier - if the repository is dirty
	 * @return the dirty qualifier
	 */
	public Property<String> getDirtyQualifier() {
		return dirtyQualifier;
	}

	/**
	 * Primarily used by unit tests to update the version on the project
	 */
	public void update() {
		projectWrapper.update();
	}

}
