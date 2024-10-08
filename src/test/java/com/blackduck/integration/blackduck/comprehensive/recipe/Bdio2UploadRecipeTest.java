package com.blackduck.integration.blackduck.comprehensive.recipe;

import com.blackduck.integration.bdio.graph.ProjectDependencyGraph;
import com.blackduck.integration.bdio.model.dependency.Dependency;
import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.bdio2.model.Bdio2Document;
import com.blackduck.integration.blackduck.bdio2.model.GitInfo;
import com.blackduck.integration.blackduck.bdio2.model.ProjectInfo;
import com.blackduck.integration.blackduck.bdio2.util.Bdio2Factory;
import com.blackduck.integration.blackduck.bdio2.util.Bdio2Writer;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatch;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.blackduck.integration.blackduck.codelocation.upload.UploadTarget;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.util.NameVersion;
import com.blackducksoftware.bdio2.Bdio;
import com.blackducksoftware.bdio2.BdioMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ExtendWith(TimingExtension.class)
class Bdio2UploadRecipeTest extends BasicRecipe {
    public static final String CODE_LOCATION_NAME = "bdio2 code location junit";
    public static final NameVersion PROJECT = new NameVersion("bdio2uploadtest-" + UUID.randomUUID().toString(), "test");
    private static final String GROUP_NAME = "com.blackduck.integration";

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
        ProjectInfo projectInfo = new ProjectInfo(
            PROJECT,
            GROUP_NAME,
            null, // TODO: What is this supposed to look like? Only used for chunking? JM-04/2022
            new GitInfo(
                "https://github.com/blackducksoftware/blackduck-common",
                "4a1f431d7aa4ac15f755edd5de004f07d36ae89a",
                "master"
            )
        );
        BdioMetadata bdio2Metadata = bdio2Factory.createBdioMetadata(CODE_LOCATION_NAME, projectInfo, now);

        // create a graph of one dependency
        Dependency projectDependency = Dependency.FACTORY.createMavenDependency(GROUP_NAME, PROJECT.getName(), PROJECT.getVersion());
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
        UploadBatchOutput uploadBatchOutput = bdio2UploadService.uploadBdioAndWait(uploadBatch, 120);
        assertFalse(uploadBatchOutput.hasAnyFailures());

        Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(PROJECT);
        assertTrue(projectVersionWrapper.isPresent());

        // Verify project headers are being set correctly
        String projectName = projectVersionWrapper.get().getProjectView().getName();
        String projectVersionName = projectVersionWrapper.get().getProjectVersionView().getVersionName();
        assertEquals(PROJECT, new NameVersion(projectName, projectVersionName));
        assertEquals(PROJECT.getName(), bdio2Document.getBdioMetadata().get(Bdio.DataProperty.project.toString()));
        assertEquals(PROJECT.getVersion(), bdio2Document.getBdioMetadata().get(Bdio.DataProperty.projectVersion.toString()));
        assertEquals(GROUP_NAME, bdio2Document.getBdioMetadata().get(Bdio.DataProperty.projectGroup.toString()));

        // verify that we now have a bom with 1 component
        List<ProjectVersionComponentVersionView> bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.get().getProjectVersionView());
        assertEquals(1, bomComponents.size());
    }

}
