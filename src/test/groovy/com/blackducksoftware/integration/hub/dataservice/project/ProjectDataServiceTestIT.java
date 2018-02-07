package com.blackducksoftware.integration.hub.dataservice.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blackducksoftware.integration.IntegrationTest;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

@Category(IntegrationTest.class)
public class ProjectDataServiceTestIT {

    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testGetComponentsForProjectNameAndVersionName() throws IllegalArgumentException, IntegrationException {
        final HubServicesFactory hubServices = restConnectionTestHelper.createHubServicesFactory();
        final ProjectDataService projectDataService = hubServices.createProjectDataService();

        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT");
        final String testProjectVersionName = "BomRequestServiceTest";
        final String testComponentName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT");
        final String testComponentVersionName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_VERSION");

        final List<VersionBomComponentView> bomComponents = projectDataService.getComponentsForProjectVersion(testProjectName, testProjectVersionName);
        System.out.println("BOM size: " + bomComponents.size());

        // Look for testComponent in BOM
        VersionBomComponentView foundComp = null;
        for (final VersionBomComponentView comp : bomComponents) {
            if ((testComponentName.equals(comp.componentName) && (testComponentVersionName.equals(comp.componentVersionName)))) {
                foundComp = comp;
            }
        }
        assertNotNull(foundComp);
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_USAGE"), foundComp.usages.get(0).toString());
    }

    @Test
    public void testGetBomEntriesForProjectVersion() throws IllegalArgumentException, IntegrationException {
        final HubServicesFactory hubServices = restConnectionTestHelper.createHubServicesFactory();
        final ProjectDataService projectDataService = hubServices.createProjectDataService();

        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT");
        final String testProjectVersionName = "BomRequestServiceTest";
        final String testComponentName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT");
        final String testComponentVersionName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_VERSION");

        final ProjectVersionWrapper projectVersionWrapper = projectDataService.getProjectVersion(testProjectName, testProjectVersionName);
        final ProjectVersionView projectVersion = projectVersionWrapper.getProjectVersionView();
        assertNotNull(projectVersion);

        final List<VersionBomComponentView> bomComponents = projectDataService.getAllResponsesFromLink(projectVersion, ProjectVersionView.COMPONENTS_LINK, VersionBomComponentView.class);
        System.out.println("BOM size: " + bomComponents.size());

        // Look for testComponent in BOM
        VersionBomComponentView foundComp = null;
        for (final VersionBomComponentView comp : bomComponents) {
            if (testComponentName.equals(comp.componentName) && (testComponentVersionName.equals(comp.componentVersionName))) {
                foundComp = comp;
            }
        }
        assertNotNull(foundComp);
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_USAGE"), foundComp.usages.get(0).toString());
    }

}
