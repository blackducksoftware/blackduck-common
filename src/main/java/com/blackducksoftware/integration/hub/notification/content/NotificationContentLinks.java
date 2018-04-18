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
package com.blackducksoftware.integration.hub.notification.content;

import com.blackducksoftware.integration.util.Stringable;

public class NotificationContentLinks extends Stringable {
    private final String projectVersionLink;
    private final String componentLink;
    private final String componentVersionLink;
    private final String policyLink;
    private final String componentIssueLink;

    public static NotificationContentLinks createPolicyLinksWithComponentOnly(final String projectVersionLink, final String componentLink, final String policyLink) {
        return new NotificationContentLinks(projectVersionLink, componentLink, null, policyLink, null);
    }

    public static NotificationContentLinks createPolicyLinksWithComponentVersion(final String projectVersionLink, final String componentVersionLink, final String policyLink) {
        return new NotificationContentLinks(projectVersionLink, null, componentVersionLink, policyLink, null);
    }

    public static NotificationContentLinks createVulnerabilityLinks(final String projectVersionLink, final String componentVersionLink, final String componentIssueLink) {
        return new NotificationContentLinks(projectVersionLink, null, componentVersionLink, null, componentIssueLink);
    }

    private NotificationContentLinks(final String projectVersionLink, final String componentLink, final String componentVersionLink, final String policyLink, final String componentIssueLink) {
        this.projectVersionLink = projectVersionLink;
        this.componentLink = componentLink;
        this.componentVersionLink = componentVersionLink;
        this.policyLink = policyLink;
        this.componentIssueLink = componentIssueLink;
    }

    public boolean hasComponentVersion() {
        return componentVersionLink != null;
    }

    public boolean hasOnlyComponent() {
        return componentLink != null;
    }

    public boolean hasPolicy() {
        return policyLink != null;
    }

    public boolean hasVulnerability() {
        return componentIssueLink != null;
    }

    public String getProjectVersionLink() {
        return projectVersionLink;
    }

    public String getComponentLink() {
        return componentLink;
    }

    public String getComponentVersionLink() {
        return componentVersionLink;
    }

    public String getPolicyLink() {
        return policyLink;
    }

    public String getComponentIssueLink() {
        return componentIssueLink;
    }

}
