/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.NotificationType;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFilter;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.util.Stringable;

public class NotificationEditor extends Stringable implements BlackDuckRequestBuilderEditor {
    public static final List<String> ALL_NOTIFICATION_TYPES = Stream.of(NotificationType.values()).map(Enum::name).collect(Collectors.toList());

    private final Date startDate;
    private final Date endDate;
    private final List<String> notificationTypesToInclude;

    public NotificationEditor(OffsetDateTime start, OffsetDateTime end) {
        this(Date.from(start.toInstant()), Date.from(end.toInstant()));
    }

    public NotificationEditor(OffsetDateTime start, OffsetDateTime end, List<String> notificationTypesToInclude) {
        this(Date.from(start.toInstant()), Date.from(end.toInstant()), notificationTypesToInclude);
    }

    public NotificationEditor(Date startDate, Date endDate) {
        this(startDate, endDate, ALL_NOTIFICATION_TYPES);
    }

    public NotificationEditor(Date startDate, Date endDate, List<String> notificationTypesToInclude) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.notificationTypesToInclude = notificationTypesToInclude;
    }

    @Override
    public void edit(BlackDuckRequestBuilder blackDuckRequestBuilder) {
        SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startDateString = sdf.format(startDate);
        String endDateString = sdf.format(endDate);

        BlackDuckRequestFilter notificationTypeFilter = createFilterForNotificationsTypes(notificationTypesToInclude);
        blackDuckRequestBuilder
            .addQueryParameter("startDate", startDateString)
            .addQueryParameter("endDate", endDateString)
            .addBlackDuckFilter(notificationTypeFilter);
    }

    public List<String> getNotificationTypesToInclude() {
        return notificationTypesToInclude;
    }

    private BlackDuckRequestFilter createFilterForNotificationsTypes(List<String> notificationTypesToInclude) {
        return BlackDuckRequestFilter.createFilterWithMultipleValues("notificationType", notificationTypesToInclude);
    }

}
