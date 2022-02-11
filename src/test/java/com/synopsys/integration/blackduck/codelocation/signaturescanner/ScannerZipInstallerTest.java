package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScannerZipInstaller;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigKeys;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.SignatureScannerClient;
import com.synopsys.integration.blackduck.http.client.TestingPropertyKey;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.model.BlackDuckServerData;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

@ExtendWith(TimingExtension.class)
class ScannerZipInstallerTest {
    @Test
    void testActualDownload() throws Exception {
        String signatureScannerDownloadPath = TestingPropertyKey.TEST_BLACKDUCK_SIGNATURE_SCANNER_DOWNLOAD_PATH.fromEnvironment();
        String blackDuckUrl = TestingPropertyKey.TEST_BLACK_DUCK_SERVER_URL.fromEnvironment();
        String blackDuckUsername = TestingPropertyKey.TEST_USERNAME.fromEnvironment();
        String blackDuckPassword = TestingPropertyKey.TEST_PASSWORD.fromEnvironment();
        assertTrue(StringUtils.isNotBlank(signatureScannerDownloadPath));
        assertTrue(StringUtils.isNotBlank(blackDuckUrl));
        assertTrue(StringUtils.isNotBlank(blackDuckUsername));
        assertTrue(StringUtils.isNotBlank(blackDuckPassword));

        IntLogger logger = new BufferedIntLogger();
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setUsername(blackDuckUsername);
        blackDuckServerConfigBuilder.setPassword(blackDuckPassword);
        blackDuckServerConfigBuilder.setTimeoutInSeconds(120);
        blackDuckServerConfigBuilder.setTrustCert(true);
        blackDuckServerConfigBuilder.setLogger(logger);

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfig.createBlackDuckHttpClient(logger);
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(blackDuckHttpClient, logger);
        BlackDuckRegistrationService blackDuckRegistrationService = blackDuckServicesFactory.createBlackDuckRegistrationService();

        OperatingSystemType operatingSystemType = OperatingSystemType.determineFromSystem();
        ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, IntEnvironmentVariables.includeSystemEnv(), operatingSystemType);
        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        KeyStoreHelper keyStoreHelper = new KeyStoreHelper(logger);
        File downloadTarget = new File(signatureScannerDownloadPath);

        ScannerZipInstaller scannerZipInstaller = new ScannerZipInstaller(logger, new SignatureScannerClient(blackDuckHttpClient), blackDuckRegistrationService, cleanupZipExpander, scanPathsUtility, keyStoreHelper,
            new HttpUrl(blackDuckUrl),
            operatingSystemType,
            downloadTarget);
        scannerZipInstaller.installOrUpdateScanner();

        ScanPaths scanPaths = scanPathsUtility.searchForScanPaths(downloadTarget);
        assertTrue(scanPaths.isManagedByLibrary());
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToJavaExecutable()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToOneJar()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToScanExecutable()));
        assertTrue(new File(scanPaths.getPathToJavaExecutable()).canExecute());
        assertTrue(new File(scanPaths.getPathToOneJar()).canExecute());
        assertTrue(new File(scanPaths.getPathToScanExecutable()).canExecute());
    }

    @Disabled
    @Test
    void testInitialDownload() throws Exception {
        // This test was already disabled, so I tried to fix it and failed.
        // ERROR: No Archiver found for the stream signature.
        // The ArchiveStreamFactory::detect method cannot determine that the stream is a Zip.
        // We could change CommonZipExpander::expandUnknownFile to allow the Archive type to be supplied (bypassing detection)
        // Using different resources does not help. It might have something to do with mockito. JM-02/2022

        IntEnvironmentVariables intEnvironmentVariables = IntEnvironmentVariables.includeSystemEnv();

        InputStream zipFileStream = getClass().getResourceAsStream("/testArchive.zip");
        Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);

        SignatureScannerClient mockScannerClient = Mockito.mock(SignatureScannerClient.class);
        Mockito.when(mockScannerClient.executeGetRequest(Mockito.any(Request.class))).thenReturn(mockResponse);

        IntLogger logger = new BufferedIntLogger();
        Path tempDirectory = Files.createTempDirectory(null);
        File downloadTarget = tempDirectory.toFile();
        try {
            CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
            BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder(BlackDuckServerConfigKeys.KEYS.apiToken);
            HttpUrl mockUrl = new HttpUrl("https://synopsys.com");
            blackDuckServerConfigBuilder.setUrl(mockUrl);
            blackDuckServerConfigBuilder.setApiToken("mock-token");
            BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
            BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfig.createBlackDuckHttpClient(logger);
            BlackDuckRegistrationService blackDuckRegistrationService = Mockito.mock(BlackDuckRegistrationService.class);
            Mockito.when(blackDuckRegistrationService.getBlackDuckServerData()).thenReturn(new BlackDuckServerData(mockUrl, "version", "registration-key"));
            KeyStoreHelper keyStoreHelper = new KeyStoreHelper(logger);
            ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, OperatingSystemType.MAC);
            ScannerZipInstaller scannerZipInstaller = new ScannerZipInstaller(
                logger,
                new SignatureScannerClient(blackDuckHttpClient),
                blackDuckRegistrationService,
                cleanupZipExpander,
                scanPathsUtility,
                keyStoreHelper,
                mockUrl,
                OperatingSystemType.MAC,
                downloadTarget
            );

            assertThrows(BlackDuckIntegrationException.class, () -> scanPathsUtility.searchForScanPaths(downloadTarget));

            scannerZipInstaller.installOrUpdateScanner();

            ScanPaths scanPaths = scanPathsUtility.searchForScanPaths(downloadTarget);
            assertTrue(scanPaths.isManagedByLibrary());
            assertTrue(StringUtils.isNotBlank(scanPaths.getPathToScanExecutable()));
            assertTrue(StringUtils.isNotBlank(scanPaths.getPathToOneJar()));
            assertTrue(StringUtils.isNotBlank(scanPaths.getPathToJavaExecutable()));
            assertTrue(new File(scanPaths.getPathToScanExecutable()).canExecute());
            assertTrue(new File(scanPaths.getPathToOneJar()).canExecute());
            assertTrue(new File(scanPaths.getPathToJavaExecutable()).canExecute());
        } finally {
            FileUtils.deleteQuietly(downloadTarget);
        }
    }

}
