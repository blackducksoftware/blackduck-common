package com.synopsys.integration.blackduck.codelocation

import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory
import com.synopsys.integration.blackduck.service.DryRunUploadResponse
import com.synopsys.integration.blackduck.service.DryRunUploadService
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger
import com.synopsys.integration.rest.exception.IntegrationRestException
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

@Tag("integration")
public class DryRunUploadServiceTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    private static File dryRunFile;

    private IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)

    @BeforeAll
    public static void init() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
        dryRunFile = new File(classLoader.getResource('dryRun.json').getFile())
    }

    @Test
    public void testDryRunUpload() {
        BlackDuckServicesFactory services = restConnectionTestHelper.createBlackDuckServicesFactory(logger)
        DryRunUploadService dryRunUploadRequestService = new DryRunUploadService(services.createBlackDuckService(), logger)
        DryRunUploadResponse response = dryRunUploadRequestService.uploadDryRunFile(dryRunFile)
        assertNotNull(response)

        CodeLocationView codeLocationView = services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
        assertNotNull(codeLocationView)

        //cleanup
        services.createCodeLocationService().deleteCodeLocation(codeLocationView)
        try {
            services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
            fail('This should have thrown an exception')
        } catch (IntegrationRestException e) {
            assertEquals(404, e.getHttpStatusCode())
        }
    }

}
