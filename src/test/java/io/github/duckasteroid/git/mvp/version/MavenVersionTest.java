package io.github.duckasteroid.git.mvp.version;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MavenVersionTest {
	Map<String, MavenVersion> versions = new HashMap<>();
	Map<MavenVersion, MavenVersion> incremented = new HashMap<>();

	public MavenVersionTest() {
		versions.put("1", new MavenVersion(1, null, null, null));
		versions.put("1.0", new MavenVersion(1, 0, null, null));
		versions.put("1.0.1", new MavenVersion(1, 0, 1, null));
		versions.put("1.0.1-SNAPSHOT", new MavenVersion(1, 0, 1, "SNAPSHOT"));
		versions.put("1-RC2", new MavenVersion(1, null, null, "RC2"));
		versions.put("1.10-WIBBLE", new MavenVersion(1, 10, null, "WIBBLE"));
		versions.put("1.2.3.4", null);
		versions.put("1.x.3-NOTVALID", null);


		incremented.put(
						new MavenVersion(1, null, null, null),
						new MavenVersion(1, 0, 2, null));
		incremented.put(
						new MavenVersion(1, 0, null, null),
						new MavenVersion(1, 0, 2, null));
		incremented.put(
						new MavenVersion(1, 0, 1, null),
						new MavenVersion(1, 0, 3, null));
		incremented.put(
						new MavenVersion(1, 0, 1, "SNAPSHOT"),
						new MavenVersion(1, 0, 3, "SNAPSHOT"));
		incremented.put(
						new MavenVersion(1, null, null, "RC2"),
						new MavenVersion(1, 0, 2, "RC2"));
		incremented.put(
						new MavenVersion(1, 10, null, "WIBBLE"),
						new MavenVersion(1, 10, 2, "WIBBLE"));
	}

	@Test
	void parse() {
		for (Map.Entry<String, MavenVersion> entry : versions.entrySet()) {
			MavenVersion expected = entry.getValue();
			String s = entry.getKey();
			MavenVersion actual = MavenVersion.parse(s);
			assertEquals(expected, actual);
		}
	}

	@Test
	void increment() {
		for (Map.Entry<MavenVersion, MavenVersion> entry : incremented.entrySet()) {
			MavenVersion expected = entry.getValue();
			MavenVersion input = entry.getKey();
			MavenVersion actual = (MavenVersion) input.increment(2);
			assertEquals(expected, actual);
		}
	}

	@Test
	void withQualifier() {
		MavenVersion majorOnly = new MavenVersion(1, null, null, null);
		Version actual = majorOnly.withQualifier("stuff");
		assertEquals("stuff", actual.qualifier());
		assertEquals("1-stuff", actual.toString());

		MavenVersion full = new MavenVersion(1, 2, 3, "SNAPSHOT");
		actual = full.withQualifier("stuff");
		assertEquals("stuff", actual.qualifier());
		assertEquals("1.2.3-stuff", actual.toString());

		actual = full.withQualifier(null);
		assertEquals("1.2.3", actual.toString());
	}
}