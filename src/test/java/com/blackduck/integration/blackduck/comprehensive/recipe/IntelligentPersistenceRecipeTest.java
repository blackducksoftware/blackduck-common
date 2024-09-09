package com.blackduck.integration.blackduck.comprehensive.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.blackduck.integration.blackduck.TimingExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.synopsys.integration.bdio.graph.ProjectDependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.bdio2.model.Bdio2Document;
import com.blackduck.integration.blackduck.bdio2.model.ProjectInfo;
import com.blackduck.integration.blackduck.bdio2.util.Bdio2Factory;
import com.blackduck.integration.blackduck.bdio2.util.Bdio2Writer;
import com.blackduck.integration.blackduck.codelocation.intelligentpersistence.IntelligentPersistenceService;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatch;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.blackduck.integration.blackduck.codelocation.upload.UploadTarget;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
@ExtendWith(TimingExtension.class)
class IntelligentPersistenceRecipeTest extends BasicRecipe {
    private static final String PROJECT_NAME = "blackduck-common-junit-test";
    private static final String PROJECT_VERSION = "0.0.1";
    public static final NameVersion PROJECT = new NameVersion(PROJECT_NAME + UUID.randomUUID(), PROJECT_VERSION + "-test");
    private static final String CODE_LOCATION_NAME = "blackduck-common/junit/small-bdio2-junit-test/0.0.1 maven/bom";

    @AfterEach
    void cleanup() throws IntegrationException {
        deleteProject(projectService.getProjectByName(PROJECT.getName()).orElse(null));
        deleteCodeLocation(CODE_LOCATION_NAME);
    }

    @Test
    void uploadBdio2() throws IOException, IntegrationException, InterruptedException {
        Bdio2Factory bdio2Factory = new Bdio2Factory();

        // create the bdio2 metadata
        ZonedDateTime now = Instant.now().atZone(ZoneId.of("EST5EDT"));
        ProjectInfo projectInfo = ProjectInfo.nameVersion(PROJECT);
        BdioMetadata bdio2Metadata = bdio2Factory.createBdioMetadata(CODE_LOCATION_NAME, projectInfo, now);

        // create the bdio2 project
        Dependency projectDependency = Dependency.FACTORY.createMavenDependency("com.synopsys.integration", PROJECT.getName(), PROJECT.getVersion());

        // create a graph of one dependency
        Dependency dependency = Dependency.FACTORY.createMavenDependency("org.apache.commons", "commons-lang3", "3.11");
        ProjectDependencyGraph dependencyGraph = new ProjectDependencyGraph(projectDependency);
        dependencyGraph.addDirectDependency(dependency);

        // now, with metadata, a project, and a graph, we can create a bdio2 document and write out the file
        Bdio2Document bdio2Document = bdio2Factory.createBdio2Document(bdio2Metadata, dependencyGraph);

        File bdio2File = File.createTempFile("test_bdio2", ".bdio");
        bdio2File.createNewFile();
        bdio2File.deleteOnExit();

        Bdio2Writer bdio2Writer = new Bdio2Writer();
        bdio2Writer.writeBdioDocument(new FileOutputStream(bdio2File), bdio2Document);

        // using the file and the previously set values, we create the UploadBatch for uploading to Black Duck
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createDefault(PROJECT, CODE_LOCATION_NAME, bdio2File));

        // now all the setup is done, we can upload the bdio2 file
        IntelligentPersistenceService intelligentPersistenceService = blackDuckServicesFactory.createIntelligentPersistenceService();
        UploadBatchOutput uploadBatchOutput = intelligentPersistenceService.uploadBdioAndWait(uploadBatch, 120, 0);
        assertFalse(uploadBatchOutput.hasAnyFailures());

        // verify that we now have a bom with 1 component
        Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(PROJECT);
        assertTrue(projectVersionWrapper.isPresent());
        List<ProjectVersionComponentVersionView> bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.get().getProjectVersionView());
        assertEquals(1, bomComponents.size());
    }
}
