package com.synopsys.integration.blackduck.api.recipe

import com.synopsys.integration.bdio.model.Forge
import com.synopsys.integration.bdio.model.externalid.ExternalId
import com.synopsys.integration.blackduck.TimingExtension
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyStatusType
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionPolicyStatusView
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.view.ComponentSearchResultView
import com.synopsys.integration.blackduck.service.ComponentService
import com.synopsys.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper
import com.synopsys.integration.rest.HttpUrl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static org.junit.jupiter.api.Assertions.assertEquals

@Tag("integration")
@ExtendWith(TimingExtension.class)
class CheckPolicyForProjectVersionRecipeTest extends BasicRecipe {
    ProjectVersionWrapper projectVersionWrapper
    PolicyRuleView policyRuleView

    @BeforeEach
    void setup() {
        String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis()
        ProjectSyncModel projectSyncModel = createProjectSyncModel(uniqueProjectName, PROJECT_VERSION_NAME)

        /*
         * We can get the project and version like this, and if they don't
         * exist they will be created for us.
         */
        projectVersionWrapper = projectService.syncProjectAndVersion(projectSyncModel, false)

        policyRuleView = constructTestPolicy(blackDuckServicesFactory.createComponentService())

        /*
         * To create a Policy Rule we can construct a PolicyRuleView and create it to Black Duck.
         */
        HttpUrl policyRuleUrl = policyRuleService.createPolicyRule(policyRuleView)
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
        projectBomService.addComponentToProjectVersion(externalId, projectVersionWrapper.getProjectVersionView())

        ProjectVersionPolicyStatusView policyStatus = projectBomService.getPolicyStatusForVersion(projectVersionWrapper.getProjectVersionView()).get()
        assertEquals(PolicyStatusType.IN_VIOLATION, policyStatus.overallStatus)
    }

    private PolicyRuleView constructTestPolicy(ComponentService componentService) {
        ExternalId externalId = constructExternalId()
        Optional<ComponentSearchResultView> searchResult = componentService.getSingleOrEmptyResult(externalId)
        ComponentVersionView componentVersionView = componentService.getComponentVersionView(searchResult.get()).get()

        /**
         * using the PolicyRuleExpressionSetBuilder we can build the expression set for a PolicyRuleView*/
        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder()
        builder.addComponentVersionCondition(PolicyRuleConditionOperatorType.EQ, componentVersionView)
        PolicyRuleExpressionView expressionSet = builder.createPolicyRuleExpressionView()

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
