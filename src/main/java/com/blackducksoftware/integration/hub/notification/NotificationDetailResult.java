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
package com.blackducksoftware.integration.hub.notification;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.enumeration.NotificationTypeGrouping;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.detail.ProjectNotificationContentDetail;

public class NotificationDetailResult {
    private final List<? extends NotificationContentDetail> notificationDetails;
    private final String contentType;
    private final Date createdAt;
    private final NotificationType type;

    // @formatter:off
    public NotificationDetailResult(
             final List<? extends NotificationContentDetail> notificationDetails
            ,final String contentType
            ,final Date createdAt
            ,final NotificationType type
            ) {
        this.notificationDetails = notificationDetails;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.type = type;
    }
    // @formatter:on

    public List<? extends NotificationContentDetail> getNotificationDetails() {
        return notificationDetails;
    }

    public List<ProjectNotificationContentDetail> getProjectNotificationDetails() {
        final NotificationTypeGrouping grouping = NotificationTypeGrouping.fromNotificationType(type);
        if (NotificationTypeGrouping.POLICY.equals(grouping) || NotificationTypeGrouping.VULNERABILITY.equals(grouping)) {
            return (List<ProjectNotificationContentDetail>) notificationDetails;
        }
        return Collections.emptyList();
    }

    public String getContentType() {
        return contentType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public NotificationType getType() {
        return type;
    }

    public Set<UriSingleResponse<? extends HubResponse>> getAllLinks() {
        final Set<UriSingleResponse<? extends HubResponse>> uriResponses = new HashSet<>();
        notificationDetails.forEach(detail -> {
            uriResponses.addAll(detail.getPresentLinks());
        });
        return uriResponses;
    }

}
