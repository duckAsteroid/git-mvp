package io.github.duckasteroid.git.mvp.version;

/**
 * Represents a standard software version.
 * A list of segments (which may be numbers or strings)
 * These are parsed by splitting a version string at '.' or '-' characters
 */
public interface Version {
	/**
	 * Is this version incrementable.
	 * That is, does it contain some meaningful numeric segment that
	 * can be incremented to represent "updates"?
	 * @return true if this version can be incremented
	 */
  boolean isIncrementable();

	/**
	 * Calculate a new incremented version.
	 * @param amount the amount of change to incorporate in the increment
	 * @return a new incremented version
	 * @throws UnsupportedOperationException If this type of version does not support incrementig.
	 * @see #isIncrementable()
	 */
	Version increment(int amount);

	/**
	 * Create a new version with the given qualifier. If this version already has a qualifier it will
	 * be replaced.
	 * @param newQualifier the new qualifier segment to append to the version
	 * @return A new qualified version
	 */
	Version withQualifier(String newQualifier);

	/**
	 * The current qualifier segment in this version (if any)
	 * @return the qualifier or an empty string
	 */
	String qualifier();

	/**
	 * Parse a version string returning a concrete instance of this class.
	 * @param versionString the string to parse
	 * @return an instance wrapping this version string
	 */
	static Version parse(String versionString) {
		Version v = MavenVersion.parse(versionString);
		if (v == null) {
			v = new SimpleStringVersion(versionString);
		}
		return v;
	}

}
