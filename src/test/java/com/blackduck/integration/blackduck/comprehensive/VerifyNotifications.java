package com.blackduck.integration.blackduck.comprehensive;

import com.blackduck.integration.blackduck.api.generated.view.UserView;
import com.blackduck.integration.blackduck.api.manual.component.VersionBomCodeLocationBomComputedNotificationContent;
import com.blackduck.integration.blackduck.api.manual.temporary.enumeration.NotificationType;
import com.blackduck.integration.blackduck.api.manual.view.NotificationUserView;
import com.blackduck.integration.blackduck.api.manual.view.NotificationView;
import com.blackduck.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationUserView;
import com.blackduck.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationView;
import com.blackduck.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.blackduck.integration.blackduck.service.dataservice.NotificationService;
import com.blackduck.integration.blackduck.service.request.NotificationEditor;
import com.blackduck.integration.exception.IntegrationException;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class VerifyNotifications {
    public static void verify(UserView currentUser, BlackDuckRegistrationService blackDuckRegistrationService, NotificationService notificationService, Date userStartDate, Date systemStartDate) throws IntegrationException {
        Date endDate = Date.from(new Date().toInstant().plus(7, ChronoUnit.DAYS));

        NotificationEditor systemDateEditor = new NotificationEditor(systemStartDate, endDate);
        NotificationEditor userDateEditor = new NotificationEditor(userStartDate, endDate);

        List<String> bomComputedFilter = Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name());
        NotificationEditor systemDateFilteredEditor = new NotificationEditor(systemStartDate, endDate, bomComputedFilter);
        NotificationEditor userDateFilteredEditor = new NotificationEditor(userStartDate, endDate, bomComputedFilter);

        List<NotificationView> allNotifications = notificationService.getAllNotifications(systemDateEditor);
        List<NotificationUserView> allUserNotifications = notificationService.getAllUserNotifications(currentUser, userDateEditor);

        List<NotificationView> filteredNotifications = notificationService.getAllNotifications(systemDateFilteredEditor);
        List<NotificationUserView> filteredUserNotifications = notificationService.getAllUserNotifications(currentUser, userDateFilteredEditor);

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

        // ejk - BD has a known bug in 2020.10.0 where version bom computed is
        // NOT included when asking for all
        /*
         * ejk 2021-07-16
         * TODO should this be in VersionSupport?
         */
        String version = blackDuckRegistrationService.getBlackDuckServerData().getVersion();
        if (!"2020.10.0".equals(version)) {
            assertTrue(allNotifications.containsAll(bomComputedNotifications));
            assertTrue(allUserNotifications.containsAll(bomComputedUserNotifications));
        }
        assertEquals(getContents(bomComputedNotifications), getContents(bomComputedUserNotifications));
    }

    private static List<VersionBomCodeLocationBomComputedNotificationContent> getContents(List<? extends NotificationView<VersionBomCodeLocationBomComputedNotificationContent>> notifications) {
        return notifications
                   .stream()
                   .map(NotificationView::getContent)
                   .collect(Collectors.toList());
    }

}
