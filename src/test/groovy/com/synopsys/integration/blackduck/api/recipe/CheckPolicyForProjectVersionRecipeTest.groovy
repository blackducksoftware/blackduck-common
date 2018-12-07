package com.synopsys.integration.blackduck.api.recipe

import com.synopsys.integration.bdio.model.Forge
import com.synopsys.integration.bdio.model.externalid.ExternalId
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyStatusView
import com.synopsys.integration.blackduck.service.ComponentService
import com.synopsys.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

@Tag("integration")
class CheckPolicyForProjectVersionRecipeTest extends BasicRecipe {
    ProjectVersionWrapper projectVersionWrapper
    PolicyRuleViewV2 policyRuleViewV2

    @BeforeEach
    void setup() {
        String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis()
        ProjectRequest projectRequest = createProjectRequest(uniqueProjectName, PROJECT_VERSION_NAME)

        /*
         * We can get the project and version like this, and if they don't
         * exist they will be created for us.
         */
        projectVersionWrapper = projectService.syncProjectAndVersion(projectRequest, false)

        policyRuleViewV2 = constructTestPolicy(blackDuckServicesFactory.createComponentService())

        /*
         * To post a Policy Rule we can construct a PolicyRuleViewV2 and post it to Black Duck.
         */
        String policyRuleUrl = policyRuleService.createPolicyRule(policyRuleViewV2)
        policyRuleViewV2 = blackDuckService.getResponse(policyRuleUrl, PolicyRuleViewV2.class)
    }

    @AfterEach
    void cleanup() {
        deleteProject(projectVersionWrapper.getProjectView())
        blackDuckService.delete(policyRuleViewV2)
    }

    @Test
    void testCheckingThePolicyForAProjectVersion() {
        ExternalId externalId = constructExternalId()

        /*
         * We add a new component to the Version that will violate our 'Test Rule'
         */
        projectService.addComponentToProjectVersion(externalId, projectVersionWrapper.getProjectVersionView())

        VersionBomPolicyStatusView policyStatus = projectService.getPolicyStatusForVersion(projectVersionWrapper.getProjectVersionView()).get()
        assertEquals(PolicySummaryStatusType.IN_VIOLATION, policyStatus.overallStatus)
    }

    private PolicyRuleViewV2 constructTestPolicy(ComponentService componentService) {
        ExternalId externalId = constructExternalId()
        ComponentVersionView componentVersionView = componentService.getComponentVersion(externalId).get()

        /**
         * using the PolicyRuleExpressionSetBuilder we can build the expression set for a PolicyRuleViewV2*/
        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder()
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
