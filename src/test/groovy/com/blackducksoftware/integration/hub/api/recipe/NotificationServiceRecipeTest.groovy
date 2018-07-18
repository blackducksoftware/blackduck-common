package com.blackducksoftware.integration.hub.api.recipe

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

import org.junit.After
import org.junit.Test
import org.junit.experimental.categories.Category

import com.blackducksoftware.integration.exception.IntegrationException
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView
import com.blackducksoftware.integration.hub.notification.CommonNotificationView
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetailFactory
import com.blackducksoftware.integration.hub.service.CodeLocationService
import com.blackducksoftware.integration.hub.service.CommonNotificationService
import com.blackducksoftware.integration.hub.service.NotificationService
import com.blackducksoftware.integration.hub.service.ProjectService
import com.blackducksoftware.integration.hub.service.bucket.HubBucket
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService
import com.blackducksoftware.integration.hub.throwaway.NotificationContentDetailOld
import com.blackducksoftware.integration.hub.throwaway.NotificationDetailResultOld
import com.blackducksoftware.integration.hub.throwaway.NotificationDetailResultsOld
import com.blackducksoftware.integration.test.annotation.IntegrationTest

@Category(IntegrationTest.class)
class NotificationServiceRecipeTest extends BasicRecipe {
    private static final String NOTIFICATION_PROJECT_NAME = "hub-notification-data-test"
    private static final String NOTIFICATION_PROJECT_VERSION_NAME = "1.0.0"

    Date generateNotifications() {
        ProjectRequest projectRequest = createProjectRequest(NOTIFICATION_PROJECT_NAME, NOTIFICATION_PROJECT_VERSION_NAME)
        ProjectService projectService = hubServicesFactory.createProjectService()
        String projectUrl = projectService.createHubProject(projectRequest)
        ZonedDateTime startTime = ZonedDateTime.now()
        startTime = startTime.withZoneSameInstant(ZoneOffset.UTC)
        startTime = startTime.withSecond(0).withNano(0)
        startTime = startTime.minusMinutes(1)
        uploadBdio('bdio/clean_notifications_bdio.jsonld')
        Thread.sleep(5000)
        uploadBdio('bdio/generate_notifications_bdio.jsonld')
        List<VersionBomComponentView> components = Collections.emptyList()
        int tryCount = 0;
        while (components.empty || tryCount < 30) {
            Thread.sleep(1000);
            components = projectService.getComponentsForProjectVersion(NOTIFICATION_PROJECT_NAME, NOTIFICATION_PROJECT_VERSION_NAME)
            tryCount++
        }
        if (!components.empty) {
            Thread.sleep(60000) // arbitrary wait for notifications
        }
        return Date.from(startTime.toInstant())
    }

    void uploadBdio(final String bdioFile) throws IntegrationException, URISyntaxException, IOException {
        final File file = restConnectionTestHelper.getFile(bdioFile)
        final CodeLocationService service = hubServicesFactory.createCodeLocationService()
        service.importBomFile(file)
    }

    @Test
    void fetchNotificationsSynchronous() {
        final HubBucketService hubBucketService = hubServicesFactory.createHubBucketService()
        final NotificationService notificationService = hubServicesFactory.createNotificationService()

        final NotificationContentDetailFactory notificationContentDetailFactory = new NotificationContentDetailFactory(gson, jsonParser);
        final CommonNotificationService commonNotificationService = hubServicesFactory.createCommonNotificationService(notificationContentDetailFactory, true)

        processNotifications(hubBucketService, notificationService, commonNotificationService)
    }

    @Test
    void fetchNotificationsAsynchronous() {
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);

        final HubBucketService hubBucketService = hubServicesFactory.createHubBucketService(executorService)
        final NotificationService notificationService = hubServicesFactory.createNotificationService()

        final NotificationContentDetailFactory notificationContentDetailFactory = new NotificationContentDetailFactory(gson, jsonParser);
        final CommonNotificationService commonNotificationService = hubServicesFactory.createCommonNotificationService(notificationContentDetailFactory, true)

        processNotifications(hubBucketService, notificationService, commonNotificationService)
    }

    private void processNotifications(final HubBucketService hubBucketService, final NotificationService notificationService, CommonNotificationService commonNotificationService) {
        cleanup()
        final Date startDate = generateNotifications()
        ZonedDateTime endTime = ZonedDateTime.now()
        endTime = endTime.withZoneSameInstant(ZoneOffset.UTC)
        endTime = endTime.withSecond(0).withNano(0)
        endTime = endTime.plusMinutes(1)
        final Date endDate = Date.from(endTime.toInstant())

        List<NotificationView> notificationViews = notificationService.getAllNotifications(startDate, endDate)
        List<CommonNotificationView> commonNotificationViews = commonNotificationService.getCommonNotifications(notificationViews)
        final NotificationDetailResultsOld notificationDetailResults = commonNotificationService.getNotificationDetailResults(commonNotificationViews)

        final HubBucket hubBucket = new HubBucket();
        commonNotificationService.populateHubBucket(hubBucketService, hubBucket, notificationDetailResults);
        final List<NotificationDetailResultOld> notificationResultList = notificationDetailResults.getResults()

        Date latestNotificationEndDate = notificationDetailResults.getLatestNotificationCreatedAtDate().get();
        println("Start Date: ${startDate}, End Date: ${endDate}, latestNotification: ${latestNotificationEndDate}")

        notificationResultList.each({
            it.getNotificationContentDetails().each({
                NotificationContentDetailOld detail = it
                String contentDetailKey
                String projectName
                String projectVersion
                String componentName
                String componentVersion
                String policyName
                boolean isVulnerability = false
                contentDetailKey = detail.contentDetailKey
                projectName = detail.projectName.get()
                projectVersion = detail.projectVersionName.get()
                if (detail.hasComponentVersion()) {
                    componentName = detail.componentName.get()
                    componentVersion = detail.componentVersionName.get()
                }

                if (detail.hasOnlyComponent()) {
                    componentName = detail.componentName.get()
                }

                if (detail.isPolicy()) {
                    policyName = detail.policyName.get()
                }

                if (detail.isVulnerability()) {
                    isVulnerability = true
                }

                println("ContentDetailKey: ${contentDetailKey} ProjectName: ${projectName} Project Version: ${projectVersion} Component: ${componentName} Component Version: ${componentVersion} Policy: ${policyName} isVulnerability: ${isVulnerability}")
            })
        })
    }

    @After
    void cleanup() {
        try {
            def projectService = hubServicesFactory.createProjectService()
            ProjectView createdProject = projectService.getProjectByName(NOTIFICATION_PROJECT_NAME)
            projectService.deleteHubProject(createdProject)
        } catch (IntegrationException ex) {
            println("Could not delete project ${NOTIFICATION_PROJECT_NAME} cause: ${ex}")
        }
    }
}
