package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.component.VersionBomCodeLocationBomComputedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationUserView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.NameVersion;

public class CodeLocationWaitJobTaskTest {
    public static final String CODE_LOCATION_URL = "http://www.disney.com";

    @Test
    public void testMultipleNotificationsExpected() throws ParseException, IntegrationException {
        BlackDuckApiClient mockBlackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        ProjectService mockProjectService = Mockito.mock(ProjectService.class);
        NotificationService mockNotificationService = Mockito.mock(NotificationService.class);

        UserView userView = new UserView();
        userView.setUserName("squiggles");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date alanTuringBirth = sdf.parse("1912-06-23");
        Date alanTuringUntimelyDeath = sdf.parse("1954-06-07");
        NotificationTaskRange notificationTaskRange = new NotificationTaskRange(System.currentTimeMillis(), alanTuringBirth, alanTuringUntimelyDeath);

        IntLogger logger = new BufferedIntLogger();
        NameVersion projectAndVersion = new NameVersion("BigSpoon", "LittleSpoon");
        String codeLocationName = "GraceIsButGloryBegunAndGloryIsButGracePerfected";
        Set<String> codeLocationNames = new HashSet<>(Arrays.asList(codeLocationName));

        CodeLocationWaitJobTask codeLocationWaitJobTask = new CodeLocationWaitJobTask(logger, mockBlackDuckApiClient, mockProjectService, mockNotificationService, userView, notificationTaskRange, projectAndVersion, codeLocationNames, 2);

        ProjectView projectView = new ProjectView();
        ProjectVersionView projectVersionView = new ProjectVersionView();
        ProjectVersionWrapper projectVersionWrapper = new ProjectVersionWrapper(projectView, projectVersionView);

        Mockito.when(mockProjectService.getProjectVersion(projectAndVersion)).thenReturn(Optional.of(projectVersionWrapper));

        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(new HttpUrl(CODE_LOCATION_URL));

        CodeLocationView foundCodeLocationView = new CodeLocationView();
        foundCodeLocationView.setName(codeLocationName);
        foundCodeLocationView.setMeta(resourceMetadata);

        Mockito.when(mockBlackDuckApiClient.getAllResponses(projectVersionView, ProjectVersionView.CODELOCATIONS_LINK_RESPONSE)).thenReturn(Arrays.asList(foundCodeLocationView));

        Mockito.when(mockNotificationService.getFilteredUserNotifications(userView, notificationTaskRange.getStartDate(), notificationTaskRange.getEndDate(), Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name())))
            .thenReturn(getExpectedNotifications());

        assertTrue(codeLocationWaitJobTask.isComplete());
    }

    private List<NotificationUserView> getExpectedNotifications() {
        return Arrays.asList(createNotification(CODE_LOCATION_URL), createNotification(CODE_LOCATION_URL));
    }

    private NotificationUserView createNotification(String codeLocationUrl) {
        VersionBomCodeLocationBomComputedNotificationContent content = new VersionBomCodeLocationBomComputedNotificationContent();
        content.setCodeLocation(codeLocationUrl);

        VersionBomCodeLocationBomComputedNotificationUserView view = new VersionBomCodeLocationBomComputedNotificationUserView();
        view.setContent(content);

        return view;
    }

}
