package io.github.duckasteroid.git.mvp.version;

import org.jetbrains.annotations.NotNull;

/**
 * A version that is represented by a dumb string.
 * It can have a qualifier segment.
 * The qualifier is determined to be everything after the last occurrence of '-' in the string
 * (if any).
 * This version CANNOT be incremented, {@link #increment(int)} always throws
 * {@link UnsupportedOperationException}
 * @param value a string to use for the value
 */
public record SimpleStringVersion(String value) implements Version {
	/**
	 * Separator between core string and a qualifier segment
	 * @see #qualifier()
	 */
	public static final char QUALIFIER_SEPARATOR = '-';

	@Override
	public boolean isIncrementable() {
		return false;
	}

	@Override
	public Version increment(int amount) {
		throw new UnsupportedOperationException("Simple String version does not support incrementation");
	}

	/**
	 * Everything after the last '-' or an empty string
	 * @return the qualifier segment of this version (if any)
	 */
	@Override
	public String qualifier() {
		int i = value.lastIndexOf(QUALIFIER_SEPARATOR);
		if (i < 0) {
			return "";
		}
		return value.substring(i + 1);
	}

	@Override
	public Version withQualifier(String newQualifier) {
		int i = value.lastIndexOf(QUALIFIER_SEPARATOR);
		if (i < 0) {
			return new SimpleStringVersion(value + QUALIFIER_SEPARATOR + newQualifier);
		}
		else {
			return new SimpleStringVersion(value.substring(0, i) + QUALIFIER_SEPARATOR + newQualifier);
		}
	}

	@Override
	public @NotNull String toString() {
		return value;
	}
}
