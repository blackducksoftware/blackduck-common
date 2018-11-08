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
package com.synopsys.integration.blackduck.global;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;
import com.synopsys.integration.rest.credentials.Credentials;

public class HubServerConfigBuilderTest {
    @Test
    public void testValidateHubURLEmpty() {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        assertFalse(hubServerConfigBuilder.isValid());
    }

    @Test
    public void testValidateHubURLMalformed() {
        final String blackDuckUrl = "TestString";

        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setUrl(blackDuckUrl);
        assertFalse(hubServerConfigBuilder.isValid());
    }

    @Test
    public void testValidateHubURLMalformed2() {
        final String blackDuckUrl = "http:TestString";

        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setUrl(blackDuckUrl);
        assertFalse(hubServerConfigBuilder.isValid());
    }

    @Test
    public void testValidConfig() {
        final String blackDuckUrl = "http://this.might.exist/somewhere";

        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setUrl(blackDuckUrl);
        hubServerConfigBuilder.setApiToken("a valid, non-empty api token");
        assertTrue(hubServerConfigBuilder.isValid());
    }

    @Test
    public void testValidConfigWithUsernameAndPassword() {
        final String blackDuckUrl = "http://this.might.exist/somewhere";

        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setUrl(blackDuckUrl);
        hubServerConfigBuilder.setCredentials(new Credentials("a valid, non-blank username", "a valid, non-blank password"));
        assertTrue(hubServerConfigBuilder.isValid());
    }

    @Test
    public void testInvalidConfigWithBlankUsernameAndPassword() {
        final String blackDuckUrl = "http://this.might.exist/somewhere";

        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setUrl(blackDuckUrl);
        hubServerConfigBuilder.setCredentials(new Credentials("", null));
        assertFalse(hubServerConfigBuilder.isValid());
    }

    @Test
    public void testTimeout() {
        final String blackDuckUrl = "http://this.might.exist/somewhere";

        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setUrl(blackDuckUrl);
        hubServerConfigBuilder.setTimeout(0);
        assertFalse(hubServerConfigBuilder.isValid());
    }

}
