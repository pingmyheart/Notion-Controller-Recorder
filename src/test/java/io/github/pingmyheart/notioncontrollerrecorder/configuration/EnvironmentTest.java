package io.github.pingmyheart.notioncontrollerrecorder.configuration;

import junit.framework.TestCase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class EnvironmentTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetEnv() {
        assertDoesNotThrow(() -> Environment.setEnv("NOTION_TOKEN", "token"));
    }

    public void testGet() {
        Environment.setEnv("NOTION_TOKEN", "token");
        assertNotNull(Environment.get("NOTION_TOKEN"));
        assertNull(Environment.get("notion_token"));
    }
}