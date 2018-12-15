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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScannerZipInstaller;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

public class ScannerZipInstallerTest {
    @Test
    public void testActualDownload() throws Exception {
        final IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables();

        final String signatureScannerDownloadPath = intEnvironmentVariables.getValue("BLACKDUCK_SIGNATURE_SCANNER_DOWNLOAD_PATH");
        final String blackDuckUrl = intEnvironmentVariables.getValue("BLACKDUCK_URL");
        final String blackDuckUsername = intEnvironmentVariables.getValue("BLACKDUCK_USERNAME");
        final String blackDuckPassword = intEnvironmentVariables.getValue("BLACKDUCK_PASSWORD");
        assumeTrue(StringUtils.isNotBlank(signatureScannerDownloadPath));
        assumeTrue(StringUtils.isNotBlank(blackDuckUrl));
        assumeTrue(StringUtils.isNotBlank(blackDuckUsername));
        assumeTrue(StringUtils.isNotBlank(blackDuckPassword));

        final File downloadTarget = new File(signatureScannerDownloadPath);

        final IntLogger logger = new BufferedIntLogger();
        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setUsername(blackDuckUsername);
        blackDuckServerConfigBuilder.setPassword(blackDuckPassword);
        blackDuckServerConfigBuilder.setTimeout(120);
        blackDuckServerConfigBuilder.setTrustCert(true);
        blackDuckServerConfigBuilder.setLogger(logger);

        final BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        final ScannerZipInstaller scannerZipInstaller = ScannerZipInstaller.defaultUtility(logger, blackDuckServerConfig, intEnvironmentVariables, OperatingSystemType.determineFromSystem());

        scannerZipInstaller.installOrUpdateScanner(downloadTarget);

        final ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, OperatingSystemType.determineFromSystem());
        final ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(downloadTarget);
        assertTrue(scanPaths.isManagedByLibrary());
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToJavaExecutable()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToOneJar()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToScanExecutable()));
        assertTrue(new File(scanPaths.getPathToJavaExecutable()).canExecute());
        assertTrue(new File(scanPaths.getPathToOneJar()).canExecute());
        assertTrue(new File(scanPaths.getPathToScanExecutable()).canExecute());
    }

    @Test
    public void testInitialDownload() throws Exception {
        final IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables();

        final InputStream zipFileStream = getClass().getResourceAsStream("/blackduck_cli_mac.zip");
        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);

        final RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
        Mockito.when(mockRestConnection.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.of(mockResponse));

        final IntLogger logger = new BufferedIntLogger();
        final Path tempDirectory = Files.createTempDirectory(null);
        final File downloadTarget = tempDirectory.toFile();
        try {
            final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
            final ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, OperatingSystemType.MAC);
            final ScannerZipInstaller scannerZipInstaller = new ScannerZipInstaller(logger, mockRestConnection, cleanupZipExpander, scanPathsUtility, "http://www.google.com", OperatingSystemType.MAC);

            try {
                final ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(downloadTarget);
                fail("Should have thrown");
            } catch (final BlackDuckIntegrationException e) {
            }

            scannerZipInstaller.installOrUpdateScanner(downloadTarget);

            final ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(downloadTarget);
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
