package com.blackducksoftware.integration.hub.api.recipe

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category

import com.blackducksoftware.integration.hub.api.enumeration.PolicyRuleConditionOperatorType
import com.blackducksoftware.integration.hub.api.generated.component.PolicyRuleExpressionSetView
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest
import com.blackducksoftware.integration.hub.api.generated.enumeration.PolicyStatusSummaryStatusType
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomPolicyStatusView
import com.blackducksoftware.integration.hub.api.view.MetaHandler
import com.blackducksoftware.integration.hub.bdio.model.Forge
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId
import com.blackducksoftware.integration.hub.service.ComponentService
import com.blackducksoftware.integration.hub.service.PolicyRuleService
import com.blackducksoftware.integration.hub.service.ProjectService
import com.blackducksoftware.integration.hub.service.model.PolicyRuleExpressionSetBuilder
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper
import com.blackducksoftware.integration.test.annotation.IntegrationTest

@Category(IntegrationTest.class)
class CheckPolicyForProjectVersionRecipeTest extends BasicRecipe {
    ProjectVersionWrapper projectVersionWrapper
    PolicyRuleView policyRuleView
    PolicyRuleViewV2 policyRuleViewV2

    @Before
    void setup() {
        String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis()
        ProjectRequest projectRequest = createProjectRequest(uniqueProjectName, PROJECT_VERSION_NAME)
        ProjectService projectService = hubServicesFactory.createProjectService()

        /**
         * we can get the project and version like this, and if they don't
         * exist they will be created for us
         */
        projectVersionWrapper = projectService.getProjectVersionAndCreateIfNeeded(projectRequest)

        PolicyRuleService policyRuleService = hubServicesFactory.createPolicyRuleService()
        policyRuleViewV2 = constructTestPolicy(hubServicesFactory.createComponentService(), new MetaHandler(hubServicesFactory.getRestConnection().logger))

        /**
         * to create a Policy Rule we can construct a PolicyRuleViewV2 and Post it to the Hub
         */
        String policyRuleUrl = policyRuleService.createPolicyRule(policyRuleViewV2)
        policyRuleViewV2 = hubServicesFactory.createHubService().getResponse(policyRuleUrl, PolicyRuleViewV2.class)
    }

    @After
    void cleanup() {
        deleteProject(projectVersionWrapper.getProjectView().name)

        PolicyRuleService policyRuleService = hubServicesFactory.createPolicyRuleService()
        policyRuleService.deletePolicyRule(policyRuleViewV2)
    }


    @Test
    void testCheckingThePolicyForAProjectVersion() {
        ProjectService projectService = hubServicesFactory.createProjectService()

        ExternalId externalId = constructExternalId()

        /**
         * We add a new component to the Version that will violate our 'Test Rule'
         */
        projectService.addComponentToProjectVersion(externalId, projectVersionWrapper.getProjectVersionView())

        VersionBomPolicyStatusView policyStatus = projectService.getPolicyStatusForVersion(projectVersionWrapper.getProjectVersionView())
        Assert.assertEquals(PolicyStatusSummaryStatusType.IN_VIOLATION, policyStatus.overallStatus)
    }


    private PolicyRuleViewV2 constructTestPolicy(ComponentService componentService, MetaHandler metaHandler) {
        ExternalId externalId = constructExternalId()
        ComponentVersionView componentVersionView =  componentService.getComponentVersion(externalId)

        /**
         * using the PolicyRuleExpressionSetBuilder we can build the expression set for a PolicyRuleViewV2
         */
        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder(metaHandler)
        builder.addComponentVersionCondition(PolicyRuleConditionOperatorType.EQ, componentVersionView)
        PolicyRuleExpressionSetView expressionSet = builder.createPolicyRuleExpressionSetView()

        PolicyRuleViewV2 policyRuleViewV2 = new PolicyRuleViewV2()
        policyRuleViewV2.name = 'Test Rule' + System.currentTimeMillis()
        policyRuleViewV2.enabled = true
        policyRuleViewV2.overridable = true
        policyRuleViewV2.expression = expressionSet
        policyRuleViewV2
    }

    private ExternalId constructExternalId() {
        ExternalId externalId = new ExternalId(Forge.MAVEN)
        externalId.group = "commons-fileupload"
        externalId.name = "commons-fileupload"
        externalId.version = "1.2.1"
        externalId
    }
}
