package com.synopsys.integration.blackduck.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.util.NoThreadExecutorService;

@ExtendWith(TimingExtension.class)
public class BlackDuckServerConfigBuilderTest {
    @Test
    void testSettingFromPropertiesMapWithMixed() {
        Map<String, String> properties = new HashMap<>();
        properties.put("BLACKDUCK_URL", "test url");
        properties.put("blackduck.username", "user");
        properties.put("BLACKDUCK_PASSWORD", "password");

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        assertNull(blackDuckServerConfigBuilder.getUrl());
        assertNull(blackDuckServerConfigBuilder.getUsername());
        assertNull(blackDuckServerConfigBuilder.getPassword());

        blackDuckServerConfigBuilder.setProperties(properties.entrySet());

        assertEquals("test url", blackDuckServerConfigBuilder.getUrl());
        assertEquals("user", blackDuckServerConfigBuilder.getUsername());
        assertEquals("password", blackDuckServerConfigBuilder.getPassword());
    }

    @Test
    public void testValidateBlackDuckURLEmpty() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testNullUrlInvalid() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl((String) null);
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
        blackDuckServerConfigBuilder.setTimeoutInSeconds(0);
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testPopulatingExecutorService() throws Exception {
        ExecutorService executorService = null;
        try {
            executorService = Executors.newSingleThreadExecutor();

            BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
            blackDuckServerConfigBuilder.setUrl("http://this.might.exist/somewhere");
            blackDuckServerConfigBuilder.setApiToken("a valid, non-empty api token");
            BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();

            Field executorServiceField = BlackDuckServerConfig.class.getDeclaredField("executorService");
            executorServiceField.setAccessible(true);
            assertTrue(executorServiceField.get(blackDuckServerConfig) instanceof NoThreadExecutorService);

            blackDuckServerConfigBuilder.setExecutorService(executorService);
            blackDuckServerConfig = blackDuckServerConfigBuilder.build();
            assertNotNull(executorServiceField.get(blackDuckServerConfig));
        } finally {
            assert executorService != null;
            executorService.shutdownNow();
        }
    }

    @Test
    public void testNewBuilder() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newBuilder();
        assertTrue(blackDuckServerConfigBuilder.getKeys().contains(BlackDuckServerConfigBuilder.API_TOKEN_KEY));
        assertTrue(blackDuckServerConfigBuilder.getKeys().contains(BlackDuckServerConfigBuilder.USERNAME_KEY));
        assertTrue(blackDuckServerConfigBuilder.getKeys().contains(BlackDuckServerConfigBuilder.PASSWORD_KEY));
    }

    @Test
    public void testNewApiBuilder() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();
        assertTrue(blackDuckServerConfigBuilder.getKeys().contains(BlackDuckServerConfigBuilder.API_TOKEN_KEY));
        assertFalse(blackDuckServerConfigBuilder.getKeys().contains(BlackDuckServerConfigBuilder.USERNAME_KEY));
        assertFalse(blackDuckServerConfigBuilder.getKeys().contains(BlackDuckServerConfigBuilder.PASSWORD_KEY));
    }

    @Test
    public void testNewUserPassBuilder() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newUserPassBuilder();
        assertFalse(blackDuckServerConfigBuilder.getKeys().contains(BlackDuckServerConfigBuilder.API_TOKEN_KEY));
        assertTrue(blackDuckServerConfigBuilder.getKeys().contains(BlackDuckServerConfigBuilder.USERNAME_KEY));
        assertTrue(blackDuckServerConfigBuilder.getKeys().contains(BlackDuckServerConfigBuilder.PASSWORD_KEY));
    }
}
