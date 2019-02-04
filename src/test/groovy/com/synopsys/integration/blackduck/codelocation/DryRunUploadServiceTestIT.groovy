package com.synopsys.integration.blackduck.codelocation

import com.synopsys.integration.blackduck.TimingExtension
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView
import com.synopsys.integration.blackduck.exception.BlackDuckApiException
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper
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
import org.junit.jupiter.api.extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class DryRunUploadServiceTestIT {
    private static final IntHttpClientTestHelper restConnectionTestHelper = new IntHttpClientTestHelper();
    private static final long FIVE_MINUTES = 5 * 60 * 1000;

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
        logger.info("upload response: ${response}");

        int attemptCount = 0;
        long start = System.currentTimeMillis();
        long duration = System.currentTimeMillis() - start;
        CodeLocationView codeLocationView = null
        while (null == codeLocationView && duration <= FIVE_MINUTES) {
            // creating the code location can take a few seconds
            try {
                codeLocationView = codeLocationService.getCodeLocationById(response.codeLocationId)
                logger.info("Found ${codeLocationView} on attempt ${attemptCount}");
            } catch (IntegrationException ignored) {
                // ignored
            }
            Thread.sleep(5000);
            attemptCount++;
            duration = System.currentTimeMillis() - start;
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
