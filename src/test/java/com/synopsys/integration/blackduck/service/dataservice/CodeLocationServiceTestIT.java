package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.graph.ProjectDependencyGraph;
import com.synopsys.integration.bdio.model.SimpleBdioDocument;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.bdiolegacy.BdioUploadCodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.bdiolegacy.BdioUploadService;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.upload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.blackduck.comprehensive.BlackDuckServices;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.http.client.TestingPropertyKey;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
@ExtendWith(TimingExtension.class)
class CodeLocationServiceTestIT {
    private static final String BASE_ELEMENT_NAME = "CodeLocationServiceTest";
    private static final String CODE_LOCATION_NAME = BASE_ELEMENT_NAME + "__CodeLocation";
    private static final String PROJECT_NAME = BASE_ELEMENT_NAME + "__ProjectName";
    private static final String COMPONENT_NAME = BASE_ELEMENT_NAME + "__ComponentName";
    private static final String VERSION = "3.13.39";
    private static final String GROUP = "com.blackducksoftware.integration";

    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);
    private final SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();

    public CodeLocationServiceTestIT() throws IntegrationException {}

    @Test
    @Disabled
        //ejk 2020-09-17 disabling this until I can figure out a better way to create the required elements for the test to pass
    void testMappingWithProjectCodeCreator() throws IntegrationException, InterruptedException {
        /*
        This test requires a project/version: code_location_mapping_test_donotdelete/code_location_mapping_test_donotdelete
        Also, it requires a user, project_code_scanner, with the Project Code Scanner role on the above project.
         */
        String codeLocationName = "bdio to be mapped";
        File bdioFile = new File(getClass().getResource("/bdio/bdio_without_project.jsonld").getFile());
        UploadTarget uploadTarget = UploadTarget.createDefault(new NameVersion("inaccurate", "inaccurate"), codeLocationName, bdioFile);
        BdioUploadService bdioUploadService = blackDuckServices.blackDuckServicesFactory.createBdioUploadService();
        bdioUploadService.uploadBdio(uploadTarget);

        UserView projectCodeScanner = blackDuckServices.blackDuckServicesFactory.createUserGroupService().getUserByUsername("project_code_scanner").get();
        Optional<CodeLocationView> codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationName);
        int attempts = 0;
        while (!codeLocationView.isPresent()) {
            attempts++;
            if (attempts > 15) {
                fail("code location not created fast enough");
            }
            Thread.sleep(5000);
            codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationName);
        }
        assertTrue(StringUtils.isBlank(codeLocationView.get().getMappedProjectVersion()));

        HttpUrl codeLocationUrl = codeLocationView.get().getHref();

        //now use the project code scanner user
        BufferedIntLogger logger = new BufferedIntLogger();
        BlackDuckServerConfigBuilder projectCodeScannerBuilder = BlackDuckServerConfig.newBuilder();
        projectCodeScannerBuilder.setUrl(intHttpClientTestHelper.getProperty(TestingPropertyKey.TEST_BLACK_DUCK_SERVER_URL));
        projectCodeScannerBuilder.setUsername("project_code_scanner");
        projectCodeScannerBuilder.setPassword("super_secure_password");
        projectCodeScannerBuilder.setTrustCert(true);
        BlackDuckServicesFactory specialFactory = projectCodeScannerBuilder.build().createBlackDuckServicesFactory(logger);
        Optional<ProjectVersionWrapper> projectVersionWrapper = specialFactory.createProjectService().getProjectVersion(
            new NameVersion(
                "code_location_mapping_test_donotdelete",
                "code_location_mapping_test_donotdelete"
            ));
        assertTrue(projectVersionWrapper.isPresent());
        CodeLocationService specialCodeLocationService = specialFactory.createCodeLocationService();
        specialCodeLocationService.mapCodeLocation(codeLocationUrl, projectVersionWrapper.get().getProjectVersionView());

        codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationName);
        assertTrue(codeLocationView.isPresent());
        assertEquals(projectVersionWrapper.get().getProjectVersionView().getHref().string(), codeLocationView.get().getMappedProjectVersion());

        blackDuckServices.blackDuckApiClient.delete(codeLocationView.get());
    }

    @Test
    void testSingleCodeLocationByName() throws IntegrationException, IOException {
        uploadAndVerifyCodeLocation(1, 1);
    }

    @Test
    void testPagingCodeLocationByName() throws IntegrationException, IOException {
        uploadAndVerifyCodeLocation(10, 10);
    }

    private void uploadAndVerifyCodeLocation(int numberOfCodeLocations, int codeLocationToTest) throws IntegrationException, IOException {
        List<String> codeLocationNames = populateCodeLocationNames(numberOfCodeLocations);
        String codeLocationToValidate = codeLocationNames.get(codeLocationToTest - 1);

        // Pre-clean test data
        deleteCodeLocationByName(blackDuckServices, codeLocationNames);
        deleteProjectByName(blackDuckServices);

        // Verify code location does not exist
        assertEquals(
            Optional.empty(),
            blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationToValidate),
            String.format("Code location %s should not exist", codeLocationToValidate)
        );

        try {
            createAndUploadSimpleBdioObject(codeLocationNames);

            // Verify code location now exists using getSomeMatchingResponses()
            Predicate<CodeLocationView> nameMatcherPredicate = codeLocationView -> CodeLocationService.NAME_MATCHER.test(codeLocationToValidate, codeLocationView);
            BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                .commonGet()
                .setLimit(2);
            BlackDuckMultipleRequest<CodeLocationView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(blackDuckServices.apiDiscovery.metaCodelocationsLink());
            List<CodeLocationView> foundCodeLocation = blackDuckServices.blackDuckApiClient.getSomeMatchingResponses(requestMultiple, nameMatcherPredicate, 1);

            assertEquals(1, foundCodeLocation.size(), String.format("Matching code locations should be 1 but is %d", foundCodeLocation.size()));
            assertEquals(codeLocationToValidate, foundCodeLocation.get(0).getName(), "Found code location does not equal expected");

            // Verify code location now exists using getCodeLocationByName()
            assertTrue(blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationToValidate).isPresent(), "Code location is empty after uploading.");
            assertEquals(
                codeLocationToValidate,
                blackDuckServices.codeLocationService.getCodeLocationByName(codeLocationToValidate).get().getName(),
                "Found code location does not equal expected"
            );
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

            ProjectDependencyGraph dependencyGraph = new ProjectDependencyGraph(Dependency.FACTORY.createMavenDependency(GROUP, PROJECT_NAME, VERSION));
            Dependency bdioTestDependency = Dependency.FACTORY.createMavenDependency(GROUP, COMPONENT_NAME, VERSION);
            dependencyGraph.addChildrenToRoot(bdioTestDependency);

            SimpleBdioDocument simpleBdioDocument = simpleBdioFactory.createPopulatedBdioDocument(codeLocationName, dependencyGraph);
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
            blackDuckServices.blackDuckApiClient.delete(projectThatShouldNotExist.get());
        }
    }

    private void deleteCodeLocationByName(BlackDuckServices blackDuckServices, List<String> codeLocationNames) throws IntegrationException {
        Predicate<CodeLocationView> toDelete = (codeLocationView -> codeLocationNames.contains(codeLocationView.getName()));
        List<CodeLocationView> codeLocationsToDelete = blackDuckServices.blackDuckApiClient.getSomeMatchingResponses(
            blackDuckServices.apiDiscovery.metaCodelocationsLink(),
            toDelete,
            codeLocationNames.size()
        );

        for (CodeLocationView codeLocationToDelete : codeLocationsToDelete) {
            blackDuckServices.blackDuckApiClient.delete(codeLocationToDelete);
        }
    }

}
