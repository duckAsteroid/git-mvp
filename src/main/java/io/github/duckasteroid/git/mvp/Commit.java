package io.github.duckasteroid.git.mvp;

import io.github.duckasteroid.git.mvp.version.Version;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents a git commit as a version source (not preferred)
 */
public record Commit(Supplier<String> explanation, String commitId) implements VersionSource {

    public Type type() {
        return Type.COMMIT;
    }

    @Override
    public String value() {
        return commitId;
    }

    public Version version() {
        return Version.parse(commitId);
    }

    @Override
    public @NotNull String toString() {
        return displayString();
    }
}
