/**
 * blackduck-common
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
package com.synopsys.integration.blackduck.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.generated.view.NotificationView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.service.model.BlackDuckRequestFilter;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.request.Request;

public class NotificationService extends DataService {
    public NotificationService(final BlackDuckService blackDuckService, final IntLogger logger) {
        super(blackDuckService, logger);
    }

    public List<NotificationView> getAllNotifications(final Date startDate, final Date endDate) throws IntegrationException {
        final List<String> allKnownNotificationTypes = getAllKnownNotificationTypes();
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate, allKnownNotificationTypes);
        final List<NotificationView> allNotificationItems = blackDuckService.getResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, true);
        return allNotificationItems;
    }

    public List<NotificationUserView> getAllUserNotifications(final UserView user, final Date startDate, final Date endDate) throws IntegrationException {
        final List<String> allKnownNotificationTypes = getAllKnownNotificationTypes();
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate, allKnownNotificationTypes);
        final String userNotificationsUri = user.getFirstLink(UserView.NOTIFICATIONS_LINK).orElse(null);
        requestBuilder.uri(userNotificationsUri);

        final List<NotificationUserView> allUserNotificationItems = blackDuckService.getResponses(requestBuilder, NotificationUserView.class, true);
        return allUserNotificationItems;
    }

    public List<NotificationView> getFilteredNotifications(final Date startDate, final Date endDate, final List<String> notificationTypesToInclude) throws IntegrationException {
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate, notificationTypesToInclude);
        final List<NotificationView> allNotificationItems = blackDuckService.getResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, true);
        return reallyFilterNotifications(allNotificationItems, notificationTypesToInclude);
    }

    public List<NotificationUserView> getFilteredUserNotifications(final UserView user, final Date startDate, final Date endDate, final List<String> notificationTypesToInclude) throws IntegrationException {
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate, notificationTypesToInclude);
        final String userNotificationsUri = user.getFirstLink(UserView.NOTIFICATIONS_LINK).orElse(null);
        requestBuilder.uri(userNotificationsUri);

        final List<NotificationUserView> allUserNotificationItems = blackDuckService.getResponses(requestBuilder, NotificationUserView.class, true);
        // return
        return reallyFilterNotifications(allUserNotificationItems, notificationTypesToInclude);
    }

    /**
     * @return The java.util.Date of the most recent notification. If there are no notifications, the current date will be returned. This can set an initial start time window for all future notifications.
     * @throws IntegrationException
     */
    public Date getLatestNotificationDate() throws IntegrationException {
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(1, RequestFactory.DEFAULT_OFFSET);
        RequestFactory.addBlackDuckFilter(requestBuilder, createFilterForAllKnownTypes());
        final List<NotificationView> notifications = blackDuckService.getResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, false);
        if (notifications.size() == 1) {
            return notifications.get(0).getCreatedAt();
        } else {
            return new Date();
        }
    }

    private List<String> getAllKnownNotificationTypes() {
        final List<String> allKnownTypes = Stream.of(NotificationType.values()).map(Enum::name).collect(Collectors.toList());
        return allKnownTypes;
    }

    private BlackDuckRequestFilter createFilterForAllKnownTypes() {
        return createFilterForSpecificTypes(getAllKnownNotificationTypes());
    }

    private BlackDuckRequestFilter createFilterForSpecificTypes(final List<String> notificationTypesToInclude) {
        final BlackDuckRequestFilter blackDuckRequestFilter = BlackDuckRequestFilter.createFilterWithMultipleValues("notificationType", notificationTypesToInclude);
        return blackDuckRequestFilter;
    }

    private Request.Builder createNotificationRequestBuilder(final Date startDate, final Date endDate, final List<String> notificationTypesToInclude) {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder().addQueryParameter("startDate", startDateString).addQueryParameter("endDate", endDateString);
        final BlackDuckRequestFilter notificationTypeFilter = createFilterForSpecificTypes(notificationTypesToInclude);
        RequestFactory.addBlackDuckFilter(requestBuilder, notificationTypeFilter);
        return requestBuilder;
    }

    /*
    FIXME
    as of 2018.11.0, the notification filtering appears to be broken, so we must REALLY filter the notifications
    when that is fixed, this can be removed
    */
    private <T extends BlackDuckView> List<T> reallyFilterNotifications(final List<T> notifications, final List<String> notificationTypesToInclude) {
        return notifications
                       .stream()
                       .filter(notification -> {
                           String notificationType = JsonPath.read(notification.getJson(), "$.type");
                           return notificationTypesToInclude.contains(notificationType);
                       })
                       .collect(Collectors.toList());
    }

}
