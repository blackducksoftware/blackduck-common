/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFilter;
import com.synopsys.integration.rest.RestConstants;

public class NotificationRequestFactory {
    private final List<String> ALL_NOTIFICATION_TYPES = Stream.of(NotificationType.values()).map(Enum::name).collect(Collectors.toList());

    private final BlackDuckRequestFactory blackDuckRequestFactory;

    public NotificationRequestFactory(BlackDuckRequestFactory blackDuckRequestFactory) {
        this.blackDuckRequestFactory = blackDuckRequestFactory;
    }

    public BlackDuckApiRequestSpec<NotificationView> createRequestSpecForAllNotificationTypes(Date startDate, Date endDate) {
        return createRequestSpecForAllNotificationTypes(startDate, endDate, ALL_NOTIFICATION_TYPES);
    }

    public BlackDuckApiRequestSpec<NotificationView> createRequestSpecForAllNotificationTypes(Date startDate, Date endDate, List<String> notificationTypesToInclude) {
        BlackDuckRequestBuilder requestBuilder = createRequestBuilderForNotificationTypes(startDate, endDate, notificationTypesToInclude);
        return new BlackDuckApiRequestSpec<>(ApiDiscovery.NOTIFICATIONS_LINK, NotificationView.class, requestBuilder);
    }

    private BlackDuckRequestBuilder createRequestBuilderForNotificationTypes(Date startDate, Date endDate, List<String> notificationTypesToInclude) {
        SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startDateString = sdf.format(startDate);
        String endDateString = sdf.format(endDate);

        BlackDuckRequestFilter notificationTypeFilter = createFilterForNotificationsTypes(notificationTypesToInclude);
        return blackDuckRequestFactory
                   .createCommonGetRequestBuilder()
                   .addQueryParameter("startDate", startDateString)
                   .addQueryParameter("endDate", endDateString)
                   .addBlackDuckFilter(notificationTypeFilter);
    }

    private BlackDuckRequestFilter createFilterForNotificationsTypes(List<String> notificationTypesToInclude) {
        return BlackDuckRequestFilter.createFilterWithMultipleValues("notificationType", notificationTypesToInclude);
    }

}
