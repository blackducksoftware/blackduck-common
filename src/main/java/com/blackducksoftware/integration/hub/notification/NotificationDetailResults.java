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

public class NotificationDetailResults extends NotificationResults<NotificationDetailResult> {
    private final List<NotificationDetailResult> notificationResults;

    public NotificationDetailResults(final List<NotificationDetailResult> notificationResults, final Date latestNotificationCreatedAtDate, final String latestNotificationCreatedAtString) {
        super(latestNotificationCreatedAtDate, latestNotificationCreatedAtString);
        this.notificationResults = notificationResults;
    }

    @Override
    public List<NotificationDetailResult> getResults() {
        return Collections.unmodifiableList(notificationResults);
    }

    public Set<UriSingleResponse<? extends HubResponse>> getAllLinks() {
        final Set<UriSingleResponse<? extends HubResponse>> uriResponses = new HashSet<>();
        notificationResults.forEach(result -> uriResponses.addAll(result.getAllLinks()));
        return uriResponses;
    }

}
