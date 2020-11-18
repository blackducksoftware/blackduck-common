/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service.dataservice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.contract.NotificationViewData;
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFilter;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.RestConstants;

public class NotificationService extends DataService {
    // ejk - to get all notifications:
    // <blackduckserver>/api/notifications?startDate=2019-07-01T00:00:00.000Z&endDate=2019-07-15T00:00:00.000Z&filter=notificationType:BOM_EDIT&filter=notificationType:LICENSE_LIMIT&filter=notificationType:POLICY_OVERRIDE&filter=notificationType:RULE_VIOLATION&filter=notificationType:RULE_VIOLATION_CLEARED&filter=notificationType:VERSION_BOM_CODE_LOCATION_BOM_COMPUTED&filter=notificationType:VULNERABILITY&filter=notificationType:PROJECT&filter=notificationType:PROJECT_VERSION

    public NotificationService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public List<NotificationView> getAllNotifications(Date startDate, Date endDate) throws IntegrationException {
        List<String> allKnownNotificationTypes = getAllKnownNotificationTypes();
        BlackDuckRequestBuilder requestBuilder = createNotificationRequestBuilder(startDate, endDate, allKnownNotificationTypes);
        List<NotificationView> allNotificationItems = blackDuckApiClient.getAllResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder);

        return allNotificationItems;
    }

    public List<NotificationUserView> getAllUserNotifications(UserView user, Date startDate, Date endDate) throws IntegrationException {
        List<String> allKnownNotificationTypes = getAllKnownNotificationTypes();
        BlackDuckRequestBuilder requestBuilder = prepareUserNotificationsRequest(user, startDate, endDate, allKnownNotificationTypes);
        List<NotificationUserView> allUserNotificationItems = blackDuckApiClient.getAllResponses(requestBuilder, NotificationUserView.class);

        return allUserNotificationItems;
    }

    public List<NotificationView> getFilteredNotifications(Date startDate, Date endDate, List<String> notificationTypesToInclude) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = createNotificationRequestBuilder(startDate, endDate, notificationTypesToInclude);
        List<NotificationView> allNotificationItems = blackDuckApiClient.getAllResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder);

        return reallyFilterNotifications(allNotificationItems, notificationTypesToInclude);
    }

    public List<NotificationUserView> getFilteredUserNotifications(UserView user, Date startDate, Date endDate, List<String> notificationTypesToInclude) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = prepareUserNotificationsRequest(user, startDate, endDate, notificationTypesToInclude);
        List<NotificationUserView> allUserNotificationItems = blackDuckApiClient.getAllResponses(requestBuilder, NotificationUserView.class);

        return reallyFilterNotifications(allUserNotificationItems, notificationTypesToInclude);
    }

    /**
     * @return The java.util.Date of the most recent notification. If there are no notifications, the current date will be returned. This can set an initial start time window for all future notifications.
     * @throws IntegrationException
     */
    public Date getLatestNotificationDate() throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = createLatestDateRequestBuilder();
        List<NotificationView> notifications = blackDuckApiClient.getSomeResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, 1);
        return getFirstCreatedAtDate(notifications);
    }

    /**
     * @return The java.util.Date of the most recent notification in the user's stream. If there are no notifications, the current date will be returned. This can set an initial start time window for all future notifications.
     * @throws IntegrationException
     */
    public Date getLatestUserNotificationDate(UserView userView) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = createLatestDateRequestBuilder();
        List<NotificationUserView> userNotifications = blackDuckApiClient.getSomeResponses(userView, UserView.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, 1);
        return getFirstCreatedAtDate(userNotifications);
    }

    private Date getFirstCreatedAtDate(List<? extends NotificationViewData> notifications) {
        if (notifications.size() == 1) {
            return notifications.get(0).getCreatedAt();
        } else {
            return new Date();
        }
    }

    private BlackDuckRequestBuilder prepareUserNotificationsRequest(UserView user, Date startDate, Date endDate, List<String> notificationTypesToInclude) throws IntegrationException {
        HttpUrl url = user.getFirstLink(UserView.NOTIFICATIONS_LINK);
        return createNotificationRequestBuilder(startDate, endDate, notificationTypesToInclude)
                   .url(url);
    }

    private BlackDuckRequestBuilder createLatestDateRequestBuilder() {
        return blackDuckRequestFactory
                   .createCommonGetRequestBuilder()
                   .addBlackDuckFilter(createFilterForAllKnownTypes());
    }

    private List<String> getAllKnownNotificationTypes() {
        List<String> allKnownTypes = Stream.of(NotificationType.values()).map(Enum::name).collect(Collectors.toList());
        return allKnownTypes;
    }

    private BlackDuckRequestFilter createFilterForAllKnownTypes() {
        return createFilterForSpecificTypes(getAllKnownNotificationTypes());
    }

    private BlackDuckRequestFilter createFilterForSpecificTypes(List<String> notificationTypesToInclude) {
        return BlackDuckRequestFilter.createFilterWithMultipleValues("notificationType", notificationTypesToInclude);
    }

    private BlackDuckRequestBuilder createNotificationRequestBuilder(Date startDate, Date endDate, List<String> notificationTypesToInclude) {
        SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startDateString = sdf.format(startDate);
        String endDateString = sdf.format(endDate);

        BlackDuckRequestFilter notificationTypeFilter = createFilterForSpecificTypes(notificationTypesToInclude);
        return blackDuckRequestFactory
                   .createCommonGetRequestBuilder()
                   .addQueryParameter("startDate", startDateString)
                   .addQueryParameter("endDate", endDateString)
                   .addBlackDuckFilter(notificationTypeFilter);
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
