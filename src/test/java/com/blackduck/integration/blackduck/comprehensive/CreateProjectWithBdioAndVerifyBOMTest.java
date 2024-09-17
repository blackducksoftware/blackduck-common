package com.blackduck.integration.blackduck.comprehensive;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.generated.view.CodeLocationView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionView;
import com.blackduck.integration.blackduck.api.generated.view.UserView;
import com.blackduck.integration.blackduck.api.manual.component.VersionBomCodeLocationBomComputedNotificationContent;
import com.blackduck.integration.blackduck.api.manual.temporary.enumeration.NotificationType;
import com.blackduck.integration.blackduck.api.manual.view.NotificationUserView;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationUserView;
import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationData;
import com.blackduck.integration.blackduck.codelocation.bdiolegacy.BdioUploadService;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatch;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.blackduck.integration.blackduck.codelocation.upload.UploadTarget;
import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.blackduck.integration.blackduck.service.request.NotificationEditor;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.LogLevel;
import com.blackduck.integration.log.PrintStreamIntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.RestConstants;
import com.blackduck.integration.util.NameVersion;
import com.blackduck.integration.wait.ResilientJobConfig;
import com.blackduck.integration.wait.WaitJob;
import com.blackduck.integration.wait.WaitJobCondition;
import com.blackduck.integration.wait.tracker.WaitIntervalTracker;
import com.blackduck.integration.wait.tracker.WaitIntervalTrackerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
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

    public static final List<String> CODE_LOCATION_NAMES = Arrays.asList(
        "blackduck-alert/6.1.0-SNAPSHOT/alert-common/blackduck-alert/alert-common/6.1.0-SNAPSHOT gradle/bom"
        , "blackduck-alert/6.1.0-SNAPSHOT/alert-database/blackduck-alert/alert-database/6.1.0-SNAPSHOT gradle/bom"
        , "blackduck-alert/6.1.0-SNAPSHOT/com.blackduck.integration/blackduck-alert/6.1.0-SNAPSHOT gradle/bom"
    );

    public static final String[] BDIO_FILE_NAMES = new String[] {
        "bdio/alert/blackduck_alert_6_1_0_SNAPSHOT_alert_common_blackduck_alert_alert_common_6_1_0_SNAPSHOT_gradle_bom.jsonld"
        , "bdio/alert/blackduck_alert_6_1_0_SNAPSHOT_alert_database_blackduck_alert_alert_database_6_1_0_SNAPSHOT_gradle_bom.jsonld"
        , "bdio/alert/blackduck_alert_6_1_0_SNAPSHOT_com_blackduck_integration_blackduck_alert_6_1_0_SNAPSHOT_gradle_bom.jsonld"
    };

    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);
    private final UserView currentUser = blackDuckServices.userService.findCurrentUser();
    private final IntLogger waitLogger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);
    WaitIntervalTracker waitIntervalTracker = WaitIntervalTrackerFactory.createConstant(TEN_MINUTES, THIRTY_SECONDS);
    private final ResilientJobConfig jobbyConfig = new ResilientJobConfig(waitLogger, ResilientJobConfig.CURRENT_TIME_SUPPLIER, waitIntervalTracker);
    private final Predicate<CodeLocationView> shouldDeleteCodeLocation = (codeLocationView -> CODE_LOCATION_NAMES.contains(codeLocationView.getName()));

    public CreateProjectWithBdioAndVerifyBOMTest() throws IntegrationException {
    }

    @BeforeEach
    public void setUp() throws IntegrationException {
        cleanBlackDuckTestElements();
    }

    @AfterEach
    public void tearDown() throws IntegrationException {
        cleanBlackDuckTestElements();
    }

    private void cleanBlackDuckTestElements() throws IntegrationException {
        Optional<ProjectView> projectView = blackDuckServices.projectService.getProjectByName(PROJECT_NAME);
        if (projectView.isPresent()) {
            blackDuckServices.blackDuckApiClient.delete(projectView.get());
        }

        List<CodeLocationView> codeLocationsToDelete = blackDuckServices.blackDuckApiClient.getSomeMatchingResponses(blackDuckServices.apiDiscovery.metaCodelocationsLink(), shouldDeleteCodeLocation, CODE_LOCATION_NAMES.size());
        for (CodeLocationView codeLocationToDelete : codeLocationsToDelete) {
            blackDuckServices.blackDuckApiClient.delete(codeLocationToDelete);
        }
    }

    @Test
    public void testCreatingProject() throws IntegrationException, InterruptedException {
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(createUploadTarget(CODE_LOCATION_NAMES.get(0), BDIO_FILE_NAMES[0]));
        uploadBatch.addUploadTarget(createUploadTarget(CODE_LOCATION_NAMES.get(1), BDIO_FILE_NAMES[1]));
        Set<String> expectedCodeLocationNames = getCodeLocationNames(uploadBatch);

        uploadAndVerifyBdio(uploadBatch, expectedCodeLocationNames);

        uploadBatch.addUploadTarget(createUploadTarget(CODE_LOCATION_NAMES.get(2), BDIO_FILE_NAMES[2]));
        expectedCodeLocationNames = getCodeLocationNames(uploadBatch);
        uploadAndVerifyBdio(uploadBatch, expectedCodeLocationNames);
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
        assertTrue(foundProject, "Project was not found");

        Optional<ProjectVersionWrapper> projectVersionWrapperOptional = blackDuckServices.projectService.getProjectVersion(PROJECT_NAME, PROJECT_VERSION_NAME);
        ProjectVersionView projectVersionView = projectVersionWrapperOptional.get().getProjectVersionView();

        boolean foundAllCodeLocations = waitForCodeLocations(expectedCodeLocationNames, projectVersionView);
        assertTrue(foundAllCodeLocations, "All code locations were not found");

        List<CodeLocationView> codeLocationViews = blackDuckServices.blackDuckApiClient.getAllResponses(projectVersionView.metaCodelocationsLink());
        Set<String> expectedCodeLocationUrls = codeLocationViews
                                                   .stream()
                                                   .map(CodeLocationView::getHref)
                                                   .map(HttpUrl::string)
                                                   .collect(Collectors.toSet());

        boolean foundAllCodeLocationUrls = waitForNotifications(startDate, endDate, expectedCodeLocationUrls);
        assertTrue(foundAllCodeLocationUrls, "All code location urls were not found");
    }

    private boolean waitForProject() throws InterruptedException, IntegrationException {
        WaitJobCondition findProjectTask = () -> blackDuckServices.projectService.getProjectVersion(PROJECT_NAME, PROJECT_VERSION_NAME).isPresent();
        return WaitJob.waitFor(jobbyConfig, findProjectTask, "wait for project");
    }

    private boolean waitForCodeLocations(Set<String> expectedCodeLocationNames, ProjectVersionView projectVersionView) throws InterruptedException, IntegrationException {
        Predicate<CodeLocationView> nameInSet = (codeLocationView) -> expectedCodeLocationNames.contains(codeLocationView.getName());
        WaitJobCondition findAllCodeLocationNames = () -> {
            List<CodeLocationView> codeLocationViews = blackDuckServices.blackDuckApiClient.getSomeMatchingResponses(projectVersionView.metaCodelocationsLink(), nameInSet, expectedCodeLocationNames.size());
            Set<String> foundCodeLocationNames = codeLocationViews
                                                     .stream()
                                                     .map(CodeLocationView::getName)
                                                     .collect(Collectors.toSet());

            printOutCodeLocations(codeLocationViews);

            return expectedCodeLocationNames.equals(foundCodeLocationNames);
        };
        return WaitJob.waitFor(jobbyConfig, findAllCodeLocationNames, "wait for code locations");
    }

    private void printOutCodeLocations(List<CodeLocationView> codeLocationViews) {
        System.out.println("found code location names:");
        codeLocationViews
            .stream()
            .map(codeLocationView -> String.format("%s (%s)", codeLocationView.getName(), codeLocationView.getHref()))
            .sorted()
            .forEach(System.out::println);
    }

    private boolean waitForNotifications(Date userStartDate, Date endDate, Set<String> expectedCodeLocationUrls) throws InterruptedException, IntegrationException {
        WaitJobCondition findNotificationsForAllCodeLocationUrls = () -> {
            NotificationEditor notificationEditor = new NotificationEditor(userStartDate, endDate, Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name()));
            List<NotificationUserView> filteredUserNotifications = blackDuckServices.notificationService
                                                                       .getAllUserNotifications(currentUser, notificationEditor);
            Set<String> foundCodeLocationUrls = filteredUserNotifications
                                                    .stream()
                                                    .map(notificationView -> (VersionBomCodeLocationBomComputedNotificationUserView) notificationView)
                                                    .map(VersionBomCodeLocationBomComputedNotificationUserView::getContent)
                                                    .map(VersionBomCodeLocationBomComputedNotificationContent::getCodeLocation)
                                                    .collect(Collectors.toSet());
            System.out.println("found code location urls:");
            foundCodeLocationUrls.forEach(System.out::println);

            return foundCodeLocationUrls.containsAll(expectedCodeLocationUrls);
        };
        return WaitJob.waitFor(jobbyConfig, findNotificationsForAllCodeLocationUrls, "wait for notifications");
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
