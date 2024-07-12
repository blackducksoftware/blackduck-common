package com.synopsys.integration.blackduck.comprehensive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatch;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchBuilder;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.SignatureScannerService;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScannerZipInstaller;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.http.client.SignatureScannerClient;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.dataservice.CodeLocationService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;
import com.synopsys.integration.wait.ResilientJobConfig;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.tracker.WaitIntervalTracker;
import com.synopsys.integration.wait.tracker.WaitIntervalTrackerFactory;

@Tag("integration")
@ExtendWith(TimingExtension.class)
class InstallAndRunSignatureScannerTestIT {
    public static final String PROJECT_NAME = "Scanner Installer Test";
    public static final String PROJECT_VERSION_NAME = "1.0";
    public static final String CODE_LOCATION_NAME = "verify-scan-installer";

    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    private File scannerDirectoryPath;

    @BeforeEach
    public void initEach() throws IOException {
        scannerDirectoryPath = Files.createTempDirectory("testscanner").toFile();
    }

    @AfterEach
    public void cleanUp() {
        FileUtils.deleteQuietly(scannerDirectoryPath);
    }

    @Test
    void testInstallingAndRunningSignatureScanner() throws IOException, InterruptedException, IntegrationException { // TOME review this
        // here, we do not want to automatically trust the server's certificate
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = intHttpClientTestHelper.getBlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setTrustCert(false);

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();

        File installDirectory = new File(scannerDirectoryPath, "scanner_install");
        File outputDirectory = new File(scannerDirectoryPath, "scanner_output");

        ScanBatch scanBatch = createScanBatch(blackDuckServerConfig, outputDirectory);

        BufferedIntLogger logger = new BufferedIntLogger();
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(logger);
        IntEnvironmentVariables environmentVariables = blackDuckServicesFactory.getEnvironmentVariables();
        OperatingSystemType operatingSystemType = OperatingSystemType.determineFromSystem();
        ExecutorService executorService = BlackDuckServicesFactory.NO_THREAD_EXECUTOR_SERVICE;
        BlackDuckHttpClient blackDuckHttpClient = blackDuckServicesFactory.getBlackDuckHttpClient();
        BlackDuckRegistrationService blackDuckRegistrationService = blackDuckServicesFactory.createBlackDuckRegistrationService();
        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        HttpUrl blackDuckServerUrl = blackDuckHttpClient.getBlackDuckUrl();

        ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, environmentVariables, operatingSystemType);
        ScanCommandRunner scanCommandRunner = new ScanCommandRunner(logger, environmentVariables, scanPathsUtility, executorService);

        // first, run a scan with an install that will NOT update the embedded keystore, which should fail
        KeyStoreHelper noOpKeyStoreHelper = new NoOpKeyStoreHelper();
        ScannerZipInstaller installerWithoutKeyStoreManagement = new ScannerZipInstaller(
            logger,
            new SignatureScannerClient(blackDuckHttpClient),
            blackDuckRegistrationService,
            cleanupZipExpander,
            scanPathsUtility,
            noOpKeyStoreHelper,
            blackDuckServerUrl,
            operatingSystemType,
            installDirectory
        );
        ScanBatchRunner scanBatchRunnerWithout = ScanBatchRunner.createComplete(environmentVariables, scanPathsUtility, scanCommandRunner, installerWithoutKeyStoreManagement);
        SignatureScannerService signatureScannerServiceWithout = blackDuckServicesFactory.createSignatureScannerService(scanBatchRunnerWithout);

        assertScanFailure(logger, blackDuckRegistrationService, signatureScannerServiceWithout, scanBatch);

        // now, delete the failed installation
        FileUtils.deleteDirectory(installDirectory);

        // second, run a scan with an install that DOES update the embedded keystore, which should succeed
        logger.resetAllLogs();
        KeyStoreHelper keyStoreHelper = new KeyStoreHelper(logger);
        ScannerZipInstaller installerWithKeyStoreManagement = new ScannerZipInstaller(logger, new SignatureScannerClient(blackDuckHttpClient), blackDuckRegistrationService, cleanupZipExpander, scanPathsUtility, keyStoreHelper,
            blackDuckServerUrl,
            operatingSystemType,
            installDirectory);
        ScanBatchRunner scanBatchRunnerWith = ScanBatchRunner.createComplete(environmentVariables, scanPathsUtility, scanCommandRunner, installerWithKeyStoreManagement);
        SignatureScannerService signatureScannerServiceWith = blackDuckServicesFactory.createSignatureScannerService(scanBatchRunnerWith);

