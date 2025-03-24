package io.github.duckasteroid.git.mvp;

public interface VersionSource {
	String name();
	boolean hasVersion();
	String version();
	Version asVersion();
}
