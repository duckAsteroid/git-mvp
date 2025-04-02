package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.version.Version;

import java.util.function.Function;

/**
 * Represents a change to the retrieved git version based on the status of the repo
 */
public class VersionAmendment {

	private final String description;
	private final Function<Version, Version> amender;

	public VersionAmendment(String description, Function<Version,Version> amender) {
		this.description = description;
		this.amender = amender;
	}

	public String description() {
		return description;
	}

	public Version amended(Version version) {
		return amender.apply(version);
	}
}
