package com.blackducksoftware.integration.hub.api.recipe

import static org.junit.Assert.assertFalse

import java.time.ZoneOffset
import java.time.ZonedDateTime

import org.junit.After
import org.junit.Test
import org.junit.experimental.categories.Category

import com.blackducksoftware.integration.exception.IntegrationException
import com.blackducksoftware.integration.hub.api.UriSingleResponse
import com.blackducksoftware.integration.hub.api.core.HubResponse
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView
import com.blackducksoftware.integration.hub.api.generated.view.IssueView
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState
import com.blackducksoftware.integration.hub.service.CodeLocationService
import com.blackducksoftware.integration.hub.service.NotificationService
import com.blackducksoftware.integration.hub.service.ProjectService
import com.blackducksoftware.integration.hub.service.bucket.HubBucket
import com.blackducksoftware.integration.hub.service.bucket.HubBucketItem
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService
import com.blackducksoftware.integration.test.annotation.IntegrationTest

@Category(IntegrationTest.class)
class NotificationServiceRecipeTest extends BasicRecipe {

    Date generateNotifications() {
        ProjectRequest projectRequest = createProjectRequest(PROJECT_NAME, PROJECT_VERSION_NAME)
        ProjectService projectService = hubServicesFactory.createProjectService()
        String projectUrl = projectService.createHubProject(projectRequest)
        ZonedDateTime startTime = ZonedDateTime.now()
        startTime = startTime.withZoneSameInstant(ZoneOffset.UTC)
        startTime = startTime.withSecond(0).withNano(0)
        startTime = startTime.minusMinutes(1)
        uploadBdio('bdio/clean_notifications_bdio.jsonld')
        uploadBdio('bdio/generate_notifications_bdio.jsonld')
        return Date.from(startTime.toInstant())
    }

    void uploadBdio(final String bdioFile) throws IntegrationException, URISyntaxException, IOException {
        final File file = restConnectionTestHelper.getFile(bdioFile)
        final CodeLocationService service = hubServicesFactory.createCodeLocationService()
        service.importBomFile(file)
        file.delete()
    }

    @Test
    void fetchNotifications() {
        final Date startDate = generateNotifications()
        Thread.sleep(60000)
        final NotificationService notificationService = hubServicesFactory.createNotificationService()
        final HubBucketService bucketService = hubServicesFactory.createHubBucketService()

        ZonedDateTime endTime = ZonedDateTime.now()
        endTime = endTime.withZoneSameInstant(ZoneOffset.UTC)
        endTime = endTime.withSecond(0).withNano(0)
        final Date endDate = Date.from(endTime.toInstant())
        final List<NotificationView> notifications = notificationService.getAllNotifications(startDate, endDate)
        final List<CommonNotificationState> commonNotificationList = notificationService.getCommonNotifications(notifications)
        final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses = notificationService.getAllLinks(commonNotificationList)

        final HubBucket bucket = bucketService.startTheBucket(uriSingleResponses)
        assertFalse(bucket.availableUris.empty)

        commonNotificationList.each({
            if(!it.content.providesLicenseDetails()) {
                ProjectVersionView projectVersion;
                ComponentView component;
                ComponentVersionView componentVersion;
                PolicyRuleView policy;
                IssueView componentIssue;
                it.content.notificationContentLinks.each({

                    if(it.hasComponentVersion()) {
                        HubBucketItem<ComponentVersionView> bucketItem = bucket.get(it.getComponentVersion().get().uri)
                        if(bucketItem.hasValidResponse()) {
                            componentVersion = bucketItem.hubResponse.get()
                        }
                    }
                    if(it.hasOnlyComponent()) {
                        HubBucketItem<ComponentView> bucketItem = bucket.get(it.getComponent().get().uri)
                        if(bucketItem.hasValidResponse()) {
                            component = bucketItem.hubResponse.get()
                        }
                    }
                    if(it.hasPolicy()) {
                        HubBucketItem<PolicyRuleView> bucketItem = bucket.get(it.getPolicy().get().uri)
                        if(bucketItem.hasValidResponse()) {
                            policy = bucketItem.hubResponse.get()
                        }
                    }
                    if(it.hasVulnerability()) {
                        HubBucketItem<IssueView> bucketItem = bucket.get(it.getComponentIssue().get().uri)
                        if(bucketItem.hasValidResponse()) {
                            componentIssue = bucketItem.hubResponse.get()
                        }
                    }
                })

                println("Component: ${component} Component Version: ${componentVersion} Policy ${policy} Issue: ${componentIssue}")
            }
        })
    }

    @After
    void cleanup() {
        def projectService = hubServicesFactory.createProjectService()
        ProjectView createdProject = projectService.getProjectByName(PROJECT_NAME)
        projectService.deleteHubProject(createdProject)
    }
}
