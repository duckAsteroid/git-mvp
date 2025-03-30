package io.github.duckasteroid.git.mvp;

import org.jetbrains.annotations.NotNull;

/**
 * A git change
 */
public record Change(String status, String path) {
    /**
     * Parse the change from Git command line
     * @param s the git status
     * @return a change instance
     */
    public static Change from(String s) {
        return new Change(s.substring(0, 1), s.substring(1).trim());
    }

    @Override
    public @NotNull String toString() {
        return status + ": " + path;
    }
}
