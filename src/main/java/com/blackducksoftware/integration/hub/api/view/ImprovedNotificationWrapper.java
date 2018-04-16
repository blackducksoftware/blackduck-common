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

public class ImprovedNotificationWrapper implements ImprovedCommonNotificationState {
    public ReducedNotificationView reducedNotificationView;
    public NotificationContent notificationContent;

    public ImprovedNotificationWrapper(final ReducedNotificationView reducedNotificationView, final NotificationContent notificationContent) {
        this.reducedNotificationView = reducedNotificationView;
        this.notificationContent = notificationContent;
    }

    public ReducedNotificationView getReducedNotificationView() {
        return reducedNotificationView;
    }

    @Override
    public NotificationContent getNotificationContent() {
        return notificationContent;
    }

    @Override
    public String getContentType() {
        return reducedNotificationView.contentType;
    }

    @Override
    public Date getCreatedAt() {
        return reducedNotificationView.createdAt;
    }

    @Override
    public NotificationType getType() {
        return reducedNotificationView.type;
    }

    @Override
    public String getJson() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setContentType(final String contentType) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCreatedAt(final Date createdAt) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setType(final NotificationType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setJson(final String json) {
        // TODO Auto-generated method stub

    }

}
