package io.github.duckasteroid.git.mvp;

/**
 * A git change
 */
public record Change(String status, String path) {

    public static Change from(String s) {
        return new Change(s.substring(0, 1), s.substring(1).trim());
    }

    public String toString() {
        return status + ": " + path;
    }
}
