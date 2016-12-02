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
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;

public class HubServerConfigBuilderTest {

    private static final String ERROR_MSG_NO_HUB_TIMEOUT = "No Hub Timeout was found.";

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

    private List<String> getMessages(final ValidationResults<GlobalFieldKey, HubServerConfig> result) {
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
    public void testValidateHubURLEmpty() throws Exception {
        expectedMessages.add(HubServerConfigBuilder.ERROR_MSG_URL_NOT_FOUND);

        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<>();
        builder.validateHubUrl(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateHubURLMalformed() throws Exception {
        expectedMessages.add(HubServerConfigBuilder.ERROR_MSG_URL_NOT_VALID);

        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setHubUrl("TestString");
        final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<>();
        builder.validateHubUrl(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateHubURLMalformed2() throws Exception {
        expectedMessages.add(HubServerConfigBuilder.ERROR_MSG_URL_NOT_VALID_PREFIX);

        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setHubUrl("http:TestString");
        final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<>();
        builder.validateHubUrl(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateHubURLNotExisting() throws Exception {
        expectedMessages.add(HubServerConfigBuilder.ERROR_MSG_UNREACHABLE_PREFIX);

        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setHubUrl("http://TestString");
        final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<>();
        builder.validateHubUrl(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateHubURL() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setHubUrl("https://www.google.com");
        final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<>();
        builder.validateHubUrl(result);
        assertTrue(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateHubTimeoutEmpty() throws Exception {
        expectedMessages.add(ERROR_MSG_NO_HUB_TIMEOUT);

        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<>();
        builder.validateTimeout(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateHubTimeoutInvalid() throws Exception {
        expectedMessages.add("The String : TestString , is not an Integer");

        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setTimeout("TestString");
        final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<>();
        builder.validateTimeout(result);
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateHubTimeout() throws Exception {

        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setTimeout("678");
        final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<>();
        builder.validateTimeout(result);
        assertTrue(result.isSuccess());

        actualMessages = getMessages(result);
    }

}
