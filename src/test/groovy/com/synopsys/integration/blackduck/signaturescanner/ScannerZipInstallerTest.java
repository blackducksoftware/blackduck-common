package com.synopsys.integration.blackduck.signaturescanner;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.test.tool.TestLogger;
import com.synopsys.integration.util.CleanupZipExpander;

public class ScannerZipInstallerTest {
    @Test
    public void testActualDownload() throws Exception {
        final String signatureScannerDownloadPath = System.getenv("BLACKDUCK_SIGNATURE_SCANNER_DOWNLOAD_PATH");
        final String blackDuckUrl = System.getenv().get("BLACKDUCK_HUB_URL");
        final String blackDuckUsername = System.getenv().get("BLACKDUCK_HUB_USERNAME");
        final String blackDuckPassword = System.getenv().get("BLACKDUCK_HUB_PASSWORD");
        Assume.assumeTrue(StringUtils.isNotBlank(signatureScannerDownloadPath));
        Assume.assumeTrue(StringUtils.isNotBlank(blackDuckUrl));
        Assume.assumeTrue(StringUtils.isNotBlank(blackDuckUsername));
        Assume.assumeTrue(StringUtils.isNotBlank(blackDuckPassword));

        final File downloadTarget = new File(signatureScannerDownloadPath);

        final IntLogger logger = new TestLogger();
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setUrl(blackDuckUrl);
        hubServerConfigBuilder.setUsername(blackDuckUsername);
        hubServerConfigBuilder.setPassword(blackDuckPassword);
        hubServerConfigBuilder.setTimeout(120);
        hubServerConfigBuilder.setTrustCert(true);
        hubServerConfigBuilder.setLogger(logger);

        final HubServerConfig hubServerConfig = hubServerConfigBuilder.build();
        final ScannerZipInstaller scannerZipInstaller = ScannerZipInstaller.defaultUtility(logger, hubServerConfig, OperatingSystemType.determineFromSystem());

        scannerZipInstaller.installOrUpdateScanner(downloadTarget);

        final ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, OperatingSystemType.determineFromSystem());
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
        final InputStream zipFileStream = getClass().getResourceAsStream("/blackduck_cli_mac.zip");
        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);

        final RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
        Mockito.when(mockRestConnection.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.of(mockResponse));

        final IntLogger logger = new TestLogger();
        final Path tempDirectory = Files.createTempDirectory(null);
        final File downloadTarget = tempDirectory.toFile();
        try {
            final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
            final ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, OperatingSystemType.MAC);
            final ScannerZipInstaller scannerZipInstaller = new ScannerZipInstaller(logger, mockRestConnection, cleanupZipExpander, scanPathsUtility, "http://www.google.com", OperatingSystemType.MAC);

            try {
                final ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(downloadTarget);
                fail("Should have thrown");
            } catch (final HubIntegrationException e) {
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
