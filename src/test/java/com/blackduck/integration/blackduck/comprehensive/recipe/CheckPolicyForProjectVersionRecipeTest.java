package com.blackduck.integration.blackduck.comprehensive.recipe;

import com.blackduck.integration.bdio.model.Forge;
import com.blackduck.integration.bdio.model.externalid.ExternalId;
import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.blackduck.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.blackduck.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.blackduck.integration.blackduck.api.generated.response.ComponentsView;
import com.blackduck.integration.blackduck.api.generated.view.ComponentVersionView;
import com.blackduck.integration.blackduck.api.generated.view.PolicyRuleView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionPolicyStatusView;
import com.blackduck.integration.blackduck.service.dataservice.ComponentService;
import com.blackduck.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder;
import com.blackduck.integration.blackduck.service.model.ProjectSyncModel;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class CheckPolicyForProjectVersionRecipeTest extends BasicRecipe {
    private ProjectVersionWrapper projectVersionWrapper;
    private PolicyRuleView policyRuleView;

    @BeforeEach
    public void setup() throws IntegrationException {
        String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis();
        ProjectSyncModel projectSyncModel = createProjectSyncModel(uniqueProjectName, PROJECT_VERSION_NAME);

        /*
         * We can get the project and version like this, and if they don't
         * exist they will be created for us.
         */
        projectVersionWrapper = projectService.syncProjectAndVersion(projectSyncModel, false);

        policyRuleView = constructTestPolicy(blackDuckServicesFactory.createComponentService());

        /*
         * To create a Policy Rule we can construct a PolicyRuleView and create
         * it in Black Duck.
         */
        HttpUrl policyRuleUrl = policyRuleService.createPolicyRule(policyRuleView);
        policyRuleView = blackDuckApiClient.getResponse(policyRuleUrl, PolicyRuleView.class);
    }

    @AfterEach
    public void cleanup() throws IntegrationException {
        deleteProject(projectVersionWrapper.getProjectView());
        blackDuckApiClient.delete(policyRuleView);
    }

    @Test
    public void testCheckingThePolicyForAProjectVersion() throws IntegrationException {
        ExternalId externalId = constructExternalId();

        /*
         * We add a new component to the Version that will violate our 'Test
         * Rule'
         */
        projectBomService.addComponentToProjectVersion(externalId, projectVersionWrapper.getProjectVersionView());

        ProjectVersionPolicyStatusView policyStatus = projectBomService.getPolicyStatusForVersion(projectVersionWrapper.getProjectVersionView()).get();
        Assertions.assertEquals(ProjectVersionComponentPolicyStatusType.IN_VIOLATION, policyStatus.getOverallStatus());
    }

    private PolicyRuleView constructTestPolicy(ComponentService componentService) throws IntegrationException {
        ExternalId externalId = constructExternalId();
        Optional<ComponentsView> searchResult = componentService.getSingleOrEmptyResult(externalId);
        ComponentVersionView componentVersionView = componentService.getComponentVersionView(searchResult.get()).get();

        /*
         * using the PolicyRuleExpressionSetBuilder we can build the expression
         * set for a PolicyRuleView
         */
        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder();
        builder.addComponentVersionCondition(PolicyRuleConditionOperatorType.EQ, componentVersionView);
        PolicyRuleExpressionView expressionSet = builder.createPolicyRuleExpressionView();

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setName("Test Rule" + System.currentTimeMillis());
        policyRuleView.setEnabled(true);
        policyRuleView.setOverridable(true);
        policyRuleView.setExpression(expressionSet);
        return policyRuleView;
    }

    private ExternalId constructExternalId() {
        ExternalId externalId = new ExternalId(Forge.MAVEN);
        externalId.setGroup("commons-fileupload");
        externalId.setName("commons-fileupload");
        externalId.setVersion("1.2.1");
        return externalId;
    }

    public ProjectVersionWrapper getProjectVersionWrapper() {
        return projectVersionWrapper;
    }

    public void setProjectVersionWrapper(ProjectVersionWrapper projectVersionWrapper) {
        this.projectVersionWrapper = projectVersionWrapper;
    }

    public PolicyRuleView getPolicyRuleView() {
        return policyRuleView;
    }

    public void setPolicyRuleView(PolicyRuleView policyRuleView) {
        this.policyRuleView = policyRuleView;
    }

}
