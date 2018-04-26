/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.throwaway;

public class NotificationEventConstants {
    public static final String EVENT_KEY_NAME_VALUE_SEPARATOR = "=";
    public static final String EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR = "|";
    public static final String EVENT_KEY_ISSUE_TYPE_NAME = "t";
    public static final String EVENT_KEY_ISSUE_TYPE_VALUE_POLICY = "p";
    public static final String EVENT_KEY_ISSUE_TYPE_VALUE_VULNERABILITY = "v";
    public static final String EVENT_KEY_JIRA_PROJECT_ID_NAME = "jp";
    public static final String EVENT_KEY_HUB_PROJECT_VERSION_REL_URL_HASHED_NAME = "hpv";
    public static final String EVENT_KEY_HUB_COMPONENT_REL_URL_HASHED_NAME = "hc";
    public static final String EVENT_KEY_HUB_COMPONENT_VERSION_REL_URL_HASHED_NAME = "hcv";
    public static final String EVENT_KEY_HUB_POLICY_RULE_REL_URL_HASHED_NAME = "hr";

    private NotificationEventConstants() throws InstantiationException {
        throw new InstantiationException("Cannot instantiate instance of utility class '" + getClass().getName() + "'");
    }
}
