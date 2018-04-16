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
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;

// This is named a ReducedNotificationView because it removes the 'content' member from NotificationView which is generated.
// This is a short term fix to release dependent projects.  A larger more appropriate fix to extract content will be made in a future release.
// In a future release this object will be removed and the parallel processing will extract the content from the notifications.
public abstract class ReducedNotificationView extends HubView implements ReducedCommonNotificationState {
    public String contentType;
    public java.util.Date createdAt;
    public NotificationType type;

    public ReducedNotificationView() {
        // gson requires a default constructor.
        // this class is abstract because it must be sub-classed to define the content member.
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
