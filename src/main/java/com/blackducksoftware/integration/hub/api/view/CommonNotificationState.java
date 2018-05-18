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

import com.blackducksoftware.integration.hub.api.core.HubView;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationStateRequestStateType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationUserView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.util.Stringable;

/**
 * This is a flattened view of both NotificationView and NotificationUserView and must be manually maintained to support both views and their api. The only common class between both views is HubView which is why sourceView is of that type,
 * but it should only ever be either NotificationView or NotificationUserView.
 */
public class CommonNotificationState extends Stringable {
    private final HubView sourceView;
    private final String contentType;
    private final Date createdAt;
    private final NotificationType type;
    private final NotificationStateRequestStateType notificationState;

    public CommonNotificationState(final NotificationView notificationView) {
        this.sourceView = notificationView;
        this.contentType = notificationView.contentType;
        this.createdAt = notificationView.createdAt;
        this.type = notificationView.type;
        this.notificationState = null;
    }

    public CommonNotificationState(final NotificationUserView notificationUserView) {
        this.sourceView = notificationUserView;
        this.contentType = notificationUserView.contentType;
        this.createdAt = notificationUserView.createdAt;
        this.type = notificationUserView.type;
        this.notificationState = notificationUserView.notificationState;
    }

    public HubView getSourceView() {
        return sourceView;
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

    public NotificationStateRequestStateType getNotificationState() {
        return notificationState;
    }

}
