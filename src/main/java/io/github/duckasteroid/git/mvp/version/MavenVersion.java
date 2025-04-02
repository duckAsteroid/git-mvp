package io.github.duckasteroid.git.mvp.version;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>MajorVersion[.MinorVersion[.IncrementalVersion]][-BuildNumber|Qualifier]</pre>
 */
public record MavenVersion(
	Integer major,
	Integer minor,
	Integer incremental,
	String qualifier)
				implements Version {

public MavenVersion(Integer major, Integer minor, Integer incremental, String qualifier) {
		this.major = major;
		this.minor = minor;
		this.incremental = incremental;
		this.qualifier = qualifier;
	}

	private static final Pattern VERSION_PATTERN = Pattern.compile(
					"^" +
									"(?<major>\\d+)" +
									"(?:\\.(?<minor>\\d+))?" +
									"(?:\\.(?<incremental>\\d+))?" +
									"(?:-(?<qualifier>[a-zA-Z0-9.-]+))?" +
									"$"
	);

	public static MavenVersion parse(String version) {
		Matcher matcher = VERSION_PATTERN.matcher(version);

		if (matcher.matches()) {
			Integer major = null;
			Integer minor = null;
			Integer incremental = null;
			String qualifier = null;

			String majorStr = matcher.group("major");
			if (majorStr != null) {
				major = Integer.parseInt(majorStr);
			}

			String minorStr = matcher.group("minor");
			if (minorStr != null) {
				minor = Integer.parseInt(minorStr);
			}

			String incrementalStr = matcher.group("incremental");
			if (incrementalStr != null) {
				incremental = Integer.parseInt(incrementalStr);
			}

			qualifier = matcher.group("qualifier");

			return new MavenVersion(major, minor, incremental, qualifier);
		}
		return null;
	}

	@Override
	public boolean isIncrementable() {
		return true;
	}

	public enum IncrementStrategy implements BiFunction<MavenVersion, Integer, Version> {
		ONLY_INCREMENTAL {
			public Version apply(MavenVersion version, Integer amount) {
				int incremented = amount;
				int newMinor = 0;
				if (version.minor != null) {
					newMinor = version.minor;
				}
				if (version.incremental != null) {
					incremented = version.incremental + amount;
				}
				return new MavenVersion(version.major, newMinor, incremented, version.qualifier);
			}
		},
		ONLY_MINOR {
			public Version apply(MavenVersion version, Integer amount) {
				int minor = amount;
				if (version.minor != null) {
					minor = version.minor + amount;
				}
				return new MavenVersion(version.major, minor, version.incremental, null);
			}
		},
		ONLY_MAJOR {
			public Version apply(MavenVersion version, Integer amount) {
				return new MavenVersion(version.major + amount, version.minor, version.incremental, null);
			}
		},
		FIRST_DEFINED {
			public Version apply(MavenVersion version, Integer amount) {
				if (version.incremental != null) {
					return IncrementStrategy.ONLY_INCREMENTAL.apply(version, amount);
				}
				else if (version.minor != null) {
					return IncrementStrategy.ONLY_MINOR.apply(version, amount);
				}
				else {
					return IncrementStrategy.ONLY_MAJOR.apply(version, amount);
				}
			}
		}
	}

	public Version increment(int amount) {
		return increment(IncrementStrategy.ONLY_INCREMENTAL, amount);
	}

	public Version increment(IncrementStrategy strategy, int amount) {
		return strategy.apply(this, amount);
	}

	@Override
	public Version withQualifier(String newQualifier) {
		return new MavenVersion(major, minor, incremental, newQualifier);
	}

	@Override
	public @NotNull String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(major);
		if (minor != null) {
			sb.append(".").append(minor);
			if (incremental != null) {
				sb.append(".").append(incremental);
			}
		}
		if (qualifier != null) {
			sb.append("-").append(qualifier);
		}
		return sb.toString();
	}
}
