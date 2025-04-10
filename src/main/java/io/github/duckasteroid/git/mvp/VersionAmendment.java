package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.version.Version;

import java.util.function.Function;

/**
 * Represents a change to the retrieved git version based on the status of the repo.
 * i.e. add a dirty qualifier
 */
public record VersionAmendment (String description, Function<Version,Version> amender) {
	/**
	 * Apply the {@link #amender()} to a version and return a new version
	 * @param version the version to amend
	 * @return a new amended version
	 */
	public Version amended(Version version) {
		return amender.apply(version);
	}
}
