package io.github.pingmyheart.notioncontrollerrecorder.configuration;

import junit.framework.TestCase;

public class ContextTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetNotionWebClient() {
        assertNotNull(Context.getNotionWebClient());
    }

    public void testGetNotionService() {
        assertNotNull(Context.getNotionService());
    }

    public void testGetWebClientImpl() {
        assertNotNull(Context.getWebClientImpl());
    }

    public void testGetObjectMapper() {
        assertNotNull(Context.getObjectMapper());
    }
}