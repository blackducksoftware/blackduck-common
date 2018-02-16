package com.blackducksoftware.integration.hub.api.recipe

import static org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test

import com.blackducksoftware.integration.hub.api.enumeration.PolicyRuleConditionOperatorType
import com.blackducksoftware.integration.hub.api.generated.component.PolicyRuleExpressionSetView
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView
import com.blackducksoftware.integration.hub.api.view.MetaHandler
import com.blackducksoftware.integration.hub.bdio.model.Forge
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId
import com.blackducksoftware.integration.hub.service.ComponentService
import com.blackducksoftware.integration.hub.service.PolicyRuleService
import com.blackducksoftware.integration.hub.service.ProjectService
import com.blackducksoftware.integration.hub.service.model.PolicyRuleExpressionSetBuilder
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper

class CheckPolicyForProjectVersionRecipeTest extends BasicRecipe {
    ProjectVersionWrapper projectVersionWrapper;
    PolicyRuleViewV2 policyRule;

    @Before
    void setup() {
        ProjectRequest projectRequest = createProjectRequest(PROJECT_NAME, PROJECT_VERSION_NAME)
        ProjectService projectService = hubServicesFactory.createProjectService()

        /**
         * we can get the project and version like this, and if they don't
         * exist they will be created for us
         */
        projectVersionWrapper = projectService.getProjectVersionAndCreateIfNeeded(projectRequest)

        PolicyRuleService policyRuleService = hubServicesFactory.createPolicyRuleService()
        PolicyRuleViewV2 policyRuleViewV2 = constructTestPolicy(hubServicesFactory.createComponentService(), new MetaHandler(hubServicesFactory.getRestConnection().logger))
        policyRuleService.createPolicyRule(policyRuleViewV2)
    }


    @Test
    void testCheckingThePolicyForAProjectVersion() {
        //TODO finish test
    }

    @After
    void cleanup() {
        def projectService = hubServicesFactory.createProjectService()
        ProjectView createdProject = projectService.getProjectByName(PROJECT_NAME)
        projectService.deleteHubProject(createdProject)
    }

    private PolicyRuleViewV2 constructTestPolicy(ComponentService componentService, MetaHandler metaHandler) {
        ExternalId externalId = new ExternalId(Forge.MAVEN)
        externalId.group = "commons-fileupload"
        externalId.name = "commons-fileupload"
        externalId.version = "1.2.1"
        ComponentVersionView componentVersionView =  componentService.getExactComponentVersionFromComponent(externalId)

        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder(metaHandler)
        builder.addComponentCondition(PolicyRuleConditionOperatorType.EQ, componentVersionView)
        PolicyRuleExpressionSetView expressionSet = builder.createPolicyRuleExpressionSetView()

        PolicyRuleViewV2 policyRuleViewV2 = new PolicyRuleViewV2()
        policyRuleViewV2.name = 'Test Rule'
        policyRuleViewV2.enabled = true
        policyRuleViewV2.overridable = true
        policyRuleViewV2.expression = expressionSet
        policyRuleViewV2
    }
}
