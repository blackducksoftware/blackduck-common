/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.builder.HubProxyInfoBuilder;
import com.blackducksoftware.integration.hub.validator.HubProxyValidator;
import com.blackducksoftware.integration.validator.ValidationResults;

public class HubProxyInfoBuilderTest {
    private static final int VALID_PORT = 2303;

    private static final String VALID_HOST = "just need a non-empty string";

    private static final String VALID_PASSWORD = "itsasecret";

    private static final String VALID_USERNAME = "memyselfandi";

    private static final String VALID_IGNORE_HOST_LIST = "google,msn,yahoo";

    private static final String VALID_IGNORE_HOST = "google.*";

    private static final String INVALID_IGNORE_HOST_LIST = "google,[^-z!,abc";

    private static final String INVALID_IGNORE_HOST = "[^-z!";

    private List<String> expectedMessages;

    private List<String> actualMessages;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        expectedMessages = new ArrayList<>();
        actualMessages = new ArrayList<>();
    }

    @After
    public void tearDown() {
        assertEquals("Too many/not enough messages expected: \n" + actualMessages.size(), expectedMessages.size(),
                actualMessages.size());

        for (final String expectedMessage : expectedMessages) {
            boolean foundExpectedMessage = false;
            for (final String actualMessage : actualMessages) {
                if (actualMessage.contains(expectedMessage)) {
                    foundExpectedMessage = true;
                    break;
                }
            }
            assertTrue("Did not find the expected message : " + expectedMessage,
                    foundExpectedMessage);
        }
    }

    private List<String> getMessages(final ValidationResults result) {
        final List<String> messageList = new ArrayList<>();
        final Map<Object, Set<String>> resultMap = result.getResultMap();
        for (final Object key : resultMap.keySet()) {
            final Set<String> resultList = resultMap.get(key);

            for (final String item : resultList) {
                if (StringUtils.isNotBlank(item)) {
                    messageList.add(item);
                }
            }
        }
        return messageList;
    }

    @Test
    public void testValidateProxyConfigHubUrlIgnored() throws Exception {
        final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
        builder.setHost(VALID_HOST);
        builder.setPort(VALID_PORT);
        builder.setIgnoredProxyHosts(VALID_IGNORE_HOST);

        final HubProxyInfo proxyInfo = builder.build();
        final boolean useProxy = proxyInfo.shouldUseProxyForUrl(new URL("https://google.com"));
        assertFalse(useProxy);
    }

    @Test
    public void testValidateProxyConfigHubUrlNotIgnored() throws Exception {
        final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
        builder.setHost(VALID_HOST);
        builder.setPort(VALID_PORT);
        builder.setIgnoredProxyHosts("test");

        final HubProxyInfo proxyInfo = builder.build();
        final boolean useProxy = proxyInfo.shouldUseProxyForUrl(new URL("https://google.com"));
        assertTrue(useProxy);
    }

    @Test
    public void testValidateProxyPort() throws Exception {
        final HubProxyValidator builder = new HubProxyValidator();
        builder.setHost(VALID_HOST);
        builder.setPort(VALID_PORT);
        final ValidationResults result = new ValidationResults();
        builder.validatePort(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testValidateProxyPortNoHost() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_PROXY_HOST_REQUIRED);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost("");
        validator.setPort(VALID_PORT);
        final ValidationResults result = new ValidationResults();
        validator.validatePort(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateCredentialsNoHost() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_PROXY_HOST_NOT_SPECIFIED);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost("");
        validator.setUsername(VALID_USERNAME);
        validator.setPassword(VALID_PASSWORD);
        final ValidationResults result = new ValidationResults();
        validator.validateCredentials(result);
        assertTrue(result.hasErrors());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateCredentialsBothEmpty() throws Exception {
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setUsername("");
        validator.setPassword("");
        final ValidationResults result = new ValidationResults();
        validator.validateCredentials(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testValidateCredentialsBothNotEmpty() throws Exception {
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setUsername(VALID_USERNAME);
        validator.setPassword(VALID_PASSWORD);
        final ValidationResults result = new ValidationResults();
        validator.validateCredentials(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testValidateCredentialsUserOnly() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_CREDENTIALS_INVALID);
        expectedMessages.add(HubProxyValidator.MSG_CREDENTIALS_INVALID);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setUsername(VALID_USERNAME);
        validator.setPassword("");
        final ValidationResults result = new ValidationResults();
        validator.validateCredentials(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateCredentialsPasswordOnly() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_CREDENTIALS_INVALID);
        expectedMessages.add(HubProxyValidator.MSG_CREDENTIALS_INVALID);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setUsername("");
        validator.setPassword(VALID_PASSWORD);
        final ValidationResults result = new ValidationResults();
        validator.validateCredentials(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateIgnoreHostNoProxyHost() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_PROXY_HOST_NOT_SPECIFIED);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost("");
        validator.setIgnoredProxyHosts(VALID_IGNORE_HOST);
        final ValidationResults result = new ValidationResults();
        validator.validateIgnoreHosts(result);
        assertTrue(result.hasErrors());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateIgnoreHost() throws Exception {
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setIgnoredProxyHosts(VALID_IGNORE_HOST);
        final ValidationResults result = new ValidationResults();
        validator.validateIgnoreHosts(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testValidateIgnoreHostList() throws Exception {
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);
        final ValidationResults result = new ValidationResults();
        validator.validateIgnoreHosts(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testValidateIgnoreHostBadPattern() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_IGNORE_HOSTS_INVALID);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setIgnoredProxyHosts(INVALID_IGNORE_HOST);
        final ValidationResults result = new ValidationResults();
        validator.validateIgnoreHosts(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateIgnoreHostListBadPattern() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_IGNORE_HOSTS_INVALID);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setIgnoredProxyHosts(INVALID_IGNORE_HOST_LIST);
        final ValidationResults result = new ValidationResults();
        validator.validateIgnoreHosts(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testAssertWithNoHost() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_PROXY_HOST_NOT_SPECIFIED);
        expectedMessages.add(HubProxyValidator.MSG_PROXY_HOST_REQUIRED);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost("");
        validator.setPort(VALID_PORT);
        validator.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);
        final ValidationResults result = validator.assertValid();
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testAssertWithInvalidPort() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_PROXY_PORT_INVALID);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setPort(-1);
        validator.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);
        final ValidationResults result = validator.assertValid();
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testAssertWithInvalidUser() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_CREDENTIALS_INVALID);
        expectedMessages.add(HubProxyValidator.MSG_CREDENTIALS_INVALID);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setPort(VALID_PORT);
        validator.setUsername("");
        validator.setPassword(VALID_PASSWORD);
        validator.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);
        final ValidationResults result = validator.assertValid();
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testAssertWithInvalidPassword() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_CREDENTIALS_INVALID);
        expectedMessages.add(HubProxyValidator.MSG_CREDENTIALS_INVALID);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setPort(VALID_PORT);
        validator.setUsername(VALID_USERNAME);
        validator.setPassword("");
        validator.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);
        final ValidationResults result = validator.assertValid();
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testAssertWithInvalidIgnoreHost() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_IGNORE_HOSTS_INVALID);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setPort(VALID_PORT);
        validator.setUsername(VALID_USERNAME);
        validator.setPassword(VALID_PASSWORD);
        validator.setIgnoredProxyHosts(INVALID_IGNORE_HOST);
        final ValidationResults result = validator.assertValid();
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testAssertWithInvalidIgnoreHostList() throws Exception {
        expectedMessages.add(HubProxyValidator.MSG_IGNORE_HOSTS_INVALID);
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setPort(VALID_PORT);
        validator.setUsername(VALID_USERNAME);
        validator.setPassword(VALID_PASSWORD);
        validator.setIgnoredProxyHosts(INVALID_IGNORE_HOST_LIST);
        final ValidationResults result = validator.assertValid();
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testAssertWithValidInput() throws Exception {

        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(VALID_HOST);
        validator.setPort(VALID_PORT);
        validator.setUsername(VALID_USERNAME);
        validator.setPassword(VALID_PASSWORD);
        validator.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);
        final ValidationResults result = validator.assertValid();
        assertTrue(result.isSuccess());
    }

    @Test
    public void testBuildWithValidInput() throws Exception {
        final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
        builder.setHost(VALID_HOST);
        builder.setPort(VALID_PORT);
        builder.setUsername(VALID_USERNAME);
        builder.setPassword(VALID_PASSWORD);
        builder.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);

        final HubProxyInfo proxyInfo = builder.build();
        assertNotNull(proxyInfo);
        assertEquals(VALID_HOST, proxyInfo.getHost());
        assertEquals(VALID_PORT, proxyInfo.getPort());
        assertEquals(VALID_USERNAME, proxyInfo.getUsername());
        assertEquals(VALID_PASSWORD, proxyInfo.getDecryptedPassword());
        assertEquals(VALID_IGNORE_HOST_LIST, proxyInfo.getIgnoredProxyHosts());
    }

    @Test
    public void testBuildWithInValidInput() throws Exception {
        final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
        builder.setPort(-512431);
        builder.setUsername(VALID_USERNAME);
        expectedException.expect(IllegalStateException.class);

        final HubProxyInfo proxyInfo = builder.build();
        assertNotNull(proxyInfo);
        assertNull(proxyInfo.getHost());
        assertEquals(-512431, proxyInfo.getPort());
        assertNull(proxyInfo.getUsername());
        assertNull(proxyInfo.getDecryptedPassword());
        assertNull(proxyInfo.getIgnoredProxyHosts());
    }

    @Test
    public void testBuildWithEmptyInput() throws Exception {
        final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
        final HubProxyInfo proxyInfo = builder.build();

        assertNotNull(proxyInfo);
        assertNull(proxyInfo.getHost());
        assertEquals(0, proxyInfo.getPort());
        assertNull(proxyInfo.getUsername());
        assertNull(proxyInfo.getEncryptedPassword());
        assertNull(proxyInfo.getIgnoredProxyHosts());
    }
}
