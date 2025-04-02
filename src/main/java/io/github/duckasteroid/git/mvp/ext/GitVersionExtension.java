package io.github.duckasteroid.git.mvp.ext;

import io.github.duckasteroid.git.mvp.GitVersionProjectWrapper;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.List;

public class GitVersionExtension {
	public static final String NAME = "versioning";
	// Properties to hold the file patterns
	// Using ListProperty allows duplicates if needed, SetProperty ensures uniqueness
	private final ListProperty<String> includes;
	private final ListProperty<String> excludes;

	private final Property<String> dirtyQualifier;

	@Inject // Inject ObjectFactory to create property instances
	public GitVersionExtension(Project project, ObjectFactory objects) {
		// Use ObjectFactory to create instances of Gradle's property types
		// convention is to include all
		this.includes = objects.listProperty(String.class).convention(List.of("**/*"));
		this.excludes = objects.listProperty(String.class).empty();

		this.dirtyQualifier = objects.property(String.class).convention("dirty");
	}
	// Getter methods for the properties (required by Gradle)
	public ListProperty<String> getIncludes() {
		return includes;
	}

	public ListProperty<String> getExcludes() {
		return excludes;
	}

	public Property<String> getDirtyQualifier() {
		return dirtyQualifier;
	}

	// --- DSL Methods for configuration convenience ---
	// These mimic the PatternFilterable methods but operate on the properties

	public void include(String... patterns) {
		this.includes.addAll(patterns);
	}

	public void include(Iterable<String> patterns) {
		this.includes.addAll(patterns);
	}

	public void exclude(String... patterns) {
		this.excludes.addAll(patterns);
	}
	public void exclude(Iterable<String> patterns) {
		this.excludes.addAll(patterns);
	}
}
