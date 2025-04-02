package io.github.duckasteroid.git.mvp.version;

import org.jetbrains.annotations.NotNull;

public record SimpleStringVersion(String value) implements Version {

	public static final char QUALIFIER_SEPARATOR = '-';

	@Override
	public boolean isIncrementable() {
		return false;
	}

	@Override
	public Version increment(int amount) {
		throw new UnsupportedOperationException("Simple String version does not support incrementation");
	}
	
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
