package io.github.duckasteroid.git.mvp.version.source;

import io.github.duckasteroid.git.mvp.version.Version;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents data about a git tag
 */
public class GitTag implements VersionSource {
	private static final Pattern PATTERN = Pattern.compile(("v(\\d+\\.\\d+(?:\\.\\d+)?(?:-[A-Z0-9]+)?)"));
	private static final char SEPARATOR = '\u0001';
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss~Z");

	private final Supplier<String> explanation;
	private final String tag;
	private final OffsetDateTime commitDate;
	private final String subject;
	private final String shortCommit;
	private final String longCommit;

	public GitTag(Supplier<String> explanation, String tag, OffsetDateTime commitDate, String subject, String shortCommit, String longCommit) {
		this.explanation = explanation;
		this.tag = tag;
		this.commitDate = commitDate;
		this.subject = subject;
		this.shortCommit = shortCommit;
		this.longCommit = longCommit;
	}

	@Override
	public String value() {
		return tag;
	}

	@Override
	public Supplier<String> explanation() {
		return explanation;
	}

	public Type type() {
		return Type.TAG;
	}

	public String versionString() {
		Matcher matcher = PATTERN.matcher(tag);
		if (!matcher.find()) return tag;
		return matcher.group(1);// Extract the version number
	}

	public Version version() {
		return Version.parse(versionString());
	}

	public static GitTag parse(Supplier<String> explanation, String formatted) {
		String[] split = formatted.split(String.valueOf(SEPARATOR));
		if (split.length < 5) throw new IllegalArgumentException("Invalid format: " + formatted);
		OffsetDateTime offsetDateTime = null;
		if (!split[1].isBlank()) {
			offsetDateTime = OffsetDateTime.parse(split[1], DATE_FORMAT);
		}
		return new GitTag(explanation, split[0], offsetDateTime , split[2], split[3], split[4]);
	}

	public static String formatString() {
		return "%(refname:short)" + SEPARATOR + "%(committerdate:format:%Y-%m-%d@%H:%M:%S~%z)" + SEPARATOR + "%(subject)" + SEPARATOR + "%(objectname:short)" + SEPARATOR + "%(objectname)";
	}

	@Override
	public String toString() {
		return displayString();
	}
}
