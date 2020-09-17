package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.IntStream;

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

    private static final String BASE_ELEMENT_NAME = "CodeLocationServiceTestDANA";
    private static final String CODE_LOCATION_NAME = BASE_ELEMENT_NAME + "__CodeLocation";
    private static final String PROJECT_NAME = BASE_ELEMENT_NAME + "__ProjectName";
    private static final String COMPONENT_NAME = BASE_ELEMENT_NAME + "__ComponentName";
    private static final String VERSION = "3.13.39";
    private static final String GROUP = "com.blackducksoftware.integration";

    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);
    private final SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();
    private final MutableDependencyGraph mutableDependencyGraph = simpleBdioFactory.createMutableDependencyGraph();

    public CodeLocationServiceTestIT() throws IntegrationException {}

    @Test
    public void testSingleCodeLocationByName() throws IntegrationException, IOException {
        uploadAndVerifyBdio(1, 1);
    }

    @Test
    public void testPagingCodeLocationByName() throws IntegrationException, IOException {
        uploadAndVerifyBdio(101, 101);
    }

    private void uploadAndVerifyBdio(Integer numberOfCodeLocations, Integer codeLocationToTest) throws IntegrationException, IOException {
        String[] codeLocationNames = populateCodeLocationNames(numberOfCodeLocations);
        String codeLocationToValidate = codeLocationNames[codeLocationToTest - 1];

        // Pre-clean test data
        deleteCodeLocationByName(blackDuckServices, codeLocationNames);
        deleteProjectByName(blackDuckServices);

        // Verify code location does not exist
        assertEquals(Optional.empty(), blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationToValidate), String.format("Code location %s should not exist", codeLocationToValidate));

        try {
            createAndUploadSimpleBdioObject(codeLocationNames);

            // Verify code location now exists
            assertTrue(blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationToValidate).isPresent(), "Code location is empty after uploading.");
            assertEquals(codeLocationToValidate, blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationToValidate).get().getName(), String.format("Code location %s should exist", codeLocationToValidate));
        } finally {
            // Post-clean test data
            deleteCodeLocationByName(blackDuckServices, codeLocationNames);
            deleteProjectByName(blackDuckServices);
        }
    }

    private String[] populateCodeLocationNames(Integer numberOfCodeLocations) {
        String[] codeLocations = new String[numberOfCodeLocations];

        IntStream.range(0, numberOfCodeLocations).forEach(num -> {
                codeLocations[num] = CODE_LOCATION_NAME + num;
            }
        );

        return codeLocations;
    }

    private void createAndUploadSimpleBdioObject(String[] codeLocationNames) throws IOException, IntegrationException {
        UploadBatch uploadBatch = new UploadBatch();
        File bdioFile;
        Dependency bdioTestDependency;
        SimpleBdioDocument simpleBdioDocument;

        for (String codeLocationName : codeLocationNames) {
            bdioFile = File.createTempFile("bdio", "jsonld");
            bdioFile.deleteOnExit();

            bdioTestDependency = simpleBdioFactory.createDependency(COMPONENT_NAME, VERSION, simpleBdioFactory.getExternalIdFactory().createMavenExternalId(GROUP, COMPONENT_NAME, VERSION));
            mutableDependencyGraph.addChildrenToRoot(bdioTestDependency);

            simpleBdioDocument = simpleBdioFactory.createSimpleBdioDocument(codeLocationName, PROJECT_NAME, VERSION, simpleBdioFactory.createMavenExternalId(GROUP, PROJECT_NAME, VERSION), mutableDependencyGraph);
            simpleBdioFactory.writeSimpleBdioDocumentToFile(bdioFile, simpleBdioDocument);

            uploadBatch.addUploadTarget(UploadTarget.createDefault(new NameVersion(PROJECT_NAME, VERSION), codeLocationName, bdioFile));
        }

        BdioUploadService bdioUploadService = blackDuckServices.blackDuckServicesFactory.createBdioUploadService();
        BdioUploadCodeLocationCreationRequest uploadRequest = bdioUploadService.createUploadRequest(uploadBatch);

        UploadBatchOutput uploadBatchOutput = bdioUploadService.uploadBdio(uploadRequest).getOutput();
        for (UploadOutput uploadOutput : uploadBatchOutput) {
            assertEquals(Result.SUCCESS, uploadOutput.getResult(), String.format("Upload result for %s was not successful", uploadOutput.getCodeLocationName()));
        }
    }

    private void deleteProjectByName(BlackDuckServices blackDuckServices) throws IntegrationException {
        Optional<ProjectView> projectThatShouldNotExist = blackDuckServices.projectService.getProjectByName(PROJECT_NAME);
        if (projectThatShouldNotExist.isPresent()) {
            blackDuckServices.blackDuckService.delete(projectThatShouldNotExist.get());
            projectThatShouldNotExist = blackDuckServices.projectService.getProjectByName(PROJECT_NAME);
            assertFalse(projectThatShouldNotExist.isPresent(), "Project was not successfully deleted.");
        }
    }

    private void deleteCodeLocationByName(BlackDuckServices blackDuckServices, String[] codeLocationNames) throws IntegrationException {
        for (String codeLocationName : codeLocationNames) {
            Optional<CodeLocationView> codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationName);
            if (codeLocationView.isPresent()) {
                blackDuckServices.blackDuckService.delete(codeLocationView.get());
                codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationName);
                assertFalse(codeLocationView.isPresent(), "CodeLocation was not successfully deleted.");
            }
        }
    }

}
