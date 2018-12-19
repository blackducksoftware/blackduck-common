package com.synopsys.integration.blackduck.codelocation

import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView
import com.synopsys.integration.blackduck.exception.BlackDuckApiException
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper
import com.synopsys.integration.blackduck.service.BlackDuckService
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory
import com.synopsys.integration.blackduck.service.CodeLocationService
import com.synopsys.integration.exception.IntegrationException
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger
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
        BlackDuckService blackDuckService = services.createBlackDuckService()
        CodeLocationService codeLocationService = services.createCodeLocationService()
        DryRunUploadService dryRunUploadRequestService = new DryRunUploadService(services.createBlackDuckService(), logger)
        DryRunUploadResponse response = dryRunUploadRequestService.uploadDryRunFile(dryRunFile)
        assertNotNull(response)

        final int maxAttempts = 10;
        int attempt = 0;
        CodeLocationView codeLocationView = null
        while (null == codeLocationView && attempt < maxAttempts) {
            // creating the code location can take a few seconds
            try {
                codeLocationView = codeLocationService.getCodeLocationById(response.codeLocationId)
            } catch (IntegrationException ignored) {
                // ignored
            }
            attempt++;
            Thread.sleep(5000);
        }
        assertNotNull(codeLocationView)

        //cleanup
        blackDuckService.delete(codeLocationView)
        try {
            codeLocationService.getCodeLocationById(response.codeLocationId)
            fail('This should have thrown an exception')
        } catch (BlackDuckApiException e) {
            assertEquals(404, e.getOriginalIntegrationRestException().httpStatusCode)
        }
    }

}
