package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.IssueView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionIssuesView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.IssueRequest;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class IssueServiceTestIT {
    private final static ExternalIdFactory externalIdFactory = new ExternalIdFactory();
    private final static IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper();
    private static BlackDuckServicesFactory blackDuckServicesFactory;
    private static BlackDuckApiClient blackDuckApiClient;
    private static ProjectService projectService;
    private static IssueService issueService;
    private static ProjectBomService projectBomService;

    private static ProjectView project = null;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        blackDuckServicesFactory = INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();
        blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        projectService = blackDuckServicesFactory.createProjectService();
        issueService = blackDuckServicesFactory.createIssueService();
        projectBomService = blackDuckServicesFactory.createProjectBomService();
    }

    @AfterEach
    public void tearDownAfterTest() throws Exception {
        if (project != null) {
            blackDuckApiClient.delete(project);
            project = null;
        }
    }

    @Test
    public void addingAndRetrievingIssues() throws IntegrationException {
        // create the project/version
        String projectName = "issues_test";
        String versionName = "1.0.0";
        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, versionName);
        ProjectVersionWrapper projectVersionWrapper = projectService.syncProjectAndVersion(projectSyncModel);

        // delete it when we're done
        project = projectVersionWrapper.getProjectView();

        // add some components to the BOM
        ExternalId blackDuckCommon = externalIdFactory.createMavenExternalId("com.synopsys.integration", "blackduck-common", "50.0.0");
        ExternalId apacheFileUpload = externalIdFactory.createMavenExternalId("commons-fileupload", "commons-fileupload");
        projectBomService.addComponentToProjectVersion(blackDuckCommon, projectVersionWrapper.getProjectVersionView());
        projectBomService.addComponentToProjectVersion(apacheFileUpload, projectVersionWrapper.getProjectVersionView());

        List<ProjectVersionComponentVersionView> componentsForProjectVersion = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(2, componentsForProjectVersion.size());

        // check existing issues
        List<ProjectVersionIssuesView> issuesForProjectVersion = issueService.getIssuesForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(0, issuesForProjectVersion.size());

        ProjectVersionComponentVersionView blackduckCommonComponent =
            componentsForProjectVersion
                .stream()
                .filter(component -> component.getComponentName().equalsIgnoreCase("blackduck-common"))
                .findAny()
                .get();

        ProjectVersionComponentVersionView fileUploadComponent =
            componentsForProjectVersion
                .stream()
                .filter(component -> !component.getComponentName().equalsIgnoreCase("blackduck-common"))
                .findAny()
                .get();

        // add some issues
        IssueRequest blackduckCommonIssue = createIssueRequest("bd-common-1", "bd desc 1");
        IssueRequest fileUploadIssue = createIssueRequest("apache-1", "apache desc 1");
        issueService.createIssueForComponent(blackduckCommonComponent, blackduckCommonIssue);
        issueService.createIssueForComponent(fileUploadComponent, fileUploadIssue);

        // check issues added and found
        issuesForProjectVersion = issueService.getIssuesForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(2, issuesForProjectVersion.size());
        Set<String> descriptions = convert(issuesForProjectVersion)
                                       .stream()
                                       .map(IssueView::getIssueDescription)
                                       .collect(Collectors.toSet());
        assertTrue(descriptions.contains("bd desc 1"));
        assertTrue(descriptions.contains("apache desc 1"));

        Optional<IssueView> issueByKey = issueService.getIssueByKey(projectVersionWrapper.getProjectVersionView(), "bd-common-1");
        assertTrue(issueByKey.get().getIssueDescription().equals("bd desc 1"));

        // update issue
        issueByKey.get().setIssueDescription("bd desc 2");
        issueService.updateIssue(issueByKey.get());
        Optional<IssueView> updatedIssue = issueService.getIssueByKey(projectVersionWrapper.getProjectVersionView(), "bd-common-1");
        assertTrue(updatedIssue.get().getIssueDescription().equals("bd desc 2"));
    }

    private IssueRequest createIssueRequest(String issueId, String description) {
        IssueRequest issueRequest = new IssueRequest();
        issueRequest.setIssueId(issueId);
        issueRequest.setIssueDescription(description);
        issueRequest.setIssueAssignee("assignee");
        issueRequest.setIssueLink("issue link");
        issueRequest.setIssueStatus("happy");
        issueRequest.setIssueCreatedAt(Date.from(Instant.now()));

        return issueRequest;
    }

    private List<IssueView> convert(List<ProjectVersionIssuesView> toConvert) throws IntegrationException {
        List<IssueView> issues = new ArrayList<>();

        for (ProjectVersionIssuesView projectVersionIssuesView : toConvert) {
            issues.add(issueService.getIssueView(projectVersionIssuesView));
        }

        return issues;
    }

}
