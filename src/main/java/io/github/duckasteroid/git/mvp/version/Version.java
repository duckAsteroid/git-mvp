package io.github.duckasteroid.git.mvp.version;

/**
 * Represents a standard software version.
 * A list of segments (which may be numbers or strings)
 * These are parsed by splitting a version string at '.' or '-' characters
 */
public interface Version {
  boolean isIncrementable();
	Version increment(int amount);
	Version withQualifier(String newQualifier);
	String qualifier();

	static Version parse(String versionString) {
		Version v = MavenVersion.parse(versionString);
		if (v == null) {
			v = new SimpleStringVersion(versionString);
		}
		return v;
	}

}
