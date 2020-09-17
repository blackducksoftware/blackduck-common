package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.model.SimpleBdioDocument;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
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
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class CodeLocationServiceTestIT {
    private static final String BASE_ELEMENT_NAME = "CodeLocationServiceTest";
    private static final String CODE_LOCATION_NAME = BASE_ELEMENT_NAME + "__CodeLocation";
    private static final String PROJECT_NAME = BASE_ELEMENT_NAME + "__ProjectName";
    private static final String COMPONENT_NAME = BASE_ELEMENT_NAME + "__ComponentName";
    private static final String VERSION = "3.13.39";
    private static final String GROUP = "com.blackducksoftware.integration";

    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);
    private final SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();
    private final MutableDependencyGraph mutableDependencyGraph = simpleBdioFactory.createMutableDependencyGraph();
    private final RequestFactory requestFactory = new RequestFactory();

    public CodeLocationServiceTestIT() throws IntegrationException {}

    @Test
    public void testSingleCodeLocationByName() throws IntegrationException, IOException {
        uploadAndVerifyCodeLocation(1, 1);
    }

    @Test
    public void testPagingCodeLocationByName() throws IntegrationException, IOException {
        uploadAndVerifyCodeLocation(10, 10);
    }

    private void uploadAndVerifyCodeLocation(int numberOfCodeLocations, int codeLocationToTest) throws IntegrationException, IOException {
        List<String> codeLocationNames = populateCodeLocationNames(numberOfCodeLocations);
        String codeLocationToValidate = codeLocationNames.get(codeLocationToTest - 1);

        // Pre-clean test data
        deleteCodeLocationByName(blackDuckServices, codeLocationNames);
        deleteProjectByName(blackDuckServices);

        // Verify code location does not exist
        assertEquals(Optional.empty(), blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationToValidate), String.format("Code location %s should not exist", codeLocationToValidate));

        try {
            createAndUploadSimpleBdioObject(codeLocationNames);

            // Verify code location now exists using getSomeMatchingResponses()
            Predicate<CodeLocationView> nameMatcherPredicate = CodeLocationService.NAME_MATCHER.apply(codeLocationToValidate);
            BlackDuckRequestBuilder blackDuckRequestBuilder = requestFactory.createCommonGetRequestBuilder(2, 0);
            List<CodeLocationView> foundCodeLocation = blackDuckServices.blackDuckService.getSomeMatchingResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE, blackDuckRequestBuilder, nameMatcherPredicate, 1);

            assertEquals(1, foundCodeLocation.size(), String.format("Matching code locations should be 1 but is %d", foundCodeLocation.size()));
            assertEquals(codeLocationToValidate, foundCodeLocation.get(0).getName(), "Found code location does not equal expected");

            // Verify code location now exists using getCodeLocationByName()
            assertTrue(blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationToValidate).isPresent(), "Code location is empty after uploading.");
            assertEquals(codeLocationToValidate, blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationToValidate).get().getName(), "Found code location does not equal expected");
        } finally {
            // Post-clean test data
            deleteCodeLocationByName(blackDuckServices, codeLocationNames);
            deleteProjectByName(blackDuckServices);
        }
    }

    private List<String> populateCodeLocationNames(int numberOfCodeLocations) {
        return IntStream
                   .range(0, numberOfCodeLocations)
                   .boxed()
                   .map(i -> CODE_LOCATION_NAME + i)
                   .collect(Collectors.toList());
    }

    private void createAndUploadSimpleBdioObject(List<String> codeLocationNames) throws IOException, IntegrationException {
        UploadBatch uploadBatch = new UploadBatch();

        for (String codeLocationName : codeLocationNames) {
            File bdioFile = File.createTempFile("bdio", "jsonld");
            bdioFile.deleteOnExit();

            Dependency bdioTestDependency = simpleBdioFactory.createDependency(COMPONENT_NAME, VERSION, simpleBdioFactory.getExternalIdFactory().createMavenExternalId(GROUP, COMPONENT_NAME, VERSION));
            mutableDependencyGraph.addChildrenToRoot(bdioTestDependency);

            SimpleBdioDocument simpleBdioDocument = simpleBdioFactory.createSimpleBdioDocument(codeLocationName, PROJECT_NAME, VERSION, simpleBdioFactory.createMavenExternalId(GROUP, PROJECT_NAME, VERSION), mutableDependencyGraph);
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
        }
    }

    private void deleteCodeLocationByName(BlackDuckServices blackDuckServices, List<String> codeLocationNames) throws IntegrationException {
        Predicate<CodeLocationView> toDelete = (codeLocationView -> codeLocationNames.contains(codeLocationView.getName()));
        List<CodeLocationView> codeLocationsToDelete = blackDuckServices.blackDuckService.getSomeMatchingResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE, toDelete, codeLocationNames.size());

        for (CodeLocationView codeLocationToDelete : codeLocationsToDelete) {
            blackDuckServices.blackDuckService.delete(codeLocationToDelete);
        }
    }

}
