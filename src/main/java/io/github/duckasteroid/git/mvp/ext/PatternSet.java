package io.github.duckasteroid.git.mvp.ext;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;

import javax.inject.Inject;
import java.util.List;

public class PatternSet {
	// Using ListProperty allows duplicates if needed, SetProperty ensures uniqueness
	private final ListProperty<String> includes;
	private final ListProperty<String> excludes;

	@Inject
	public PatternSet(ObjectFactory objects, List<String> includesConvention, List<String> excludesConvention) {
		this.includes = objects.listProperty(String.class);
		if (includesConvention != null) {
			includes.convention(includesConvention);
		}
		this.excludes = objects.listProperty(String.class);
		if (excludesConvention != null) {
			excludes.convention(excludesConvention);
		}
	}

	/**
	 * A set of Ant style path include specifications
	 * @return the list of includes
	 */
	public ListProperty<String> getIncludes() {
		return includes;
	}

	/**
	 * A set of Ant style path exclude specifications
	 * @return the list of excludes
	 */
	public ListProperty<String> getExcludes() {
		return excludes;
	}

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
