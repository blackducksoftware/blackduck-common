package com.synopsys.integration.blackduck.comprehensive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleCategoryType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.temporary.response.PolicySummaryView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.PolicyRuleService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectBomService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ProjectBomServiceTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final ExternalIdFactory externalIdFactory = new ExternalIdFactory();

    @Test
    public void testAddingComponentToBom() throws Exception {
        String projectName = "adding_component_test";
        String projectVersionName = "1.0.0";

        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        ProjectBomService projectBomService = blackDuckServicesFactory.createProjectBomService();
        IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the project, if it exists
        intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckApiClient, projectName);

        // create the project
        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, projectVersionName);
        ProjectVersionWrapper projectVersionWrapper = projectService.syncProjectAndVersion(projectSyncModel);

        // verify the bom
        List<ProjectVersionComponentVersionView> bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(0, bomComponents.size());

        ExternalId externalId = new ExternalIdFactory().createMavenExternalId("com.synopsys.integration", "blackduck-common", "47.0.0");
        projectBomService.addComponentToProjectVersion(externalId, projectVersionWrapper.getProjectVersionView());

        bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(1, bomComponents.size());
    }

    @Test
    public void testAddingProjectVersionToBom() throws Exception {
        String projectName = "adding_project_version_test";
        String projectVersionName = "1.0.0";

        String projectNameToAdd = "to_add_project";
        String projectVersionNameToAdd = "to_add_project_version";

        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        ProjectBomService projectBomService = blackDuckServicesFactory.createProjectBomService();
        IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the projects, if they exist
        intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckApiClient, projectName);

        // as this might have been previously added to a bom, it might still register as in use - try a few times
        int count = 0;
        boolean succeeded = false;
        while (count < 10 && !succeeded) {
            try {
                intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckApiClient, projectNameToAdd);
                succeeded = true;
            } catch (Exception ignored) {
                // ignored
            }
            count++;
        }

        // create the projects
        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, projectVersionName);
        ProjectVersionWrapper projectVersionWrapper = projectService.syncProjectAndVersion(projectSyncModel);

        ProjectSyncModel projectSyncModelToAdd = ProjectSyncModel.createWithDefaults(projectNameToAdd, projectVersionNameToAdd);
        ProjectVersionWrapper projectVersionWrapperToAdd = projectService.syncProjectAndVersion(projectSyncModelToAdd);

        // verify the boms
        List<ProjectVersionComponentVersionView> bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(0, bomComponents.size());

        projectBomService.addProjectVersionToProjectVersion(projectVersionWrapperToAdd.getProjectVersionView(), projectVersionWrapper.getProjectVersionView());

        bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(1, bomComponents.size());
    }

    @Test
    public void testGetActivePoliciesForVersion() throws Exception {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        ProjectBomService projectBomService = blackDuckServicesFactory.createProjectBomService();
        PolicyRuleService policyRuleService = blackDuckServicesFactory.createPolicyRuleService();
        IntLogger logger = blackDuckServicesFactory.getLogger();

        String projectName = "get_active_policies_test";
        String projectVersionName = "1.0.0";
        String testPolicyName = "testPolicy";
        String componentGroup = "com.synopsys.integration";
        String componentName = "blackduck-common";
        String componentVersion = "47.0.0";
        ExternalId componentExternalId = externalIdFactory.createMavenExternalId(componentGroup, componentName, componentVersion);

        // delete the project, if it exists
        intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckApiClient, projectName);

        // create the project
        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, projectVersionName);
        ProjectVersionWrapper projectVersionWrapper = projectService.syncProjectAndVersion(projectSyncModel);

        // verify the bom
        List<ProjectVersionComponentVersionView> bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(0, bomComponents.size());

        projectBomService.addComponentToProjectVersion(componentExternalId, projectVersionWrapper.getProjectVersionView());

        // get added component
        ProjectVersionView projectVersionView = projectVersionWrapper.getProjectVersionView();
        ProjectVersionComponentVersionView projectVersionComponentVersionView = blackDuckApiClient.getAllResponses(projectVersionView.metaComponentsLink()).stream().filter(component -> component.getComponentName().equals(componentName)).findFirst().orElse(null);

        //find correspondng ComponentVersionView
        HttpUrl projectVersionComponentUrl = new HttpUrl(projectVersionComponentVersionView.getComponentVersion());
        ComponentVersionView componentVersionView = blackDuckApiClient.getResponse(projectVersionComponentUrl, ComponentVersionView.class);

        // create policy rule that should be violated by that projectversion
        PolicyRuleView policyRule = createTestPolicyRuleForProjectWithComponentVersion(projectVersionWrapper.getProjectView(), componentVersionView, testPolicyName);
        Optional<PolicyRuleView> existingDuplicateRule = policyRuleService.getAllPolicyRules().stream()
            .filter(rule -> rule.getName().equals(testPolicyName))
            .findFirst();
        if (existingDuplicateRule.isPresent()) {
            blackDuckApiClient.delete(existingDuplicateRule.get());
            policyRuleService.createPolicyRule(policyRule);
        }

        // query projectBomService to see if projctversion has violated rule
        ProjectVersionView projectVersionViewWithRuleApplied = projectService.getProjectVersion(projectName, projectVersionName).orElse(null).getProjectVersionView();
        projectBomService.getActivePoliciesForVersion(projectVersionViewWithRuleApplied).ifPresent(policies -> {
            Assertions.assertTrue(policies.stream()
                .filter(rule -> ProjectVersionComponentPolicyStatusType.IN_VIOLATION.equals(rule.getStatus()))
                .map(PolicySummaryView::getName)
                .anyMatch(name -> name.equals(testPolicyName)));
        });
    }

    private PolicyRuleView createTestPolicyRuleForProjectWithComponentVersion(ProjectView projectView, ComponentVersionView componentVersion, String policyRuleName) throws BlackDuckIntegrationException {
        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder();
        builder.addProjectCondition(PolicyRuleConditionOperatorType.EQ, projectView);
        builder.addComponentVersionCondition(PolicyRuleConditionOperatorType.EQ, componentVersion);
        PolicyRuleExpressionView expressionSet = builder.createPolicyRuleExpressionView();

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setCategory(PolicyRuleCategoryType.COMPONENT);
        policyRuleView.setEnabled(true);
        policyRuleView.setName(policyRuleName);
        policyRuleView.setExpression(expressionSet);

        return policyRuleView;
    }

}
