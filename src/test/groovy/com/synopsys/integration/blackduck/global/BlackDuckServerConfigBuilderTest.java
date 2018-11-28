package com.synopsys.integration.blackduck.global;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.rest.credentials.Credentials;

public class BlackDuckServerConfigBuilderTest {
    @Test
    public void testValidateHubURLEmpty() {
        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testValidateHubURLMalformed() {
        final String blackDuckUrl = "TestString";

        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testValidateHubURLMalformed2() {
        final String blackDuckUrl = "http:TestString";

        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testValidConfig() {
        final String blackDuckUrl = "http://this.might.exist/somewhere";

        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setApiToken("a valid, non-empty api token");
        assertTrue(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testValidConfigWithUsernameAndPassword() {
        final String blackDuckUrl = "http://this.might.exist/somewhere";

        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setCredentials(new Credentials("a valid, non-blank username", "a valid, non-blank password"));
        assertTrue(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testInvalidConfigWithBlankUsernameAndPassword() {
        final String blackDuckUrl = "http://this.might.exist/somewhere";

        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setCredentials(new Credentials("", null));
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testTimeout() {
        final String blackDuckUrl = "http://this.might.exist/somewhere";

        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setTimeout(0);
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

}
