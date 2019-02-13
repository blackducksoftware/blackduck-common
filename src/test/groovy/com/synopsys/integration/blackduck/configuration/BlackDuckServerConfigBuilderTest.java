package com.synopsys.integration.blackduck.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;

@ExtendWith(TimingExtension.class)
public class BlackDuckServerConfigBuilderTest {
    @Test
    void testSettingFromPropertiesMap() {
        Map<String, String> properties = new HashMap<>();
        properties.put("BLACKDUCK_URL", "test url");
        properties.put("BLACKDUCK_USERNAME", "user");
        properties.put("BLACKDUCK_PASSWORD", "password");

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        assertNull(blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.URL));
        assertNull(blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.USERNAME));
        assertNull(blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.PASSWORD));

        blackDuckServerConfigBuilder.setFromProperties(properties);

        assertEquals("test url", blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.URL));
        assertEquals("user", blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.USERNAME));
        assertEquals("password", blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.PASSWORD));
    }

    @Test
    void testSettingFromPropertiesMapWithMixed() {
        Map<String, String> properties = new HashMap<>();
        properties.put("BLACKDUCK_URL", "test url");
        properties.put("blackduck.username", "user");
        properties.put("BLACKDUCK_PASSWORD", "password");

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        assertNull(blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.URL));
        assertNull(blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.USERNAME));
        assertNull(blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.PASSWORD));

        blackDuckServerConfigBuilder.setFromProperties(properties);

        assertEquals("test url", blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.URL));
        assertEquals("user", blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.USERNAME));
        assertEquals("password", blackDuckServerConfigBuilder.get(BlackDuckServerConfigBuilder.Property.PASSWORD));
    }

    @Test
    public void testValidateBlackDuckURLEmpty() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testNullUrlInvalid() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(null);
        blackDuckServerConfigBuilder.setUsername("fakeUser");
        blackDuckServerConfigBuilder.setPassword("fakePassword");
        assertFalse(blackDuckServerConfigBuilder.isValid());
        try {
            blackDuckServerConfigBuilder.build();
            fail("Should have thrown an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testValidateBlackDuckURLMalformed() {
        String blackDuckUrl = "TestString";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testValidateBlackDuckURLMalformed2() {
        String blackDuckUrl = "http:TestString";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testValidConfig() {
        String blackDuckUrl = "http://this.might.exist/somewhere";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setApiToken("a valid, non-empty api token");
        assertTrue(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testValidConfigWithUsernameAndPassword() {
        String blackDuckUrl = "http://this.might.exist/somewhere";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword("a valid, non-blank username", "a valid, non-blank password");
        blackDuckServerConfigBuilder.setCredentials(credentialsBuilder.build());
        assertTrue(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testInvalidConfigWithBlankUsernameAndPassword() {
        String blackDuckUrl = "http://this.might.exist/somewhere";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword("", null);
        blackDuckServerConfigBuilder.setCredentials(credentialsBuilder.build());
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testTimeout() {
        String blackDuckUrl = "http://this.might.exist/somewhere";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setTimeout(0);
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

}
