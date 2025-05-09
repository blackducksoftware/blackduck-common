package com.blackduck.integration.blackduck.comprehensive;

import com.blackduck.integration.bdio.model.externalid.ExternalId;
import com.blackduck.integration.bdio.model.externalid.ExternalIdFactory;
import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.blackduck.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.blackduck.integration.blackduck.api.generated.enumeration.PolicyRuleCategoryType;
import com.blackduck.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.blackduck.integration.blackduck.api.generated.view.*;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.dataservice.PolicyRuleService;
import com.blackduck.integration.blackduck.service.dataservice.ProjectBomService;
import com.blackduck.integration.blackduck.service.dataservice.ProjectService;
import com.blackduck.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder;
import com.blackduck.integration.blackduck.service.model.ProjectSyncModel;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import org.junit.Assume;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        ExternalId externalId = new ExternalIdFactory().createMavenExternalId("com.blackducksoftware.integration", "blackduck-common", "45.0.7");
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
        String componentGroup = "com.blackducksoftware.integration";
        String componentName = "blackduck-common";
        String componentVersion = "45.0.7";
        ExternalId componentExternalId = externalIdFactory.createMavenExternalId(componentGroup, componentName, componentVersion);

        // delete the project, if it exists
        intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckApiClient, projectName);

        // create the project
        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, projectVersionName);
        ProjectVersionWrapper projectVersionWrapper = projectService.syncProjectAndVersion(projectSyncModel);

        // check for presence of active-policy-rules link for project version (if not present then this is an older/incompatible BlackDuck, test should abort)
        try {
            projectVersionWrapper.getProjectVersionView().metaActivePolicyRulesLink();
        } catch (NoSuchElementException e) {
            Assume.assumeNoException(e); // skip test if exception is thrown
        }

        // verify the bom
        List<ProjectVersionComponentVersionView> bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(0, bomComponents.size());

        projectBomService.addComponentToProjectVersion(componentExternalId, projectVersionWrapper.getProjectVersionView());

        // get added component
        ProjectVersionView projectVersionView = projectVersionWrapper.getProjectVersionView();
        ProjectVersionComponentVersionView projectVersionComponentVersionView = blackDuckApiClient.getAllResponses(projectVersionView.metaComponentsLink()).stream()
            .filter(component -> component.getComponentName().equals(componentName)).findFirst().orElse(null);

        //find corresponding ComponentVersionView
        HttpUrl projectVersionComponentUrl = new HttpUrl(projectVersionComponentVersionView.getComponentVersion());
        ComponentVersionView componentVersionView = blackDuckApiClient.getResponse(projectVersionComponentUrl, ComponentVersionView.class);

        // create policy rule that should be violated by that projectversion
        PolicyRuleView policyRule = createTestPolicyRuleForProjectWithComponentVersion(projectVersionWrapper.getProjectView(), componentVersionView, testPolicyName);
        Optional<PolicyRuleView> existingDuplicateRule = policyRuleService.getAllPolicyRules().stream()
            .filter(rule -> rule.getName().equals(testPolicyName))
            .findFirst();
        if (existingDuplicateRule.isPresent()) {
            blackDuckApiClient.delete(existingDuplicateRule.get());
        }
        policyRuleService.createPolicyRule(policyRule);

        Thread.sleep(60000); // need this to give Black Duck enough time to check the project version against the policy rule

        // query projectBomService to see if project version has violated rule
        Optional<List<ProjectVersionPolicyRulesView>> activePolicies = projectBomService.getActivePoliciesForVersion(projectVersionView);
        Assertions.assertTrue(activePolicies.isPresent());

        System.out.println("Active Policies Present: " + activePolicies.isPresent());
        activePolicies.ifPresent(policies -> System.out.println("Policies Size: " + policies.size()));

        Assertions.assertFalse(activePolicies.get().isEmpty());

        Assertions.assertTrue(activePolicies.get().stream()
            .filter(rule -> ProjectVersionComponentPolicyStatusType.IN_VIOLATION.equals(rule.getStatus()))
            .map(ProjectVersionPolicyRulesView::getName)
            .anyMatch(name -> name.equals(testPolicyName)));
    }

    private PolicyRuleView createTestPolicyRuleForProjectWithComponentVersion(ProjectView projectView, ComponentVersionView componentVersion, String policyRuleName)
        throws BlackDuckIntegrationException {
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
