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
package com.blackducksoftware.integration.hub.notification.content.detail;

import java.util.List;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.enumeration.NotificationTypeGrouping;
import com.blackducksoftware.integration.util.Stringable;

public abstract class NotificationContentDetail2 extends Stringable {
    private final NotificationTypeGrouping notificationTypeGrouping;

    public NotificationContentDetail2(final NotificationTypeGrouping notificationTypeGrouping) {
        this.notificationTypeGrouping = notificationTypeGrouping;
    }

    public NotificationTypeGrouping getNotificationTypeGrouping() {
        return notificationTypeGrouping;
    }

    public abstract List<UriSingleResponse<? extends HubResponse>> getPresentLinks();

    protected final <T extends HubResponse> UriSingleResponse<T> createUriSingleResponse(final String uri, final Class<T> responseClass) {
        if (uri != null && responseClass != null) {
            return new UriSingleResponse<>(uri, responseClass);
        }
        return null;
    }

}
