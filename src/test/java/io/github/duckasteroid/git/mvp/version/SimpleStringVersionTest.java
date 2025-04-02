package io.github.duckasteroid.git.mvp.version;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleStringVersionTest {
	private final Version versionWithQualifier = new SimpleStringVersion("foo-bar");
	private final Version versionWithoutQualifier = new SimpleStringVersion("foobar");

	@Test
	void basicStringVersionBehaviour() {
		assertFalse(versionWithQualifier.isIncrementable());
		assertFalse(versionWithoutQualifier.isIncrementable());

		assertEquals("bar", versionWithQualifier.qualifier());
		assertEquals("", versionWithoutQualifier.qualifier());

		assertEquals("foo-test", versionWithQualifier.withQualifier("test").toString());
		assertEquals("foobar-test", versionWithoutQualifier.withQualifier("test").toString());

	}
}