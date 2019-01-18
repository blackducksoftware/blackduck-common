package com.synopsys.integration.blackduck.api.recipe

import com.synopsys.integration.bdio.model.Forge
import com.synopsys.integration.bdio.model.externalid.ExternalId
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView
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
    PolicyRuleView policyRuleView

    @BeforeEach
    void setup() {
        String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis()
        ProjectRequest projectRequest = createProjectRequest(uniqueProjectName, PROJECT_VERSION_NAME)

        /*
         * We can get the project and version like this, and if they don't
         * exist they will be created for us.
         */
        projectVersionWrapper = projectService.syncProjectAndVersion(projectRequest, false)

        policyRuleView = constructTestPolicy(blackDuckServicesFactory.createComponentService())

        /*
         * To create a Policy Rule we can construct a PolicyRuleView and create it to Black Duck.
         */
        String policyRuleUrl = policyRuleService.createPolicyRule(policyRuleView)
        policyRuleView = blackDuckService.getResponse(policyRuleUrl, PolicyRuleView.class)
    }

    @AfterEach
    void cleanup() {
        deleteProject(projectVersionWrapper.getProjectView())
        blackDuckService.delete(policyRuleView)
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

    private PolicyRuleView constructTestPolicy(ComponentService componentService) {
        ExternalId externalId = constructExternalId()
        ComponentVersionView componentVersionView = componentService.getComponentVersion(externalId).get()

        /**
         * using the PolicyRuleExpressionSetBuilder we can build the expression set for a PolicyRuleView*/
        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder()
        builder.addComponentVersionCondition(PolicyRuleConditionOperatorType.EQ, componentVersionView)
        PolicyRuleExpressionSetView expressionSet = builder.createPolicyRuleExpressionSetView()

        PolicyRuleView policyRuleView = new PolicyRuleView()
        policyRuleView.name = 'Test Rule' + System.currentTimeMillis()
        policyRuleView.enabled = true
        policyRuleView.overridable = true
        policyRuleView.expression = expressionSet
        policyRuleView
    }

    private ExternalId constructExternalId() {
        ExternalId externalId = new ExternalId(Forge.MAVEN)
        externalId.group = "commons-fileupload"
        externalId.name = "commons-fileupload"
        externalId.version = "1.2.1"
        externalId
    }

}
