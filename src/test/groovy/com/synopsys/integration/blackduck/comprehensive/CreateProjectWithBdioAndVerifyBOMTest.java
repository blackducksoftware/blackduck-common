package com.synopsys.integration.blackduck.comprehensive;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.component.VersionBomCodeLocationBomComputedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationUserView;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.bdioupload.BdioUploadService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.util.NameVersion;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobTask;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class CreateProjectWithBdioAndVerifyBOMTest {
    public static final String PROJECT_NAME = "blackduck-alert-junit";
    public static final String PROJECT_VERSION_NAME = "6.1.0-SNAPSHOT";

    public static final int FIVE_MINUTES = 5 * 60;
    public static final int TEN_MINUTES = FIVE_MINUTES * 2;
    public static final int THIRTY_SECONDS = 30;

    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);
    private final UserView currentUser = blackDuckServices.blackDuckService.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
    private final IntLogger waitLogger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);

    public static final String[] CODE_LOCATION_NAMES = new String[]{
            "blackduck-alert/6.1.0-SNAPSHOT/alert-common/blackduck-alert/alert-common/6.1.0-SNAPSHOT gradle/bom"
            , "blackduck-alert/6.1.0-SNAPSHOT/alert-database/blackduck-alert/alert-database/6.1.0-SNAPSHOT gradle/bom"
            , "blackduck-alert/6.1.0-SNAPSHOT/com.synopsys.integration/blackduck-alert/6.1.0-SNAPSHOT gradle/bom"
    };

    public static final String[] BDIO_FILE_NAMES = new String[]{
            "bdio/alert/blackduck_alert_6_1_0_SNAPSHOT_alert_common_blackduck_alert_alert_common_6_1_0_SNAPSHOT_gradle_bom.jsonld"
            , "bdio/alert/blackduck_alert_6_1_0_SNAPSHOT_alert_database_blackduck_alert_alert_database_6_1_0_SNAPSHOT_gradle_bom.jsonld"
            , "bdio/alert/blackduck_alert_6_1_0_SNAPSHOT_com_synopsys_integration_blackduck_alert_6_1_0_SNAPSHOT_gradle_bom.jsonld"
    };

    public CreateProjectWithBdioAndVerifyBOMTest() throws IntegrationException {
    }

    @Test
    public void testCreatingProject() throws IntegrationException, InterruptedException {
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(createUploadTarget(CODE_LOCATION_NAMES[0], BDIO_FILE_NAMES[0]));
        uploadBatch.addUploadTarget(createUploadTarget(CODE_LOCATION_NAMES[1], BDIO_FILE_NAMES[1]));
        Set<String> expectedCodeLocationNames = getCodeLocationNames(uploadBatch);

        uploadAndVerifyBdio(uploadBatch, expectedCodeLocationNames);

        uploadBatch.addUploadTarget(createUploadTarget(CODE_LOCATION_NAMES[2], BDIO_FILE_NAMES[2]));
        expectedCodeLocationNames = getCodeLocationNames(uploadBatch);
        uploadAndVerifyBdio(uploadBatch, expectedCodeLocationNames);

        ProjectView projectView = blackDuckServices.projectService.getProjectByName(PROJECT_NAME).get();
        blackDuckServices.blackDuckService.delete(projectView);

        for (String codeLocationName : CODE_LOCATION_NAMES) {
            CodeLocationView codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationName).get();
            blackDuckServices.blackDuckService.delete(codeLocationView);
        }
    }

    private void uploadAndVerifyBdio(UploadBatch uploadBatch, Set<String> expectedCodeLocationNames) throws IntegrationException, InterruptedException {
        Date startDate = blackDuckServices.notificationService.getLatestUserNotificationDate(currentUser);
        System.out.println("start date: ");
        System.out.println(RestConstants.formatDate(startDate));

        BdioUploadService bdioUploadService = blackDuckServices.blackDuckServicesFactory.createBdioUploadService();
        CodeLocationCreationData<UploadBatchOutput> creationData = bdioUploadService.uploadBdio(uploadBatch);
        Date endDate = Date.from(new Date().toInstant().plus(7, ChronoUnit.DAYS));

        assertCodeLocationsAddedToBOM(expectedCodeLocationNames, startDate, endDate);
    }

    private void assertCodeLocationsAddedToBOM(Set<String> expectedCodeLocationNames, Date startDate, Date endDate) throws InterruptedException, IntegrationException {
        boolean foundProject = waitForProject();
        assertTrue(foundProject);

        Optional<ProjectVersionWrapper> projectVersionWrapperOptional = blackDuckServices.projectService.getProjectVersion(PROJECT_NAME, PROJECT_VERSION_NAME);
        ProjectVersionView projectVersionView = projectVersionWrapperOptional.get().getProjectVersionView();

        boolean foundAllCodeLocations = waitForCodeLocations(expectedCodeLocationNames, projectVersionView);
        assertTrue(foundAllCodeLocations);

        List<CodeLocationView> codeLocationViews = blackDuckServices.blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        Set<String> expectedCodeLocationUrls = codeLocationViews
                .stream()
                .map(CodeLocationView::getHref)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(HttpUrl::string)
                .collect(Collectors.toSet());

        boolean foundAllCodeLocationUrls = waitForNotifications(startDate, endDate, expectedCodeLocationUrls);
        assertTrue(foundAllCodeLocationUrls);
    }

    private boolean waitForProject() throws InterruptedException, IntegrationException {
        WaitJobTask findProjectTask = () -> blackDuckServices.projectService.getProjectVersion(PROJECT_NAME, PROJECT_VERSION_NAME).isPresent();
        return WaitJob.createUsingSystemTimeWhenInvoked(waitLogger, FIVE_MINUTES, THIRTY_SECONDS, "wait for project", findProjectTask).waitFor();
    }

    private boolean waitForCodeLocations(Set<String> expectedCodeLocationNames, ProjectVersionView projectVersionView) throws InterruptedException, IntegrationException {
        WaitJobTask findAllCodeLocationNames = () -> {
            List<CodeLocationView> codeLocationViews = blackDuckServices.blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
            Set<String> foundCodeLocationNames = codeLocationViews
                    .stream()
                    .map(CodeLocationView::getName)
                    .collect(Collectors.toSet());

            System.out.println("found code location names:");
            codeLocationViews
                    .stream()
                    .map(codeLocationView -> String.format("%s (%s)", codeLocationView.getName(), codeLocationView.getHref().get()))
                    .sorted()
                    .forEach(System.out::println);

            return expectedCodeLocationNames.equals(foundCodeLocationNames);
        };
        return WaitJob.createUsingSystemTimeWhenInvoked(waitLogger, TEN_MINUTES, THIRTY_SECONDS, "wait for code locations", findAllCodeLocationNames).waitFor();
    }

    private boolean waitForNotifications(Date userStartDate, Date endDate, Set<String> expectedCodeLocationUrls) throws InterruptedException, IntegrationException {
        WaitJobTask findNotificationsForAllCodeLocationUrls = () -> {
            List<NotificationUserView> filteredUserNotifications = blackDuckServices.notificationService.getFilteredUserNotifications(currentUser, userStartDate, endDate, Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name()));
            Set<String> foundCodeLocationUrls = filteredUserNotifications
                    .stream()
                    .map(notificationView -> (VersionBomCodeLocationBomComputedNotificationUserView) notificationView)
                    .map(VersionBomCodeLocationBomComputedNotificationUserView::getContent)
                    .map(VersionBomCodeLocationBomComputedNotificationContent::getCodeLocation)
                    .collect(Collectors.toSet());
            System.out.println("found code location urls:");
            foundCodeLocationUrls.forEach(System.out::println);

            return expectedCodeLocationUrls.equals(foundCodeLocationUrls);
        };
        return WaitJob.createUsingSystemTimeWhenInvoked(waitLogger, TEN_MINUTES, THIRTY_SECONDS, "wait for notifications", findNotificationsForAllCodeLocationUrls).waitFor();
    }

    private UploadTarget createUploadTarget(String codeLocationName, String bdioFilename) {
        File bdioFile = intHttpClientTestHelper.getFile(bdioFilename);
        return UploadTarget.createDefault(new NameVersion(PROJECT_NAME, PROJECT_VERSION_NAME), codeLocationName, bdioFile);
    }

    private Set<String> getCodeLocationNames(UploadBatch uploadBatch) {
        return uploadBatch.getUploadTargets()
                .stream()
                .map(UploadTarget::getCodeLocationName)
                .collect(Collectors.toSet());
    }

}
