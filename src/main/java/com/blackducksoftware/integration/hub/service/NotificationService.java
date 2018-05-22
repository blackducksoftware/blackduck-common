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
import com.blackducksoftware.integration.hub.api.core.HubPathMultipleResponses;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationUserView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.notification.CommonNotificationView;
import com.blackducksoftware.integration.hub.notification.CommonNotificationViewResults;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetailFactory;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService;
import com.blackducksoftware.integration.hub.service.model.RequestFactory;
import com.blackducksoftware.integration.rest.RestConstants;
import com.blackducksoftware.integration.rest.request.Request;

public class NotificationService extends DataService {
    private final HubBucketService hubBucketService;
    private final NotificationContentDetailFactory notificationContentDetailFactory;
    private final boolean oldestFirst;

    public NotificationService(final HubService hubService, final HubBucketService hubBucketService) {
        super(hubService);
        this.hubBucketService = hubBucketService;
        this.notificationContentDetailFactory = new NotificationContentDetailFactory(hubService.getGson(), hubService.getJsonParser());
        // hub default behavior is to return the latest notifications first.
        this.oldestFirst = false;
    }

    public NotificationService(final HubService hubService, final HubBucketService hubBucketService, final boolean oldestFirst) {
        super(hubService);
        this.hubBucketService = hubBucketService;
        this.notificationContentDetailFactory = new NotificationContentDetailFactory(hubService.getGson(), hubService.getJsonParser());
        this.oldestFirst = oldestFirst;
    }

    public NotificationService(final HubService hubService, final HubBucketService hubBucketService, final NotificationContentDetailFactory notificationContentDetailFactory, final boolean oldestFirst) {
        super(hubService);
        this.hubBucketService = hubBucketService;
        this.notificationContentDetailFactory = notificationContentDetailFactory;
        this.oldestFirst = oldestFirst;
    }

