package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.version.Version;

import java.util.function.Supplier;

/**
 * A source of version information for the plugin
 */
public interface VersionSource {
	enum Type {TAG, COMMIT}

	Type type();

	/**
	 * The raw string value of the version data source
	 */
	String value();

	/**
	 * The provider of an explanation of the source of this version information
	 * @return a function that can provide a description of where this value comes from
	 */
	Supplier<String> explanation();
	/**
	 * The version instance itself
	 * @return the version instance
	 */
	Version version();

	default String displayString() {
		return type() + " '" + value() + "' interpreted as version=" + version();
	}
}
