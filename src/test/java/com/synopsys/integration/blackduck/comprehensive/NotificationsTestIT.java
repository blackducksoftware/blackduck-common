package com.synopsys.integration.blackduck.comprehensive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.synopsys.integration.blackduck.http.client.TestingPropertyKey;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.component.ProjectNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectVersionNotificationUserView;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.dataservice.UserService;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.blackduck.service.request.NotificationEditor;
import com.synopsys.integration.exception.IntegrationException;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class NotificationsTestIT {
    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    @Test
    public void testProjectNotifications() throws IntegrationException, InterruptedException {
        IntLogger logger = intHttpClientTestHelper.createIntLogger(intHttpClientTestHelper.getTestLogLevel());
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory(logger);

        String projectName = "notifications_test_" + System.currentTimeMillis(); // TOME also fails to clean up
        String projectVersionName = "notifications_test_version_" + System.currentTimeMillis();
        String projectVersion2Name = "notifications_test_version2_" + System.currentTimeMillis();

        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        NotificationService notificationService = blackDuckServicesFactory.createNotificationService();
        UserService userService = blackDuckServicesFactory.createUserService();

        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, projectVersionName);
        ProjectSyncModel projectSyncModel2 = ProjectSyncModel.createWithDefaults(projectName, projectVersion2Name);

        UserView currentUser = userService.findCurrentUser();
        Date startDate = notificationService.getLatestUserNotificationDate(currentUser);
        Date endDate = Date.from(startDate.toInstant().plus(1, ChronoUnit.DAYS));
        List<String> notificationTypes = new ArrayList<>();
        notificationTypes.add(NotificationType.PROJECT.name());
        notificationTypes.add(NotificationType.PROJECT_VERSION.name());

        // CREATE
        ProjectVersionWrapper projectVersionWrapper = projectService.syncProjectAndVersion(projectSyncModel); // TOME did this succeed? prob not b/c it already exists?
        logger.info("created project version 1");
        ProjectVersionWrapper projectVersionWrapper2 = projectService.syncProjectAndVersion(projectSyncModel2, true);

        // DELETE
        blackDuckApiClient.delete(projectVersionWrapper2.getProjectVersionView()); // TOME looks to be successful, v#2 on BD
        blackDuckApiClient.delete(projectVersionWrapper.getProjectView()); // TOME deleting #1

        // two project version create
        // one project version delete, one project delete
        Set<String> expectedKeys = new HashSet(Arrays.asList("CREATE" + projectVersionName, "CREATE" + projectVersion2Name, "DELETE" + projectName, "DELETE" + projectVersion2Name));

        Set<String> foundKeys = new HashSet<>();
        long start = System.currentTimeMillis();
        long duration = 0;

        while ((foundKeys.size() < expectedKeys.size()) && duration < FIVE_MINUTES) {
            NotificationEditor notificationEditor = new NotificationEditor(startDate, endDate, notificationTypes);
            List<NotificationUserView> notifications = notificationService.getAllUserNotifications(currentUser, notificationEditor);
            for (NotificationUserView notificationUserView : notifications) {
                if (notificationUserView instanceof ProjectNotificationUserView) {
                    ProjectNotificationContent content = ((ProjectNotificationUserView) notificationUserView).getContent();
                    if (projectName.equals(content.getProjectName())) {
                        foundKeys.add(content.getOperationType() + content.getProjectName());
                    }
                } else if (notificationUserView instanceof ProjectVersionNotificationUserView) {
                    ProjectVersionNotificationContent content = ((ProjectVersionNotificationUserView) notificationUserView).getContent();
                    if (projectName.equals(content.getProjectName())) {
                        foundKeys.add(content.getOperationType() + content.getProjectVersionName());
                    }
                }
            }
            Thread.sleep(2000);
            duration = System.currentTimeMillis() - start;
        }

        assertEquals(expectedKeys, foundKeys);
    }

}
