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

import java.util.Date;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationStateRequestStateType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;

public class NotificationDetailResult {
    private final NotificationContent notificationContent;
    private final String contentType;
    private final Date createdAt;
    private final NotificationType type;
    private final String notificationGroup;
    private final String contentDetailKey;
    private final Optional<NotificationStateRequestStateType> notificationState;

    private final NotificationContentDetail notificationContentDetail;

    public NotificationDetailResult(final NotificationContent notificationContent, final String contentType, final Date createdAt, final NotificationType type, final String notificationGroup, final String contentDetailKey,
            final Optional<NotificationStateRequestStateType> notificationState, final NotificationContentDetail notificationContentDetail) {
        this.notificationContent = notificationContent;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.type = type;
        this.notificationGroup = notificationGroup;
        this.contentDetailKey = contentDetailKey;
        this.notificationState = notificationState;
        this.notificationContentDetail = notificationContentDetail;
    }

    public NotificationContent getNotificationContent() {
        return notificationContent;
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

    public String getNotificationGroup() {
        return notificationGroup;
    }

    public String getContentDetailKey() {
        return contentDetailKey;
    }

    public Optional<NotificationStateRequestStateType> getNotificationState() {
        return notificationState;
    }

    public NotificationContentDetail getNotificationContentDetail() {
        return notificationContentDetail;
    }

}