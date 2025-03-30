package io.github.duckasteroid.git.mvp;

/**
 * A source of version information for the plugin
 */
public interface VersionSource {
	/**
	 * The name of the source
	 * @return the name
	 */
	String name();

	/**
	 * Does this source contain version information
	 * @return true if the source has version
	 */
	boolean hasVersion();

	/**
	 * The version information (if any)
	 * @return version information
	 */
	String version();

	/**
	 * Parse into a version instance
	 * @return the version instance
	 */
	Version asVersion();
}
