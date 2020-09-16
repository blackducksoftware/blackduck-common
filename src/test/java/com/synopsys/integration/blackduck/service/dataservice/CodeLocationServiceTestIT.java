package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.model.SimpleBdioDocument;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.bdioupload.BdioUploadCodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.bdioupload.BdioUploadService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.comprehensive.BlackDuckServices;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class CodeLocationServiceTestIT {

    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    @Test
    public void testCodeLocationByName() throws IOException, IntegrationException {
        String codeLocationName = "CodeLocationByNameTest";
        String projectName = "CodeLocationByNameTest_Project";
        String componentName = "CodeLocationByNameTest_Component";
        String version = "3.13.39";
        String group = "com.blackducksoftware.integration";

        BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);
        SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();
        MutableDependencyGraph mutableDependencyGraph = simpleBdioFactory.createMutableDependencyGraph();

        File bdioFile = File.createTempFile("bdio", "jsonld");
        bdioFile.deleteOnExit();

        // Pre-clean test data
        deleteProjectByName(blackDuckServices, projectName);
        deleteCodeLocationByName(blackDuckServices, codeLocationName);

        // Verify code location does not exist
        assertEquals(Optional.empty(), blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationName), "Confirm code location does not exist failed");

        // Create data and upload
        Dependency bdioTestDependency = simpleBdioFactory.createDependency(componentName, version, simpleBdioFactory.getExternalIdFactory().createMavenExternalId(group, componentName, version));
        mutableDependencyGraph.addChildrenToRoot(bdioTestDependency);

        SimpleBdioDocument simpleBdioDocument = simpleBdioFactory.createSimpleBdioDocument(codeLocationName, projectName, version, simpleBdioFactory.createMavenExternalId(group, projectName, version), mutableDependencyGraph);
        simpleBdioFactory.writeSimpleBdioDocumentToFile(bdioFile, simpleBdioDocument);

        UploadBatch uploadBatch = new UploadBatch(UploadTarget.createDefault(new NameVersion(projectName, version), codeLocationName, bdioFile));

        BdioUploadService bdioUploadService = blackDuckServices.blackDuckServicesFactory.createBdioUploadService();
        BdioUploadCodeLocationCreationRequest uploadRequest = bdioUploadService.createUploadRequest(uploadBatch);

        UploadBatchOutput uploadBatchOutput = bdioUploadService.uploadBdio(uploadRequest).getOutput();
        for (UploadOutput uploadOutput : uploadBatchOutput) {
            assertEquals(Result.SUCCESS, uploadOutput.getResult());
        }

        // Verify code location now exists
        assertTrue(blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationName).isPresent(), "Code location is empty after uploading.");
        assertEquals(codeLocationName, blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationName).get().getName(), "Confirm code location does exist failed");

        // Post-clean test data
        deleteProjectByName(blackDuckServices, projectName);
        deleteCodeLocationByName(blackDuckServices, codeLocationName);
    }

    private void deleteProjectByName(BlackDuckServices blackDuckServices, String projectName) throws IntegrationException {
        Optional<ProjectView> projectThatShouldNotExist = blackDuckServices.projectService.getProjectByName(projectName);
        if (projectThatShouldNotExist.isPresent()) {
            blackDuckServices.blackDuckService.delete(projectThatShouldNotExist.get());
            projectThatShouldNotExist = blackDuckServices.projectService.getProjectByName(projectName);
            assertFalse(projectThatShouldNotExist.isPresent(), "Project was not successfully deleted.");
        }
    }

    private void deleteCodeLocationByName(BlackDuckServices blackDuckServices, String codeLocationName) throws IntegrationException {
        Optional<CodeLocationView> codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationName);
        if (codeLocationView.isPresent()) {
            blackDuckServices.blackDuckService.delete(codeLocationView.get());
            codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationName);
            assertFalse(codeLocationView.isPresent(), "CodeLocation was not successfully deleted.");
        }
    }

}
