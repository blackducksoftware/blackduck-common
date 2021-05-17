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

import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.component.VersionBomCodeLocationBomComputedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationUserView;
import com.synopsys.integration.blackduck.codelocation.CodeLocationWaitJobTask;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.blackduck.service.request.NotificationEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.NameVersion;

public class CodeLocationWaitJobTaskTest {
    private final HttpUrl codeLocationsUrl = new HttpUrl("https://synopsys.com/codelocations");
    private final UrlMultipleResponses<CodeLocationView> codeLocationResponses = new UrlMultipleResponses<>(codeLocationsUrl, CodeLocationView.class);
    private final HttpUrl codeLocationUrl = new HttpUrl("https://synopsys.com/codelocations/2.71828182845");

    public CodeLocationWaitJobTaskTest() throws IntegrationException {}

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

        ResourceLink resourceLink = new ResourceLink();
        resourceLink.setRel(ProjectVersionView.CODELOCATIONS_LINK);
        resourceLink.setHref(codeLocationsUrl);

        ResourceMetadata projectVersionViewMeta = new ResourceMetadata();
        projectVersionViewMeta.setLinks(Arrays.asList(resourceLink));

        ProjectVersionView projectVersionView = new ProjectVersionView();
        projectVersionView.setMeta(projectVersionViewMeta);

        ProjectVersionWrapper projectVersionWrapper = new ProjectVersionWrapper(projectView, projectVersionView);

        Mockito.when(mockProjectService.getProjectVersion(projectAndVersion)).thenReturn(Optional.of(projectVersionWrapper));

        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(codeLocationUrl);

        CodeLocationView foundCodeLocationView = new CodeLocationView();
        foundCodeLocationView.setName(codeLocationName);
        foundCodeLocationView.setMeta(resourceMetadata);

        Mockito.when(mockBlackDuckApiClient.getAllResponses(Mockito.eq(codeLocationResponses))).thenReturn(Arrays.asList(foundCodeLocationView));

        NotificationEditor notificationEditor = new NotificationEditor(notificationTaskRange.getStartDate(), notificationTaskRange.getEndDate(), Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name()));
        Mockito.when(mockNotificationService.getAllUserNotifications(userView, notificationEditor)).thenReturn(getExpectedNotifications());

        assertTrue(codeLocationWaitJobTask.isComplete());
    }

    private List<NotificationUserView> getExpectedNotifications() {
        return Arrays.asList(createNotification(codeLocationUrl), createNotification(codeLocationUrl));
    }

    private NotificationUserView createNotification(HttpUrl codeLocationUrl) {
        VersionBomCodeLocationBomComputedNotificationContent content = new VersionBomCodeLocationBomComputedNotificationContent();
        content.setCodeLocation(codeLocationUrl.string());

        VersionBomCodeLocationBomComputedNotificationUserView view = new VersionBomCodeLocationBomComputedNotificationUserView();
        view.setContent(content);

        return view;
    }

}
