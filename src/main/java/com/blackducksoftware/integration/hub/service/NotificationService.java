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
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationUserView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.NotificationViewResults;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetailFactory;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService;
import com.blackducksoftware.integration.hub.service.model.RequestFactory;
import com.blackducksoftware.integration.rest.RestConstants;
import com.blackducksoftware.integration.rest.request.Request;

public class NotificationService extends DataService {
    private final HubBucketService hubBucketService;

    public NotificationService(final HubService hubService, final HubBucketService hubBucketService) {
        super(hubService);
        this.hubBucketService = hubBucketService;
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

    public NotificationViewResults getAllNotificationViewResults(final Date startDate, final Date endDate) throws IntegrationException {
        final List<NotificationView> allNotificationItems = getAllNotifications(startDate, endDate);
        final List<CommonNotificationState> commonNotifications = getCommonNotifications(allNotificationItems);
        return createNotificationViewResults(commonNotifications);
    }

    public NotificationViewResults getAllNotificationViewResults(final UserView user, final Date startDate, final Date endDate) throws IntegrationException {
        final List<NotificationUserView> allNotificationItems = getAllUserNotifications(user, startDate, endDate);
        final List<CommonNotificationState> commonNotifications = getCommonUserNotifications(allNotificationItems);
        return createNotificationViewResults(commonNotifications);
    }

    public NotificationDetailResults getAllNotificationResults(final Date startDate, final Date endDate) throws IntegrationException {
        final List<NotificationView> notificationViewResults = getAllNotifications(startDate, endDate);
        final NotificationDetailResults results = createNotificationDetailResults(notificationViewResults);
        return results;
    }

    public NotificationDetailResults getAllUserNotificationResults(final UserView user, final Date startDate, final Date endDate) throws IntegrationException {
        final List<NotificationUserView> notificationViewResults = getAllUserNotifications(user, startDate, endDate);
        final NotificationDetailResults results = createNotificationDetailResults(notificationViewResults);
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

    private NotificationViewResults createNotificationViewResults(final List<CommonNotificationState> commonNotifications) {
        if (commonNotifications == null || commonNotifications.isEmpty()) {
            return new NotificationViewResults(Collections.emptyList(), Optional.empty(), Optional.empty());
        }

        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // we know that the first notification in the list is the most current
        final Date latestCreatedAtDate = commonNotifications.get(0).getCreatedAt();
        final String latestCreatedAtString = sdf.format(latestCreatedAtDate);
        return new NotificationViewResults(commonNotifications, Optional.of(latestCreatedAtDate), Optional.of(latestCreatedAtString));
    }

    public List<CommonNotificationState> getCommonNotifications(final List<NotificationView> notificationViews) {
        final List<CommonNotificationState> commonStates = notificationViews
                .stream()
                .map(view -> {
                    return new CommonNotificationState(view);
                }).collect(Collectors.toList());

        return commonStates;
    }

    public List<CommonNotificationState> getCommonUserNotifications(final List<NotificationUserView> notificationUserViews) {
        final List<CommonNotificationState> commonStates = notificationUserViews
                .stream()
                .map(view -> {
                    return new CommonNotificationState(view);
                }).collect(Collectors.toList());

        return commonStates;
    }

    public List<UriSingleResponse<? extends HubResponse>> getAllLinks(final List<NotificationContentDetail> details) {
        final List<UriSingleResponse<? extends HubResponse>> uriResponses = new ArrayList<>();
        details.forEach(detail -> {
            uriResponses.addAll(detail.getPresentLinks());
        });

        return uriResponses;
    }

    // TODO do the same thing for userViews and abstract common functionality
    private NotificationDetailResults createNotificationDetailResults(final List<NotificationView> views) throws IntegrationException {
        if (views == null || views.isEmpty()) {
            return new NotificationDetailResults(Collections.emptyList(), Optional.empty(), Optional.empty(), new HubBucket());
        }
        final List<NotificationContentDetail> details = new ArrayList<>();

        final NotificationContentDetailFactory detailFactory = new NotificationContentDetailFactory(hubService.getGson(), hubService.getJsonParser());
        views.forEach(view -> {
            details.addAll(getDetailsFromSingleView(detailFactory, view.type, view.json));
        });

        final List<UriSingleResponse<? extends HubResponse>> uriResponseList = new ArrayList<>();
        notificationViewResults.forEach(notificationViewResult -> {
            uriResponseList.addAll(getAllLinks(notificationViewResult.getNotificationContentDetails()));
        });
        final HubBucket bucket = hubBucketService.startTheBucket(uriResponseList);

        // TODO lastNotificationDate/String
        return new NotificationDetailResults(details, , , bucket);
    }

    private List<NotificationContentDetail> getDetailsFromSingleView(final NotificationContentDetailFactory detailFactory, final NotificationType type, final String notificationJson) {
        return detailFactory.generateContentDetails(type, notificationJson);
    }

    private Request.Builder createNotificationRequestBuilder(final Date startDate, final Date endDate) {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        return RequestFactory.createCommonGetRequestBuilder().addQueryParameter("startDate", startDateString).addQueryParameter("endDate", endDateString);
    }

}
