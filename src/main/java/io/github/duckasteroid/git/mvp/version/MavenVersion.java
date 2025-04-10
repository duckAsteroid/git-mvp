package io.github.duckasteroid.git.mvp.version;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Maven style version number that follows the pattern:
 * <pre>MajorVersion[.MinorVersion[.IncrementalVersion]][-BuildNumber|Qualifier]</pre>
 * @param major The major version component
 * @param minor The minor version component
 * @param incremental The incremental version component
 * @param qualifier The build number or qualifier version component
 */
public record MavenVersion(
				Integer major,
				Integer minor,
				Integer incremental,
				String qualifier)
				implements Version {


	private static final Pattern VERSION_PATTERN = Pattern.compile(
					"^" +
									"(?<major>\\d+)" +
									"(?:\\.(?<minor>\\d+))?" +
									"(?:\\.(?<incremental>\\d+))?" +
									"(?:-(?<qualifier>[a-zA-Z0-9.-]+))?" +
									"$"
	);

	/**
	 * Parse a string of the established pattern into an instance
	 * @param version the string to parse
	 * @return an instance or null if not parsed
	 */
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

	/**
	 * Strategies for updating versions when change is detected.
	 * Takes the current version, and an "amount of change" (as an int) and returns
	 * a new version instance
	 */
	public enum IncrementStrategy implements BiFunction<MavenVersion, Integer, Version> {
		/**
		 * Only update the incremental component with the amount.
		 * If other components are not specified, they become 0
		 */
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
		/**
		 * Only update the minor component with the amount.
		 */
		ONLY_MINOR {
			public Version apply(MavenVersion version, Integer amount) {
				int minor = amount;
				if (version.minor != null) {
					minor = version.minor + amount;
				}
				return new MavenVersion(version.major, minor, version.incremental, null);
			}
		},
		/**
		 * Only update the major component with the amount.
		 */
		ONLY_MAJOR {
			public Version apply(MavenVersion version, Integer amount) {
				return new MavenVersion(version.major + amount, version.minor, version.incremental, null);
			}
		},
		/**
		 * Update the first component defined with the amount. Starting with incremental, then minor,
		 * then major
		 */
		FIRST_DEFINED {
			public Version apply(MavenVersion version, Integer amount) {
				if (version.incremental != null) {
					return IncrementStrategy.ONLY_INCREMENTAL.apply(version, amount);
				} else if (version.minor != null) {
					return IncrementStrategy.ONLY_MINOR.apply(version, amount);
				} else {
					return IncrementStrategy.ONLY_MAJOR.apply(version, amount);
				}
			}
		}
	}

	@Override
	public Version increment(int amount) {
		return increment(IncrementStrategy.ONLY_INCREMENTAL, amount);
	}

	/**
	 * A specialisation to use alternative version incrementing strategies.
	 * @param strategy the increment strategy to use
	 * @param amount the amount to increment
	 * @return the new incremented version
	 */
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
