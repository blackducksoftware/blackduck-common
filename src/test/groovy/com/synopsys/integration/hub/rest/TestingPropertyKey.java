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
package com.synopsys.integration.hub.rest;

public enum TestingPropertyKey {
    TEST_HUB_SERVER_URL,
    TEST_HTTPS_HUB_SERVER_URL,
    TEST_HTTPS_IGNORE_HOST,
    TEST_USERNAME,
    TEST_PASSWORD,
    TEST_TRUST_HTTPS_CERT,
    TEST_PROJECT,
    TEST_VERSION,
    TEST_PHASE,
    TEST_DISTRIBUTION,
    TEST_CREATE_PROJECT,
    TEST_CREATE_VERSION,
    TEST_HUB_TIMEOUT,
    TEST_REPORT_PROJECT,
    TEST_REPORT_VERSION,
    TEST_PROJECT_COMPONENT,
    TEST_PROJECT_COMPONENT_VERSION,
    TEST_PROJECT_COMPONENT_USAGE,
    TEST_SCAN_PROJECT,
    TEST_SCAN_VERSION,
    TEST_PROXY_HOST_PASSTHROUGH,
    TEST_PROXY_PORT_PASSTHROUGH,
    TEST_PROXY_HOST_BASIC,
    TEST_PROXY_PORT_BASIC,
    TEST_PROXY_USER_BASIC,
    TEST_PROXY_PASSWORD_BASIC,
    TEST_PROXY_HOST_DIGEST,
    TEST_PROXY_PORT_DIGEST,
    TEST_PROXY_USER_DIGEST,
    TEST_PROXY_PASSWORD_DIGEST,
    TEST_PROXY_HOST_NTLM,
    TEST_PROXY_PORT_NTLM,
    TEST_PROXY_USER_NTLM,
    TEST_PROXY_PASSWORD_NTLM,
    TEST_NOTIFICATION_START_DATE,
    TEST_NOTIFICATION_END_DATE,
    TEST_NOTIFICATION_LIMIT,
    TEST_NOTIFICATION_COUNT,
    TEST_NOTIFICATION_TYPE,
    TEST_RULE_VIOLATION_NOTIFICATION_JSON_FILE,
    TEST_RULE_VIOLATION_NOTIFICATION_COMPONENT,
    TEST_RULE_VIOLATION_NOTIFICATION_RULE,
    TEST_RULE_VIOLATION_NOTIFICATION_CLEARED_JSON_FILE,
    TEST_RULE_VIOLATION_NOTIFICATION_CLEARED_COMPONENT,
    TEST_RULE_VIOLATION_NOTIFICATION_CLEARED_RULE,
    TEST_VULNERABLE_COMPONENT_PROJECT_ID,
    TEST_VULNERABLE_COMPONENT_PROJECT_VERSION_ID,
    TEST_VULNERABLE_COMPONENT_NAME,
    TEST_VULNERABLE_COMPONENT_MIN_VULNERABILITIES,
    TEST_VULNERABLE_COMPONENT_VULNERABILITY_NAME,
    LOG_DETAILS_TO_CONSOLE;

}
