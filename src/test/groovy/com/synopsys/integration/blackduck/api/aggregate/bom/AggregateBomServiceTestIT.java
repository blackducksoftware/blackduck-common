package com.synopsys.integration.blackduck.api.aggregate.bom;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;

@Tag("integration")
public class AggregateBomServiceTestIT {
    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testGetBomEntriesForUrl() throws IllegalArgumentException, IntegrationException {
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();

        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT");
        final String testProjectVersionName = "BomRequestServiceTest";
        final String testComponentName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT");
        final String testComponentVersionName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_VERSION");

        final Optional<ProjectView> project = blackDuckServicesFactory.createProjectService().getProjectByName(testProjectName);
        assertTrue(project.isPresent());
        final List<ProjectVersionView> projectVersions = blackDuckService.getAllResponses(project.get(), ProjectView.VERSIONS_LINK_RESPONSE);
        ProjectVersionView projectVersion = null;
        for (final ProjectVersionView projectVersionCandidate : projectVersions) {
            if (projectVersionCandidate.getVersionName().equals(testProjectVersionName)) {
                projectVersion = projectVersionCandidate;
            }
        }
        assertNotNull(projectVersion);

        final String bomUrl = projectVersion.getFirstLink(ProjectVersionView.COMPONENTS_LINK).get();
        final List<VersionBomComponentView> bomComponents = blackDuckService.getResponses(bomUrl, VersionBomComponentView.class, true);
        System.out.println("BOM size: " + bomComponents.size());

        // Look for testComponent in BOM
        VersionBomComponentView foundComp = null;
        for (final VersionBomComponentView comp : bomComponents) {
            if ((testComponentName.equals(comp.getComponentName()) && (testComponentVersionName.equals(comp.getComponentVersionName())))) {
                foundComp = comp;
            }
        }
        assertNotNull(foundComp);
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_USAGE"), foundComp.getUsages().get(0).toString());
    }

    @Test
    public void testGetBomEntriesForProjectVersion() throws IllegalArgumentException, IntegrationException {
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();

        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT");
        final String testProjectVersionName = "BomRequestServiceTest";
        final String testComponentName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT");
        final String testComponentVersionName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_VERSION");

        final Optional<ProjectView> project = blackDuckServicesFactory.createProjectService().getProjectByName(testProjectName);
        assertTrue(project.isPresent());
        final List<ProjectVersionView> projectVersions = blackDuckServicesFactory.createBlackDuckService().getAllResponses(project.get(), ProjectView.VERSIONS_LINK_RESPONSE);
        ProjectVersionView projectVersion = null;
        for (final ProjectVersionView projectVersionCandidate : projectVersions) {
            if (projectVersionCandidate.getVersionName().equals(testProjectVersionName)) {
                projectVersion = projectVersionCandidate;
            }
        }
        assertNotNull(projectVersion);

        final List<VersionBomComponentView> bomComponents = blackDuckServicesFactory.createBlackDuckService().getAllResponses(projectVersion, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        System.out.println("BOM size: " + bomComponents.size());

        // Look for testComponent in BOM
        VersionBomComponentView foundComp = null;
        for (final VersionBomComponentView comp : bomComponents) {
            if (testComponentName.equals(comp.getComponentName()) && (testComponentVersionName.equals(comp.getComponentVersionName()))) {
                foundComp = comp;
            }
        }
        assertNotNull(foundComp);
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_USAGE"), foundComp.getUsages().get(0).toString());
    }

}
