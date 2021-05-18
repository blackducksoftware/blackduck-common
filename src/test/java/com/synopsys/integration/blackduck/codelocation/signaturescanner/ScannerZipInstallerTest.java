package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.SignatureScannerClient;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

@ExtendWith(TimingExtension.class)
public class ScannerZipInstallerTest {
    @Test
    public void testActualDownload() throws Exception {
        IntEnvironmentVariables intEnvironmentVariables = IntEnvironmentVariables.includeSystemEnv();

        String signatureScannerDownloadPath = intEnvironmentVariables.getValue("BLACKDUCK_SIGNATURE_SCANNER_DOWNLOAD_PATH");
        String blackDuckUrl = intEnvironmentVariables.getValue("BLACKDUCK_URL");
        String blackDuckUsername = intEnvironmentVariables.getValue("BLACKDUCK_USERNAME");
        String blackDuckPassword = intEnvironmentVariables.getValue("BLACKDUCK_PASSWORD");
        assumeTrue(StringUtils.isNotBlank(signatureScannerDownloadPath));
        assumeTrue(null != blackDuckUrl);
        assumeTrue(StringUtils.isNotBlank(blackDuckUsername));
        assumeTrue(StringUtils.isNotBlank(blackDuckPassword));

        IntLogger logger = new BufferedIntLogger();
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setUsername(blackDuckUsername);
        blackDuckServerConfigBuilder.setPassword(blackDuckPassword);
        blackDuckServerConfigBuilder.setTimeoutInSeconds(120);
        blackDuckServerConfigBuilder.setTrustCert(true);
        blackDuckServerConfigBuilder.setLogger(logger);

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfig.createBlackDuckHttpClient(logger);

        OperatingSystemType operatingSystemType = OperatingSystemType.determineFromSystem();
        ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, operatingSystemType);
        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        KeyStoreHelper keyStoreHelper = new KeyStoreHelper(logger);
        File downloadTarget = new File(signatureScannerDownloadPath);

        ScannerZipInstaller scannerZipInstaller = new ScannerZipInstaller(logger, new SignatureScannerClient(blackDuckHttpClient), cleanupZipExpander, scanPathsUtility, keyStoreHelper, new HttpUrl(blackDuckUrl), operatingSystemType, downloadTarget);
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
    public void testInitialDownload() throws Exception {
        IntEnvironmentVariables intEnvironmentVariables = IntEnvironmentVariables.includeSystemEnv();

        InputStream zipFileStream = getClass().getResourceAsStream("/blackduck_cli_mac.zip");
        Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);

        BlackDuckHttpClient mockBlackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Mockito.when(mockBlackDuckHttpClient.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.of(mockResponse));

        IntLogger logger = new BufferedIntLogger();
        Path tempDirectory = Files.createTempDirectory(null);
        File downloadTarget = tempDirectory.toFile();
        try {
            CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
            BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
            BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
            BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfig.createBlackDuckHttpClient(logger);
            KeyStoreHelper keyStoreHelper = new KeyStoreHelper(logger);
            ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, OperatingSystemType.MAC);
            ScannerZipInstaller scannerZipInstaller = new ScannerZipInstaller(logger, new SignatureScannerClient(blackDuckHttpClient), cleanupZipExpander, scanPathsUtility, keyStoreHelper, new HttpUrl("http://www.synopsys.com"),
                OperatingSystemType.MAC, downloadTarget);

            try {
                ScanPaths scanPaths = scanPathsUtility.searchForScanPaths(downloadTarget);
                fail("Should have thrown");
            } catch (BlackDuckIntegrationException e) {
            }

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
