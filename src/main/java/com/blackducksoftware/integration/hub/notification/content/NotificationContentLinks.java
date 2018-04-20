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

/**
 * For Notification types [] there are 1 or more related project/version and component/version links
 */
public class NotificationContentLinks extends Stringable {
    private final String projectVersionLink;
    private final String componentLink;
    private final String componentVersionLink;

    public static NotificationContentLinks createLinksWithComponentOnly(final String projectVersionLink, final String componentLink) {
        return new NotificationContentLinks(projectVersionLink, componentLink, null);
    }

    public static NotificationContentLinks createLinksWithComponentVersion(final String projectVersionLink, final String componentVersionLink) {
        return new NotificationContentLinks(projectVersionLink, null, componentVersionLink);
    }

    private NotificationContentLinks(final String projectVersionLink, final String componentLink, final String componentVersionLink) {
        this.projectVersionLink = projectVersionLink;
        this.componentLink = componentLink;
        this.componentVersionLink = componentVersionLink;
    }

    public boolean hasComponentVersion() {
        return componentVersionLink != null;
    }

    public boolean hasOnlyComponent() {
        return componentLink != null;
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

}
