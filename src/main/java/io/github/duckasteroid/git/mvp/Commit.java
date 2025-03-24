package io.github.duckasteroid.git.mvp;

/**
 * Represents a git commit as a version source (not preferred)
 */
public record Commit(String commitId) implements VersionSource {
    @Override
    public boolean hasVersion() {
        return true;
    }

    public String name() {
        return commitId;
    }

    public String version() {
        return commitId;
    }

    public String toString() {
        return "Commit: " + commitId;
    }

    public Version asVersion() {
        return null;
    }

}
