package io.github.duckasteroid.git.mvp.version.source;

import io.github.duckasteroid.git.mvp.version.Version;

import java.util.function.Supplier;

/**
 * A source of version information for the plugin
 */
public interface VersionSource {
	/**
	 * An enumeration of the types of source
	 */
	enum Type {
		/**
		 * A git tag
		 */
		TAG,
		/**
		 * A git commit
		 */
		COMMIT
	}

	/**
	 * What type of source is this
	 * @return a type enum
	 */
	Type type();

	/**
	 * The raw string value of the version data source
	 * @return the raw version value from the source
	 */
	String value();

	/**
	 * The explanation of the source of this version information
	 * @return a function that can provide a description of where this value comes from
	 */
	Supplier<String> explanation();
	/**
	 * The version instance itself
	 * @return the version instance
	 */
	Version version();

	/**
	 * A display string for this version source
	 * @return the type, value and version instance
	 */
	default String displayString() {
		return type() + " '" + value() + "' interpreted as version=" + version();
	}
}
