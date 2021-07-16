package com.synopsys.integration.blackduck.comprehensive.recipe;

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

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.blackducksoftware.bdio2.model.Project;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.bdio2.model.Bdio2Document;
import com.synopsys.integration.blackduck.bdio2.util.Bdio2Factory;
import com.synopsys.integration.blackduck.bdio2.util.Bdio2Writer;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class Bdio2UploadRecipeTest extends BasicRecipe {
    public static final String CODE_LOCATION_NAME = "bdio2 code location junit";
    public static final NameVersion PROJECT = new NameVersion("bdio2uploadtest-" + UUID.randomUUID().toString(), "test");

    private Optional<ProjectVersionWrapper> projectVersionWrapper;

    @AfterEach
    public void cleanup() throws IntegrationException {
        deleteProject(projectService.getProjectByName(PROJECT.getName()).orElse(null));
        deleteCodeLocation(CODE_LOCATION_NAME);
    }

    @Test
    public void uploadBdio2() throws IOException, IntegrationException, InterruptedException {
        Bdio2Factory bdio2Factory = new Bdio2Factory();

        // create the bdio2 metadata
        ZonedDateTime now = Instant.now().atZone(ZoneId.of("EST5EDT"));
        BdioMetadata bdio2Metadata = bdio2Factory.createBdioMetadata(CODE_LOCATION_NAME, now);

        // create the bdio2 project
        ExternalIdFactory externalIdFactory = new ExternalIdFactory();
        ExternalId externalId = externalIdFactory.createMavenExternalId("com.synopsys.integration", PROJECT.getName(), PROJECT.getVersion());
        Project bdio2Project = bdio2Factory.createProject(externalId, PROJECT.getName(), PROJECT.getVersion());

        // create a graph of one dependency
        Dependency dependency = createDependency(externalIdFactory, "org.apache.commons", "commons-lang3", "3.11");
        MutableDependencyGraph dependencyGraph = new MutableMapDependencyGraph();
        dependencyGraph.addChildToRoot(dependency);

        // now, with metadata, a project, and a graph, we can create a bdio2 document and write out the file
        Bdio2Document bdio2Document = bdio2Factory.createBdio2Document(bdio2Metadata, bdio2Project, dependencyGraph);

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

        // verify that we now have a bom with 1 component
        projectVersionWrapper = projectService.getProjectVersion(PROJECT);
        assertTrue(projectVersionWrapper.isPresent());
        List<ProjectVersionComponentVersionView> bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.get().getProjectVersionView());
        assertEquals(1, bomComponents.size());
    }

    @NotNull
    private Dependency createDependency(ExternalIdFactory externalIdFactory, String group, String artifact, String version) {
        ExternalId commonsLangId = externalIdFactory.createMavenExternalId(group, artifact, version);
        Dependency dependency = new Dependency(artifact, version, commonsLangId);
        return dependency;
    }

}
