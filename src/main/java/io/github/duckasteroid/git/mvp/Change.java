package io.github.duckasteroid.git.mvp;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * A git status change indication
 * @param status git status string reported against the path
 * @param path the path to the file concerned (from repo root)
 */
public record Change(String status, Path path) {
    /**
     * Parse the change from Git command line
     * @param s the git status
     * @return a change instance
     */
    public static Change from(String s) {
        int i = s.indexOf(' ');
        String status, path;
        if (i > 0) {
            status = s.substring(0, i);
            path = s.substring(i + 1);
        }
        else {
            status = "";
            path = s;
        }
        return new Change(status, Path.of(path));
    }
}