        assertScanSuccess(logger, signatureScannerServiceWith, scanBatch);

        // finally, verify the code location exists and then delete it to clean up
        CodeLocationService codeLocationService = blackDuckServicesFactory.createCodeLocationService();
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        WaitIntervalTracker waitIntervalTracker = WaitIntervalTrackerFactory.createConstant(120, 10);
        ResilientJobConfig jobConfig = new ResilientJobConfig(logger, System.currentTimeMillis(), waitIntervalTracker);
        WaitJob.waitFor(jobConfig, () -> codeLocationService.getCodeLocationByName(CODE_LOCATION_NAME).isPresent(), "codeLocationTest");

        Optional<CodeLocationView> codeLocationViewOptional = codeLocationService.getCodeLocationByName(CODE_LOCATION_NAME);
        assertTrue(codeLocationViewOptional.isPresent());
        blackDuckApiClient.delete(codeLocationViewOptional.get());
    }

    private ScanBatch createScanBatch(BlackDuckServerConfig blackDuckServerConfig, File outputDirectory) {
        File scanFile = intHttpClientTestHelper.getFile("integration-bdio-21.0.2-sources.jar");
        ScanBatchBuilder scanBatchBuilder = new ScanBatchBuilder();
        scanBatchBuilder.fromBlackDuckServerConfig(blackDuckServerConfig);
        scanBatchBuilder.outputDirectory(outputDirectory);
        scanBatchBuilder.projectAndVersionNames(PROJECT_NAME, PROJECT_VERSION_NAME);
        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget(scanFile.getAbsolutePath(), CODE_LOCATION_NAME));
        return scanBatchBuilder.build();
    }

    private void assertScanFailure(BufferedIntLogger logger, BlackDuckRegistrationService blackDuckRegistrationService, SignatureScannerService signatureScannerService, ScanBatch scanBatch)
        throws InterruptedException, IntegrationException {
        assertEquals(0, logger.getOutputList(LogLevel.ERROR).size());
        ScanBatchOutput scanBatchOutput = signatureScannerService.performSignatureScanAndWait(scanBatch, 15 * 60);
        assertEquals(1, scanBatchOutput.getOutputs().size());
        ScanCommandOutput scanCommandOutput = scanBatchOutput.getOutputs().get(0);
        assertEquals(Result.FAILURE, scanCommandOutput.getResult());
        assertTrue(scanCommandOutput.getScanExitCode().isPresent());
        assertEquals(1, scanCommandOutput.getScanExitCode().get());

        /*
         * ejk 2021-07-16
         * TODO should this be in VersionSupport?
         */
        String version = blackDuckRegistrationService.getBlackDuckServerData().getVersion();
        List<String> versionsThatDoNotLogCorrectly = Arrays.asList("2020.2.0", "2020.2.1");
        if (!versionsThatDoNotLogCorrectly.contains(version)) {
            assertEquals(1, logger.getOutputList(LogLevel.ERROR).size());
            assertTrue(logger.getOutputList(LogLevel.ERROR).get(0).contains("use --insecure"));
        }
    }

    private void assertScanSuccess(BufferedIntLogger logger, SignatureScannerService signatureScannerService, ScanBatch scanBatch) throws InterruptedException, IntegrationException {
        assertEquals(0, logger.getOutputList(LogLevel.ERROR).size());
        ScanBatchOutput scanBatchOutput = signatureScannerService.performSignatureScanAndWait(scanBatch, 15 * 60);
        assertEquals(1, scanBatchOutput.getOutputs().size());
        ScanCommandOutput scanCommandOutput = scanBatchOutput.getOutputs().get(0);
        assertEquals(Result.SUCCESS, scanCommandOutput.getResult());
        assertEquals(0, logger.getOutputList(LogLevel.ERROR).size());
    }

    public static class NoOpKeyStoreHelper extends KeyStoreHelper {
        public NoOpKeyStoreHelper() {
            super(new SilentIntLogger());
        }

        @Override
        public void updateKeyStoreWithServerCertificate(String alias, Certificate serverCertificate, String keyStoreFilePath) {
            // do nothing
        }
    }

}
