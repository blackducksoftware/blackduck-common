/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.contract.NotificationViewData;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.service.model.BlackDuckRequestFilter;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.request.Request;

public class NotificationService extends DataService {
    // ejk - to get all notifications:
    // <blackduckserver>/api/notifications?startDate=2019-07-01T00:00:00.000Z&endDate=2019-07-15T00:00:00.000Z&filter=notificationType:BOM_EDIT&notificationType:LICENSE_LIMIT&filter=notificationType:POLICY_OVERRIDE&filter=notificationType:RULE_VIOLATION&filter=notificationType:RULE_VIOLATION_CLEARED&filter=notificationType:VERSION_BOM_CODE_LOCATION_BOM_COMPUTED&filter=notificationType:VULNERABILITY&filter=notificationType:PROJECT&filter=notificationType:PROJECT_VERSION

    public NotificationService(BlackDuckService blackDuckService, IntLogger logger) {
        super(blackDuckService, logger);
    }

    public List<NotificationView> getAllNotifications(Date startDate, Date endDate) throws IntegrationException {
        List<String> allKnownNotificationTypes = getAllKnownNotificationTypes();
        Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate, allKnownNotificationTypes);
        List<NotificationView> allNotificationItems = blackDuckService.getResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, true);

        return allNotificationItems;
    }

    public List<NotificationUserView> getAllUserNotifications(UserView user, Date startDate, Date endDate) throws IntegrationException {
        List<String> allKnownNotificationTypes = getAllKnownNotificationTypes();
        Request.Builder requestBuilder = prepareUserNotificationsRequest(user, startDate, endDate, allKnownNotificationTypes);
        List<NotificationUserView> allUserNotificationItems = blackDuckService.getResponses(requestBuilder, NotificationUserView.class, true);

        return allUserNotificationItems;
    }

    public List<NotificationView> getFilteredNotifications(Date startDate, Date endDate, List<String> notificationTypesToInclude) throws IntegrationException {
        Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate, notificationTypesToInclude);
        List<NotificationView> allNotificationItems = blackDuckService.getResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, true);

        return reallyFilterNotifications(allNotificationItems, notificationTypesToInclude);
    }

    public List<NotificationUserView> getFilteredUserNotifications(UserView user, Date startDate, Date endDate, List<String> notificationTypesToInclude) throws IntegrationException {
        Request.Builder requestBuilder = prepareUserNotificationsRequest(user, startDate, endDate, notificationTypesToInclude);
        List<NotificationUserView> allUserNotificationItems = blackDuckService.getResponses(requestBuilder, NotificationUserView.class, true);

        return reallyFilterNotifications(allUserNotificationItems, notificationTypesToInclude);
    }

    /**
     * @return The java.util.Date of the most recent notification. If there are no notifications, the current date will be returned. This can set an initial start time window for all future notifications.
     * @throws IntegrationException
     */
    public Date getLatestNotificationDate() throws IntegrationException {
        Request.Builder requestBuilder = createLatestDateRequestBuilder();
        List<NotificationView> notifications = blackDuckService.getResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, false);
        return getFirstCreatedAtDate(notifications);
    }

    /**
     * @return The java.util.Date of the most recent notification in the user's stream. If there are no notifications, the current date will be returned. This can set an initial start time window for all future notifications.
     * @throws IntegrationException
     */
    public Date getLatestUserNotificationDate(UserView userView) throws IntegrationException {
        Request.Builder requestBuilder = createLatestDateRequestBuilder();
        List<NotificationUserView> userNotifications = blackDuckService.getResponses(userView, UserView.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, false);
        return getFirstCreatedAtDate(userNotifications);
    }

    private Date getFirstCreatedAtDate(List<? extends NotificationViewData> notifications) {
        if (notifications.size() == 1) {
            return notifications.get(0).getCreatedAt();
        } else {
            return new Date();
        }
    }

    private Request.Builder prepareUserNotificationsRequest(UserView user, Date startDate, Date endDate, List<String> notificationTypesToInclude) {
        Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate, notificationTypesToInclude);
        String userNotificationsUri = user.getFirstLink(UserView.NOTIFICATIONS_LINK).orElse(null);
        requestBuilder.uri(userNotificationsUri);

        return requestBuilder;
    }

    private Request.Builder createLatestDateRequestBuilder() {
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(1, RequestFactory.DEFAULT_OFFSET);
        RequestFactory.addBlackDuckFilter(requestBuilder, createFilterForAllKnownTypes());
        return requestBuilder;
    }

    private List<String> getAllKnownNotificationTypes() {
        List<String> allKnownTypes = Stream.of(NotificationType.values()).map(Enum::name).collect(Collectors.toList());
        return allKnownTypes;
    }

    private BlackDuckRequestFilter createFilterForAllKnownTypes() {
        return createFilterForSpecificTypes(getAllKnownNotificationTypes());
    }

    private BlackDuckRequestFilter createFilterForSpecificTypes(List<String> notificationTypesToInclude) {
        BlackDuckRequestFilter blackDuckRequestFilter = BlackDuckRequestFilter.createFilterWithMultipleValues("notificationType", notificationTypesToInclude);
        return blackDuckRequestFilter;
    }

    private Request.Builder createNotificationRequestBuilder(Date startDate, Date endDate, List<String> notificationTypesToInclude) {
        SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startDateString = sdf.format(startDate);
        String endDateString = sdf.format(endDate);

        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder().addQueryParameter("startDate", startDateString).addQueryParameter("endDate", endDateString);
        BlackDuckRequestFilter notificationTypeFilter = createFilterForSpecificTypes(notificationTypesToInclude);
        RequestFactory.addBlackDuckFilter(requestBuilder, notificationTypeFilter);
        return requestBuilder;
    }

    /*
    FIXME
    as of 2018.11.0, the notification filtering appears to be broken, so we must REALLY filter the notifications
    when that is fixed, this can be removed
    UPDATE: as of 2018.12.0, this is fixed - this can likely be removed when we no longer care about supporting flavors of 2018.11
    */
    private <T extends NotificationViewData> List<T> reallyFilterNotifications(List<T> notifications, List<String> notificationTypesToInclude) {
        return notifications
                       .stream()
                       .filter(notification -> notificationTypesToInclude.contains(notification.getType().name()))
                       .collect(Collectors.toList());
    }

}
