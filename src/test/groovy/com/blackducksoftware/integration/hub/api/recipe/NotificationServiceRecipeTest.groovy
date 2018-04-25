package com.blackducksoftware.integration.hub.api.recipe

import static org.junit.Assert.assertFalse

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
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState
import com.blackducksoftware.integration.hub.notification.NotificationResults
import com.blackducksoftware.integration.hub.service.CodeLocationService
import com.blackducksoftware.integration.hub.service.NotificationService
import com.blackducksoftware.integration.hub.service.ProjectService
import com.blackducksoftware.integration.hub.service.bucket.HubBucket
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService
import com.blackducksoftware.integration.test.annotation.IntegrationTest

@Category(IntegrationTest.class)
class NotificationServiceRecipeTest extends BasicRecipe {

    private static final String NOTIFICATION_PROJECT_NAME = "hub-notification-data-test"

    Date generateNotifications() {
        ProjectRequest projectRequest = createProjectRequest(NOTIFICATION_PROJECT_NAME, PROJECT_VERSION_NAME)
        ProjectService projectService = hubServicesFactory.createProjectService()
        String projectUrl = projectService.createHubProject(projectRequest)
        ZonedDateTime startTime = ZonedDateTime.now()
        startTime = startTime.withZoneSameInstant(ZoneOffset.UTC)
        startTime = startTime.withSecond(0).withNano(0)
        startTime = startTime.minusMinutes(1)
        uploadBdio('bdio/clean_notifications_bdio.jsonld')
        Thread.sleep(5000)
        uploadBdio('bdio/generate_notifications_bdio.jsonld')
        return Date.from(startTime.toInstant())
    }

    void uploadBdio(final String bdioFile) throws IntegrationException, URISyntaxException, IOException {
        final File file = restConnectionTestHelper.getFile(bdioFile)
        final CodeLocationService service = hubServicesFactory.createCodeLocationService()
        service.importBomFile(file)
    }

    @Test
    void fetchNotificationsSynchronous() {
        final Date startDate = generateNotifications()
        Thread.sleep(25000)
        final NotificationService notificationService = hubServicesFactory.createNotificationService()
        final HubBucketService bucketService = hubServicesFactory.createHubBucketService()

        ZonedDateTime endTime = ZonedDateTime.now()
        endTime = endTime.withZoneSameInstant(ZoneOffset.UTC)
        endTime = endTime.withSecond(0).withNano(0)
        endTime = endTime.plusMinutes(1)
        final Date endDate = Date.from(endTime.toInstant())
        final NotificationResults results = notificationService.getAllNotificationResults(startDate, endDate)
        final List<CommonNotificationState> commonNotificationList = results.getNotificationContentItems()

        final HubBucket bucket = results.getHubBucket()
        assertFalse(bucket.availableUris.empty)

        commonNotificationList.each({
            if(!it.content.providesLicenseDetails()) {
                String projectName
                String projectVersion
                String componentName
                String componentVersion
                String policyName
                boolean isVulnerability = false
                it.content.notificationContentDetails.each({
                    projectName = it.projectName
                    projectVersion = it.projectVersionName
                    if(it.hasComponentVersion()) {
                        componentVersion = it.componentVersionName.get()
                    }
                    if(it.hasOnlyComponent()) {
                        componentName = it.componentName.get()
                    }
                    if(it.isPolicy()) {
                        policyName = it.policyName.get()
                    }
                    if(it.isVulnerability()) {
                        isVulnerability = true
                    }
                })

                println("ProjectName: ${projectName} Project Version: ${projectVersion} Component: ${componentName} Component Version: ${componentVersion} Policy: ${policyName} isVulnerability: ${isVulnerability}")
            }
        })
    }

    @Test
    void fetchNotificationsAsynchronous() {
        final Date startDate = generateNotifications()
        Thread.sleep(25000)
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
        final NotificationService notificationService = hubServicesFactory.createNotificationService(executorService)

        ZonedDateTime endTime = ZonedDateTime.now()
        endTime = endTime.withZoneSameInstant(ZoneOffset.UTC)
        endTime = endTime.withSecond(0).withNano(0)
        endTime = endTime.plusMinutes(1)
        final Date endDate = Date.from(endTime.toInstant())
        final NotificationResults results = notificationService.getAllNotificationResults(startDate,endDate)
        final List<CommonNotificationState> commonNotificationList = results.getNotificationContentItems()

        final HubBucket bucket = results.getHubBucket()
        assertFalse(bucket.availableUris.empty)

        commonNotificationList.each({
            if(!it.content.providesLicenseDetails()) {
                String projectName
                String projectVersion
                String componentName
                String componentVersion
                String policyName
                boolean isVulnerability = false
                it.content.notificationContentDetails.each({
                    projectName = it.projectName
                    projectVersion = it.projectVersionName
                    if(it.hasComponentVersion()) {
                        componentVersion = it.componentVersionName.get()
                    }
                    if(it.hasOnlyComponent()) {
                        componentName = it.componentName.get()
                    }
                    if(it.isPolicy()) {
                        policyName = it.policyName.get()
                    }
                    if(it.isVulnerability()) {
                        isVulnerability = true
                    }
                })

                println("ProjectName: ${projectName} Project Version: ${projectVersion} Component: ${componentName} Component Version: ${componentVersion} Policy: ${policyName} isVulnerability: ${isVulnerability}")
            }
        })
    }

    @After
    void cleanup() {
        def projectService = hubServicesFactory.createProjectService()
        ProjectView createdProject = projectService.getProjectByName(NOTIFICATION_PROJECT_NAME)
        projectService.deleteHubProject(createdProject)
    }
}