    public List<NotificationView> getAllNotifications(final Date startDate, final Date endDate) throws IntegrationException {
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate);
        final HubPathMultipleResponses<NotificationView> notificationLinkResponse = new HubPathMultipleResponses<>(ApiDiscovery.NOTIFICATIONS_LINK, NotificationView.class);
        final List<NotificationView> allNotificationItems = hubService.getResponses(notificationLinkResponse, requestBuilder, true);
        return allNotificationItems;
    }

    public List<NotificationUserView> getAllUserNotifications(final UserView user, final Date startDate, final Date endDate) throws IntegrationException {
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate);
        final String userNotificationsUri = hubService.getFirstLink(user, UserView.NOTIFICATIONS_LINK);
        requestBuilder.uri(userNotificationsUri);

        final List<NotificationUserView> allUserNotificationItems = hubService.getResponses(requestBuilder, NotificationUserView.class, true);
        return allUserNotificationItems;
    }

    public CommonNotificationViewResults getAllCommonNotificationViewResults(final Date startDate, final Date endDate) throws IntegrationException {
        final List<NotificationView> allNotificationItems = getAllNotifications(startDate, endDate);
        final List<CommonNotificationView> commonNotifications = getCommonNotifications(allNotificationItems);
        return createNotificationViewResults(commonNotifications);
    }

    public CommonNotificationViewResults getAllCommonNotificationViewResults(final UserView user, final Date startDate, final Date endDate) throws IntegrationException {
        final List<NotificationUserView> allNotificationItems = getAllUserNotifications(user, startDate, endDate);
        final List<CommonNotificationView> commonNotifications = getCommonUserNotifications(allNotificationItems);
        return createNotificationViewResults(commonNotifications);
    }

    public NotificationDetailResults getAllNotificationDetailResults(final HubBucket hubBucket, final Date startDate, final Date endDate) throws IntegrationException {
        final List<NotificationView> notificationViewResults = getAllNotifications(startDate, endDate);
        final List<CommonNotificationView> commonNotificationViews = getCommonNotifications(notificationViewResults);
        final NotificationDetailResults results = createNotificationDetailResults(hubBucket, commonNotificationViews);
        return results;
    }

    public NotificationDetailResults getAllUserNotificationDetailResults(final HubBucket hubBucket, final UserView user, final Date startDate, final Date endDate) throws IntegrationException {
        final List<NotificationUserView> notificationViewResults = getAllUserNotifications(user, startDate, endDate);
        final List<CommonNotificationView> commonNotificationViews = getCommonUserNotifications(notificationViewResults);
        final NotificationDetailResults results = createNotificationDetailResults(hubBucket, commonNotificationViews);
        return results;
    }

    public NotificationDetailResults getAllNotificationDetailResultsPopulated(final HubBucket hubBucket, final Date startDate, final Date endDate) throws IntegrationException {
        final List<NotificationView> notificationViewResults = getAllNotifications(startDate, endDate);
        final List<CommonNotificationView> commonNotificationViews = getCommonNotifications(notificationViewResults);
        final NotificationDetailResults results = createNotificationDetailResults(hubBucket, commonNotificationViews);
        populateNotificationDetailResults(results);
        return results;
    }

    public NotificationDetailResults getAllUserNotificationDetailResultsPopulated(final HubBucket hubBucket, final UserView user, final Date startDate, final Date endDate) throws IntegrationException {
        final List<NotificationUserView> notificationViewResults = getAllUserNotifications(user, startDate, endDate);
        final List<CommonNotificationView> commonNotificationViews = getCommonUserNotifications(notificationViewResults);
        final NotificationDetailResults results = createNotificationDetailResults(hubBucket, commonNotificationViews);
        populateNotificationDetailResults(results);
        return results;
    }

    /**
     * @return The java.util.Date of the most recent notification. If there are no notifications, the current date will be returned. This can set an initial start time window for all future notifications.
     * @throws IntegrationException
     */
    public Date getLatestNotificationDate() throws IntegrationException {
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(1, RequestFactory.DEFAULT_OFFSET);
        final List<NotificationView> notifications = hubService.getResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, false);
        if (notifications.size() == 1) {
            return notifications.get(0).createdAt;
        } else {
            return new Date();
        }
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

    public void populateNotificationDetailResults(final NotificationDetailResults notificationDetailResults) throws IntegrationException {
        final List<UriSingleResponse<? extends HubResponse>> uriResponseList = new ArrayList<>();
        uriResponseList.addAll(notificationDetailResults.getAllLinks());
        hubBucketService.addToTheBucket(notificationDetailResults.getHubBucket(), uriResponseList);
    }

    private CommonNotificationViewResults createNotificationViewResults(final List<CommonNotificationView> commonNotifications) {
        if (commonNotifications == null || commonNotifications.isEmpty()) {
            return new CommonNotificationViewResults(Collections.emptyList(), Optional.empty(), Optional.empty());
        }

        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // we know that the first notification in the list is the most current
        final Date latestCreatedAtDate = commonNotifications.get(0).getCreatedAt();
        final String latestCreatedAtString = sdf.format(latestCreatedAtDate);
        return new CommonNotificationViewResults(commonNotifications, Optional.of(latestCreatedAtDate), Optional.of(latestCreatedAtString));
    }

    private NotificationDetailResults createNotificationDetailResults(final HubBucket hubBucket, final List<CommonNotificationView> views)
            throws IntegrationException {
        if (views == null || views.isEmpty()) {
            return new NotificationDetailResults(Collections.emptyList(), Optional.empty(), Optional.empty(), hubBucket);
        }
        List<NotificationDetailResult> sortedDetails;
        final List<NotificationDetailResult> details = new ArrayList<>();

        views.forEach(view -> {
            details.add(notificationContentDetailFactory.generateContentDetails(view));
        });

        if (oldestFirst) {
            sortedDetails = details.stream().sorted((result_1, result_2) -> {
                return result_1.getCreatedAt().compareTo(result_2.getCreatedAt());
            }).collect(Collectors.toList());
        } else {
            // use the default sorting from the Hub.
            sortedDetails = details;
        }

        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Date latestCreatedAtDate = views.get(0).getCreatedAt();
        final String latestCreatedAtString = sdf.format(latestCreatedAtDate);
        return new NotificationDetailResults(sortedDetails, Optional.of(latestCreatedAtDate), Optional.of(latestCreatedAtString), hubBucket);
    }

    private Request.Builder createNotificationRequestBuilder(final Date startDate, final Date endDate) {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        return RequestFactory.createCommonGetRequestBuilder().addQueryParameter("startDate", startDateString).addQueryParameter("endDate", endDateString);
    }

}
