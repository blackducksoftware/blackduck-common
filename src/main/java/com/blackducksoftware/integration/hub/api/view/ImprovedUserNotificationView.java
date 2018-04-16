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
package com.blackducksoftware.integration.hub.api.view;

import java.util.Date;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;

// This is named ImprovedUserNotificationView because it adds the 'content' member with an actual type.
public class ImprovedUserNotificationView extends ReducedNotificationView implements CommonNotificationState {
    public NotificationContent notificationContent;

    @Override
    public NotificationContent getNotificationContent() {
        return notificationContent;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public NotificationType getType() {
        return type;
    }

    @Override
    public String getJson() {
        return json;
    }

    @Override
    public void setNotificationContent(final NotificationContent notificationContent) {
        this.notificationContent = notificationContent;
    }

    @Override
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @Override
    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setType(final NotificationType type) {
        this.type = type;
    }

    @Override
    public void setJson(final String json) {
        this.json = json;
    }

}
