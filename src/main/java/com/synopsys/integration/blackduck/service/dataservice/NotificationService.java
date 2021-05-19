/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFilter;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.synopsys.integration.blackduck.service.request.NotificationEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class NotificationService extends DataService {
    // ejk - to get all notifications:
    // <blackduckserver>/api/notifications?startDate=2019-07-01T00:00:00.000Z&endDate=2019-07-15T00:00:00.000Z&filter=notificationType:BOM_EDIT&filter=notificationType:LICENSE_LIMIT&filter=notificationType:POLICY_OVERRIDE&filter=notificationType:RULE_VIOLATION&filter=notificationType:RULE_VIOLATION_CLEARED&filter=notificationType:VERSION_BOM_CODE_LOCATION_BOM_COMPUTED&filter=notificationType:VULNERABILITY&filter=notificationType:PROJECT&filter=notificationType:PROJECT_VERSION

    private final UrlMultipleResponses<NotificationView> notificationsResponses = apiDiscovery.metaMultipleResponses(ApiDiscovery.NOTIFICATIONS_PATH);
    private final Function<UserView, UrlMultipleResponses<NotificationUserView>> userNotificationsResponses = (userView) -> userView.metaNotificationsLink();

    public NotificationService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, blackDuckRequestBuilderFactory, logger);
    }

    public List<NotificationView> getAllNotifications(NotificationEditor notificationEditor) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createNotificationRequestBuilder(notificationEditor);
        BlackDuckMultipleRequest<NotificationView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(notificationsResponses);
        return blackDuckApiClient.getAllResponses(requestMultiple);
    }

    public List<NotificationUserView> getAllUserNotifications(UserView userView, NotificationEditor notificationEditor) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createNotificationRequestBuilder(notificationEditor);
        BlackDuckMultipleRequest<NotificationUserView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(userNotificationsResponses.apply(userView));
        return blackDuckApiClient.getAllResponses(requestMultiple);
    }

    public BlackDuckPageResponse<NotificationView> getPageOfNotifications(NotificationEditor notificationEditor, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createNotificationRequestBuilder(notificationEditor)
                                                              .setBlackDuckPageDefinition(blackDuckPageDefinition);
        BlackDuckMultipleRequest<NotificationView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(notificationsResponses);
        return blackDuckApiClient.getPageResponse(requestMultiple);
    }

    public BlackDuckPageResponse<NotificationUserView> getPageOfUserNotifications(UserView userView, NotificationEditor notificationEditor, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createNotificationRequestBuilder(notificationEditor)
                                                              .setBlackDuckPageDefinition(blackDuckPageDefinition);
        BlackDuckMultipleRequest<NotificationUserView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(userNotificationsResponses.apply(userView));
        return blackDuckApiClient.getPageResponse(requestMultiple);
    }

    /**
     * @return The java.util.Date of the most recent notification. If there are no notifications, the current date will be returned. This can set an initial start time window for all future notifications.
     * @throws IntegrationException
     */
    public Date getLatestNotificationDate() throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createLatestDateRequestBuilder();
        BlackDuckMultipleRequest<NotificationView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(notificationsResponses);
        List<NotificationView> notifications = blackDuckApiClient.getSomeResponses(requestMultiple, 1);
        return getFirstCreatedAtDate(notifications);
    }

    /**
     * @return The java.util.Date of the most recent notification in the user's stream. If there are no notifications, the current date will be returned. This can set an initial start time window for all future notifications.
     * @throws IntegrationException
     */
    public Date getLatestUserNotificationDate(UserView userView) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createLatestDateRequestBuilder();
        BlackDuckMultipleRequest<NotificationUserView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(userNotificationsResponses.apply(userView));
        List<NotificationUserView> userNotifications = blackDuckApiClient.getSomeResponses(requestMultiple, 1);
        return getFirstCreatedAtDate(userNotifications);
    }

    private Date getFirstCreatedAtDate(List<? extends NotificationView> notifications) {
        if (notifications.size() == 1) {
            return notifications.get(0).getCreatedAt();
        } else {
            return new Date();
        }
    }

    private BlackDuckRequestBuilder prepareUserNotificationsRequest(UserView user, Date startDate, Date endDate, List<String> notificationTypesToInclude) {
        HttpUrl url = user.getFirstLink(UserView.NOTIFICATIONS_LINK);
        return createNotificationRequestBuilder(startDate, endDate, notificationTypesToInclude)
                   .url(url);
    }

    private BlackDuckRequestBuilder createLatestDateRequestBuilder() {
        return blackDuckRequestBuilderFactory.createCommonGet(createFilterForAllKnownTypes());
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
        NotificationEditor notificationEditor = new NotificationEditor(startDate, endDate, notificationTypesToInclude);
        return createNotificationRequestBuilder(notificationEditor);
    }

    private BlackDuckRequestBuilder createNotificationRequestBuilder(NotificationEditor notificationEditor) {
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestBuilderFactory.createCommonGet();
        notificationEditor.edit(blackDuckRequestBuilder);
        return blackDuckRequestBuilder;
    }

    /*
    ejk - We can not trust the filtering from the Black Duck API. There have
    been at least 2 instances where the lack of filtering created customer
    issues, so we will do this in perpetuity.
    */
    private <T extends NotificationView> List<T> reallyFilterNotifications(List<T> notifications, List<String> notificationTypesToInclude) {
        return notifications
                   .stream()
                   .filter(notification -> notificationTypesToInclude.contains(notification.getType().name()))
                   .collect(Collectors.toList());
    }

}
