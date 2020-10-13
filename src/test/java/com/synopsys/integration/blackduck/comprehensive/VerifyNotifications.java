package com.synopsys.integration.blackduck.comprehensive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.component.VersionBomCodeLocationBomComputedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.contract.NotificationContentData;
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationView;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.exception.IntegrationException;

public class VerifyNotifications {
    public static void verify(UserView currentUser, NotificationService notificationService, Date userStartDate, Date systemStartDate) throws IntegrationException {
        Date endDate = Date.from(new Date().toInstant().plus(7, ChronoUnit.DAYS));

        List<NotificationView> allNotifications = notificationService.getAllNotifications(systemStartDate, endDate);
        List<NotificationUserView> allUserNotifications = notificationService.getAllUserNotifications(currentUser, userStartDate, endDate);
        List<NotificationView> filteredNotifications = notificationService.getFilteredNotifications(systemStartDate, endDate, Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name()));
        List<NotificationUserView> filteredUserNotifications = notificationService.getFilteredUserNotifications(currentUser, userStartDate, endDate, Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name()));

        assertFalse(allNotifications.isEmpty());
        assertFalse(allUserNotifications.isEmpty());
        assertFalse(filteredNotifications.isEmpty());
        assertFalse(filteredUserNotifications.isEmpty());

        List<VersionBomCodeLocationBomComputedNotificationView> bomComputedNotifications =
            filteredNotifications
                .stream()
                .map(notificationView -> (VersionBomCodeLocationBomComputedNotificationView) notificationView)
                .collect(Collectors.toList());

        List<VersionBomCodeLocationBomComputedNotificationUserView> bomComputedUserNotifications =
            filteredUserNotifications
                .stream()
                .map(notificationView -> (VersionBomCodeLocationBomComputedNotificationUserView) notificationView)
                .collect(Collectors.toList());

        assertTrue(allNotifications.containsAll(bomComputedNotifications));
        assertTrue(allUserNotifications.containsAll(bomComputedUserNotifications));
        assertEquals(getContents(bomComputedNotifications), getContents(bomComputedUserNotifications));
    }

    private static List<VersionBomCodeLocationBomComputedNotificationContent> getContents(List<? extends NotificationContentData<VersionBomCodeLocationBomComputedNotificationContent>> notifications) {
        return notifications
                   .stream()
                   .map(NotificationContentData::getContent)
                   .collect(Collectors.toList());
    }

}
