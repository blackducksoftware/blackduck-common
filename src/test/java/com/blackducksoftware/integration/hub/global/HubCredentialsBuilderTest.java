/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.builder.ValidationResults;
import com.blackducksoftware.integration.hub.builder.HubCredentialsBuilder;

public class HubCredentialsBuilderTest {
    private static final String VALID_PASSWORD = "Password";

    private static final String VALID_USERNAME = "User";

    private static final String ERROR_MSG_NO_PASSWORD_FOUND = "No Hub Password was found.";

    private static final String ERROR_MSG_NO_USER_FOUND = "No Hub Username was found.";

    private List<String> expectedMessages;

    private List<String> actualMessages;

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

    private List<String> getMessages(final ValidationResults<GlobalFieldKey, HubCredentials> result) {
        final List<String> messageList = new ArrayList<>();
        final Map<GlobalFieldKey, Set<String>> resultMap = result.getResultMap();
        for (final GlobalFieldKey key : resultMap.keySet()) {
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
    public void testvalidateCredentialsNull() throws Exception {
        expectedMessages.add(ERROR_MSG_NO_USER_FOUND);
        expectedMessages.add(ERROR_MSG_NO_PASSWORD_FOUND);

        final HubCredentialsBuilder builder = new HubCredentialsBuilder();
        final ValidationResults<GlobalFieldKey, HubCredentials> result = new ValidationResults<>();
        builder.validateCredentials(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testvalidateCredentialsEmpty() throws Exception {
        expectedMessages.add(ERROR_MSG_NO_USER_FOUND);
        expectedMessages.add(ERROR_MSG_NO_PASSWORD_FOUND);

        final HubCredentialsBuilder builder = new HubCredentialsBuilder();
        builder.setUsername("");
        builder.setPassword("   ");
        final ValidationResults<GlobalFieldKey, HubCredentials> result = new ValidationResults<>();
        builder.validateCredentials(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testvalidateCredentials() throws Exception {
        final HubCredentialsBuilder builder = new HubCredentialsBuilder();
        builder.setUsername(VALID_USERNAME);
        builder.setPassword(VALID_PASSWORD);
        final ValidationResults<GlobalFieldKey, HubCredentials> result = new ValidationResults<>();
        builder.validateCredentials(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testValidateHubUserNull() throws Exception {
        expectedMessages.add(ERROR_MSG_NO_USER_FOUND);

        final HubCredentialsBuilder builder = new HubCredentialsBuilder();
        final ValidationResults<GlobalFieldKey, HubCredentials> result = new ValidationResults<>();
        builder.validateUsername(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateHubUser() throws Exception {
        final HubCredentialsBuilder builder = new HubCredentialsBuilder();
        builder.setUsername(VALID_USERNAME);
        final ValidationResults<GlobalFieldKey, HubCredentials> result = new ValidationResults<>();
        builder.validateUsername(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testValidateHubPasswordNull() throws Exception {
        expectedMessages.add(ERROR_MSG_NO_PASSWORD_FOUND);

        final HubCredentialsBuilder builder = new HubCredentialsBuilder();
        final ValidationResults<GlobalFieldKey, HubCredentials> result = new ValidationResults<>();
        builder.validatePassword(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateHubPassword() throws Exception {
        final HubCredentialsBuilder builder = new HubCredentialsBuilder();
        builder.setPassword(VALID_PASSWORD);
        final ValidationResults<GlobalFieldKey, HubCredentials> result = new ValidationResults<>();
        builder.validatePassword(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testBuildWithValidInput() throws Exception {
        final HubCredentialsBuilder builder = new HubCredentialsBuilder();
        builder.setUsername(VALID_USERNAME);
        builder.setPassword(VALID_PASSWORD);

        final ValidationResults<GlobalFieldKey, HubCredentials> result = builder.buildResults();
        assertNotNull(result);
        assertTrue(result.isSuccess());

        final HubCredentials credentials = result.getConstructedObject();
        assertNotNull(credentials);
        assertEquals(VALID_USERNAME, credentials.getUsername());
        assertEquals(VALID_PASSWORD, credentials.getDecryptedPassword());
    }

    @Test
    public void testBuildWithEmptyInput() throws Exception {
        final HubCredentialsBuilder builder = new HubCredentialsBuilder();

        final ValidationResults<GlobalFieldKey, HubCredentials> result = builder.buildResults();

        assertNotNull(result);
        assertFalse(result.isSuccess());

        final HubCredentials credentials = result.getConstructedObject();
        assertNotNull(credentials);
        assertNull(credentials.getUsername());
        assertNull(credentials.getEncryptedPassword());
    }
}
