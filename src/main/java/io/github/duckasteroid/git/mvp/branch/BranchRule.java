package io.github.duckasteroid.git.mvp.branch;

import java.util.function.Predicate;

/**
 * Used to describe a rule that matches branch names
 * @param description A description
 * @param rule A predicate (rule) that matches branch names
 */
public record BranchRule(String description, Predicate<String> rule) {
}
