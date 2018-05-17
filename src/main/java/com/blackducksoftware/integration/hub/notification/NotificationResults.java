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
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.hub.service.bucket.HubBucket;

public class NotificationResults {
    private final List<NotificationViewResult> notificationResults;
    private final Optional<Date> latestNotificationCreatedAtDate;
    private final Optional<String> latestNotificationCreatedAtString;
    private final HubBucket hubBucket;

    public NotificationResults(final NotificationViewResults notificationViewResults, final HubBucket hubBucket) {
        this.notificationResults = notificationViewResults.getResultList();
        this.latestNotificationCreatedAtDate = notificationViewResults.getLatestNotificationCreatedAtDate();
        this.latestNotificationCreatedAtString = notificationViewResults.getLatestNotificationCreatedAtString();
        this.hubBucket = hubBucket;
    }

    public List<NotificationViewResult> getNotificationResults() {
        return notificationResults;
    }

    public Optional<Date> getLatestNotificationCreatedAtDate() {
        return latestNotificationCreatedAtDate;
    }

    public Optional<String> getLatestNotificationCreatedAtString() {
        return latestNotificationCreatedAtString;
    }

    public HubBucket getHubBucket() {
        return hubBucket;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }
}
