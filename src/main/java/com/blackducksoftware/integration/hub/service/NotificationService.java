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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationUserView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.service.model.HubFilter;
import com.blackducksoftware.integration.hub.service.model.RequestFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.RestConstants;
import com.blackducksoftware.integration.rest.request.Request;

public class NotificationService extends DataService {
    public NotificationService(final HubService hubService, final IntLogger logger) {
        super(hubService, logger);
    }

    public List<NotificationView> getAllNotifications(final Date startDate, final Date endDate) throws IntegrationException {
        final List<String> allKnownNotificationTypes = getAllKnownTypesToInclude();
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate, allKnownNotificationTypes);
        final List<NotificationView> allNotificationItems = hubService.getResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, true);
        return allNotificationItems;
    }

    public List<NotificationUserView> getAllUserNotifications(final UserView user, final Date startDate, final Date endDate) throws IntegrationException {
        final List<String> allKnownNotificationTypes = getAllKnownTypesToInclude();
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate, allKnownNotificationTypes);
        final String userNotificationsUri = hubService.getFirstLink(user, UserView.NOTIFICATIONS_LINK);
        requestBuilder.uri(userNotificationsUri);

        final List<NotificationUserView> allUserNotificationItems = hubService.getResponses(requestBuilder, NotificationUserView.class, true);
        return allUserNotificationItems;
    }

    public List<NotificationView> getFilteredNotifications(final Date startDate, final Date endDate, final List<String> notificationTypesToInclude) throws IntegrationException {
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate, notificationTypesToInclude);
        final List<NotificationView> allNotificationItems = hubService.getResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, true);
        return allNotificationItems;
    }

    public List<NotificationUserView> getFilteredUserNotifications(final UserView user, final Date startDate, final Date endDate, final List<String> notificationTypesToInclude) throws IntegrationException {
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate, notificationTypesToInclude);
        final String userNotificationsUri = hubService.getFirstLink(user, UserView.NOTIFICATIONS_LINK);
        requestBuilder.uri(userNotificationsUri);

        final List<NotificationUserView> allUserNotificationItems = hubService.getResponses(requestBuilder, NotificationUserView.class, true);
        return allUserNotificationItems;
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

    private Request.Builder createNotificationRequestBuilder(final Date startDate, final Date endDate, final List<String> notificationTypesToInclude) {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        final HubFilter hubFilter = HubFilter.createFilterWithMultipleValues("notificationType", notificationTypesToInclude);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder().addQueryParameter("startDate", startDateString).addQueryParameter("endDate", endDateString);
        RequestFactory.addHubFilter(requestBuilder, hubFilter);
        return requestBuilder;
    }

    private List<String> getAllKnownTypesToInclude() {
        return Arrays.stream(NotificationType.values()).map(NotificationType::name).collect(Collectors.toList());
    }

}
