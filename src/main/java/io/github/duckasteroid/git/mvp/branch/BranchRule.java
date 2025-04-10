package io.github.duckasteroid.git.mvp.branch;

import java.util.function.Predicate;

public record BranchRule(String description, Predicate<String> rule) {
}
