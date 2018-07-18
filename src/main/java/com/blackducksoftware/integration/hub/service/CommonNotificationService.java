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
package com.blackducksoftware.integration.hub.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationUserView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.notification.CommonNotificationView;
import com.blackducksoftware.integration.hub.notification.CommonNotificationViewResults;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetailFactory;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService;
import com.blackducksoftware.integration.rest.RestConstants;

public class CommonNotificationService {
    private final NotificationContentDetailFactory notificationContentDetailFactory;
    private final boolean oldestFirst;

    public CommonNotificationService(final NotificationContentDetailFactory notificationContentDetailFactory, final boolean oldestFirst) {
        this.notificationContentDetailFactory = notificationContentDetailFactory;
        this.oldestFirst = oldestFirst;
    }

    public List<CommonNotificationView> getCommonNotifications(final List<NotificationView> notificationViews) {
        final List<CommonNotificationView> commonStates = notificationViews
                .stream()
                .map(view -> {
                    return new CommonNotificationView(view);
                }).collect(Collectors.toList());

        return commonStates;
    }

    public List<CommonNotificationView> getCommonUserNotifications(final List<NotificationUserView> notificationUserViews) {
        final List<CommonNotificationView> commonStates = notificationUserViews
                .stream()
                .map(view -> {
                    return new CommonNotificationView(view);
                }).collect(Collectors.toList());

        return commonStates;
    }

    public CommonNotificationViewResults getCommonNotificationViewResults(final List<CommonNotificationView> commonNotifications) {
        if (commonNotifications == null || commonNotifications.isEmpty()) {
            return new CommonNotificationViewResults(Collections.emptyList(), Optional.empty(), Optional.empty());
        }

        final DatePair datePair = getLatestCreatedAtString(commonNotifications);
        return new CommonNotificationViewResults(commonNotifications, datePair.date, datePair.dateString);
    }

    public NotificationDetailResults getNotificationDetailResults(final List<CommonNotificationView> commonNotifications) throws IntegrationException {
        if (commonNotifications == null || commonNotifications.isEmpty()) {
            return new NotificationDetailResults(Collections.emptyList(), Optional.empty(), Optional.empty());
        }

        List<NotificationDetailResult> sortedDetails = commonNotifications
                .stream()
                .map(view -> notificationContentDetailFactory.generateContentDetails(view))
                .collect(Collectors.toList());

        if (oldestFirst) {
            // we don't want to use the default sorting from the hub
            sortedDetails = sortedDetails
                    .stream()
                    .sorted((result_1, result_2) -> result_1.getCreatedAt().compareTo(result_2.getCreatedAt()))
                    .collect(Collectors.toList());
        }

        final DatePair datePair = getLatestCreatedAtString(commonNotifications);
        return new NotificationDetailResults(sortedDetails, datePair.date, datePair.dateString);
    }

    public void populateHubBucket(final HubBucketService hubBucketService, final HubBucket hubBucket, final NotificationDetailResults notificationDetailResults) throws IntegrationException {
        final List<UriSingleResponse<? extends HubResponse>> uriResponseList = new ArrayList<>();
        uriResponseList.addAll(notificationDetailResults.getAllLinks());
        hubBucketService.addToTheBucket(hubBucket, uriResponseList);
    }

    private DatePair getLatestCreatedAtString(final List<CommonNotificationView> views) {
        // sortedViews will be sorted most recent to oldest
        final List<CommonNotificationView> sortedViews = views.stream().sorted((left, right) -> {
            return right.getCreatedAt().compareTo(left.getCreatedAt());
        }).collect(Collectors.toList());

        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // we know that the first in the list is the most current
        final Date latestCreatedAtDate = sortedViews.get(0).getCreatedAt();
        final String latestCreatedAtString = sdf.format(latestCreatedAtDate);
        return new DatePair(latestCreatedAtDate, latestCreatedAtString);
    }

    private static class DatePair {
        public final Optional<Date> date;
        public final Optional<String> dateString;

        public DatePair(final Date date, final String dateString) {
            this.date = Optional.ofNullable(date);
            this.dateString = Optional.ofNullable(dateString);
        }
    }

}
