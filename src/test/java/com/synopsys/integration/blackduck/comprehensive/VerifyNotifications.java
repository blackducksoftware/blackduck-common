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
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationView;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.request.NotificationEditor;
import com.synopsys.integration.exception.IntegrationException;

public class VerifyNotifications {
    public static void verify(UserView currentUser, NotificationService notificationService, Date userStartDate, Date systemStartDate) throws IntegrationException {
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

        assertTrue(allNotifications.containsAll(bomComputedNotifications));
        assertTrue(allUserNotifications.containsAll(bomComputedUserNotifications));
        assertEquals(getContents(bomComputedNotifications), getContents(bomComputedUserNotifications));
    }

    private static List<VersionBomCodeLocationBomComputedNotificationContent> getContents(List<? extends NotificationView<VersionBomCodeLocationBomComputedNotificationContent>> notifications) {
        return notifications
                   .stream()
                   .map(NotificationView::getContent)
                   .collect(Collectors.toList());
    }

}
