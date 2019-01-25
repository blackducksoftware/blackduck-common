package com.synopsys.integration.blackduck.api.codelocation

import com.synopsys.integration.blackduck.api.core.ProjectRequestBuilder
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView
import com.synopsys.integration.blackduck.api.generated.view.ProjectView
import com.synopsys.integration.blackduck.codelocation.DryRunUploadResponse
import com.synopsys.integration.blackduck.codelocation.DryRunUploadService
import com.synopsys.integration.blackduck.exception.BlackDuckApiException
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory
import com.synopsys.integration.blackduck.service.ProjectService
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

@Tag("integration")
class CodeLocationRequestServiceTestIT {
    private static final IntHttpClientTestHelper restConnectionTestHelper = new IntHttpClientTestHelper();

    private IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)

    private static File dryRunFile;

    @BeforeAll
    public static void init() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
        dryRunFile = new File(classLoader.getResource('dryRun.json').getFile())
    }

    @AfterEach
    public void testCleanup() {
        BlackDuckServicesFactory services = restConnectionTestHelper.createBlackDuckServicesFactory(logger)
        Optional<ProjectView> project = services.createProjectService().getProjectByName(restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT"))
        if (project.isPresent()) {
            services.createBlackDuckService().delete(project.get())
        }
    }

    @Test
    public void testDryRunUpload() {
        final String projectName = restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT");
        final String versionName = restConnectionTestHelper.getProperty("TEST_CREATE_VERSION");

        BlackDuckServicesFactory services = restConnectionTestHelper.createBlackDuckServicesFactory(logger)
        DryRunUploadService dryRunUploadRequestService = new DryRunUploadService(services.createBlackDuckService(), logger)
        DryRunUploadResponse response = dryRunUploadRequestService.uploadDryRunFile(dryRunFile)
        assertNotNull(response)

        CodeLocationView codeLocationView = null;
        long startTime = System.currentTimeMillis();
        long duration = System.currentTimeMillis() - startTime;
        while (!codeLocationView && duration < 1000 * 60 * 5) {
            try {
                codeLocationView = services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
            } catch (Exception ignored) {
                // ignore
            }
        }
        assertNotNull(codeLocationView)
        assertTrue(StringUtils.isBlank(codeLocationView.mappedProjectVersion))

        ProjectRequestBuilder projectBuilder = new ProjectRequestBuilder()
        projectBuilder.setProjectName(projectName)
        projectBuilder.setVersionName(versionName)

        ProjectService projectService = services.createProjectService();
        ProjectRequest projectRequest = projectBuilder.build();

        ProjectVersionWrapper projectVersionWrapper = projectService.syncProjectAndVersion(projectRequest, false);

        services.createCodeLocationService().mapCodeLocation(codeLocationView, projectVersionWrapper.projectVersionView)
        codeLocationView = services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
        assertNotNull(codeLocationView)
        assertTrue(StringUtils.isNotBlank(codeLocationView.mappedProjectVersion))

        services.createCodeLocationService().unmapCodeLocation(codeLocationView)
        codeLocationView = services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
        assertNotNull(codeLocationView)
        assertTrue(StringUtils.isBlank(codeLocationView.mappedProjectVersion))

        services.createBlackDuckService().delete(codeLocationView)
        try {
            services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
            // TODO: Expects exception. integration-rest no longer throws an exception by default
            fail('This should have thrown an exception')
        } catch (BlackDuckApiException e) {
            assertEquals(404, e.getOriginalIntegrationRestException().getHttpStatusCode())
        }
    }

}
