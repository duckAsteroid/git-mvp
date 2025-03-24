package io.github.duckasteroid.git.mvp;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents data about a git tag
 */
public class GitTag implements VersionSource {
    private static final Pattern PATTERN = Pattern.compile("v(\\d+\\.\\d+\\.\\d+)$");
    private static final char SEPARATOR = '\u0001';
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss~Z");

    private final String tag;
    private final OffsetDateTime commitDate;
    private final String subject;
    private final String shortCommit;
    private final String longCommit;

	public GitTag(String tag, OffsetDateTime commitDate, String subject, String shortCommit, String longCommit) {
		this.tag = tag;
		this.commitDate = commitDate;
		this.subject = subject;
		this.shortCommit = shortCommit;
		this.longCommit = longCommit;
	}


	public boolean hasVersion() {
        Matcher matcher = PATTERN.matcher(tag);
        return matcher.find();
    }

    public String name() {
        return tag;
    }

    public String version() {
        Matcher matcher = PATTERN.matcher(tag);
        matcher.find();
        return matcher.group(1);// Extract the version number
    }

    public Version asVersion() {
        return new Version(version());
    }

    public static GitTag parse(String formatted) {
        String[] split = formatted.split(String.valueOf(SEPARATOR));
        return new GitTag(split[0], OffsetDateTime.parse(split[1], DATE_FORMAT), split[2], split[3], split[4]);
    }

    public static String formatString() {
        return "%(refname:short)" + SEPARATOR + "%(committerdate:format:%Y-%m-%d@%H:%M:%S~%z)" +SEPARATOR + "%(subject)" + SEPARATOR + "%(objectname:short)" + SEPARATOR + "%(objectname)";
    }

    public String toString() {
        return "Tag [" + tag + "]:" + version();
    }
}
