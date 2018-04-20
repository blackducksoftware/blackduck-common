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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class RuleViolationClearedNotificationContent extends NotificationContent {
    public String projectName;
    public String projectVersionName;
    public int componentVersionsInViolation;
    public List<ComponentVersionStatus> componentVersionStatuses;

    @SerializedName("projectVersion")
    public String projectVersionLink;

    @Override
    public boolean providesProjectComponentDetails() {
        return true;
    }

    @Override
    public List<NotificationContentLinks> getNotificationContentLinks() {
        final List<NotificationContentLinks> links = new ArrayList<>();
        componentVersionStatuses.forEach(componentVersionStatus -> {
            if (componentVersionStatus.componentVersionLink != null) {
                links.add(NotificationContentLinks.createLinksWithComponentVersion(projectVersionLink, componentVersionStatus.componentVersionLink));
            } else {
                links.add(NotificationContentLinks.createLinksWithComponentOnly(projectVersionLink, componentVersionStatus.componentLink));
            }
        });
        return links;
    }

}
