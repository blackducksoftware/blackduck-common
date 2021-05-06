package com.synopsys.integration.blackduck.service.request;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFilter;
import com.synopsys.integration.rest.RestConstants;

public class NotificationEditor implements BlackDuckRequestBuilderEditor {
    @Override
    public void edit(BlackDuckRequestBuilder blackDuckRequestBuilder) {

    }
    private BlackDuckRequestBuilder createRequestBuilderForNotificationTypes(Date startDate, Date endDate, List<String> notificationTypesToInclude) {
        SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startDateString = sdf.format(startDate);
        String endDateString = sdf.format(endDate);

        BlackDuckRequestFilter notificationTypeFilter = createFilterForNotificationsTypes(notificationTypesToInclude);
        return blackDuckRequestBuilderFactory.createCommonGet(notificationTypeFilter)
                   .addQueryParameter("startDate", startDateString)
                   .addQueryParameter("endDate", endDateString);
    }

    private BlackDuckRequestFilter createFilterForNotificationsTypes(List<String> notificationTypesToInclude) {
        return BlackDuckRequestFilter.createFilterWithMultipleValues("notificationType", notificationTypesToInclude);
    }

}
