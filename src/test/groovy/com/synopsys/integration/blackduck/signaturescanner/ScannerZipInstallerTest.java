package com.synopsys.integration.blackduck.signaturescanner;

import java.io.File;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.test.TestLogger;

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

        final IntLogger intLogger = new TestLogger();
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setUrl(blackDuckUrl);
        hubServerConfigBuilder.setUsername(blackDuckUsername);
        hubServerConfigBuilder.setPassword(blackDuckPassword);
        hubServerConfigBuilder.setTimeout(120);
        hubServerConfigBuilder.setTrustCert(true);
        hubServerConfigBuilder.setLogger(intLogger);

        final HubServerConfig hubServerConfig = hubServerConfigBuilder.build();
        final ScannerZipInstaller scannerZipInstaller = ScannerZipInstaller.defaultUtility(intLogger, hubServerConfig);

        final Optional<String> scanInstallPath = scannerZipInstaller.retrieveBlackDuckScanInstallPath(downloadTarget);
        Assert.assertTrue(scanInstallPath.isPresent());
        Assert.assertTrue(scanInstallPath.get().length() > 0);
    }

    //    @Test
    //    public void testInitialDownload() throws Exception {
    //        final InputStream zipFileStream = getClass().getResourceAsStream("/swip_mac.zip");
    //        final Response mockResponse = Mockito.mock(Response.class);
    //        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);
    //
    //        final RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
    //        Mockito.when(mockRestConnection.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.of(mockResponse));
    //
    //        final IntLogger intLogger = new TestLogger();
    //        final Path tempDirectory = Files.createTempDirectory(null);
    //        final File downloadTarget = tempDirectory.toFile();
    //        downloadTarget.deleteOnExit();
    //
    //        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
    //        final SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
    //        final Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();
    //
    //        Assert.assertTrue(swipCliPath.isPresent());
    //        Assert.assertTrue(swipCliPath.get().length() > 0);
    //    }
    //
    //    @Test
    //    public void testNotDownloadIfNotUpdatedOnServer() throws Exception {
    //        final RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
    //        Mockito.when(mockRestConnection.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.empty());
    //
    //        final TestLogger intLogger = new TestLogger();
    //
    //        final Path tempDirectory = Files.createTempDirectory(null);
    //        final File downloadTarget = tempDirectory.toFile();
    //        downloadTarget.deleteOnExit();
    //
    //        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
    //        final SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
    //        final Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();
    //
    //        Assert.assertFalse(swipCliPath.isPresent());
    //        Assert.assertTrue(intLogger.getOutputString().contains("skipping download"));
    //    }
    //
    //    @Test
    //    public void testDownloadIfServerUpdated() throws Exception {
    //        final InputStream zipFileStream = getClass().getResourceAsStream("/swip_mac.zip");
    //        final Response mockResponse = Mockito.mock(Response.class);
    //        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);
    //
    //        final RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
    //        Mockito.when(mockRestConnection.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.of(mockResponse));
    //
    //        final TestLogger intLogger = new TestLogger();
    //
    //        final Path tempDirectory = Files.createTempDirectory(null);
    //        final File downloadTarget = tempDirectory.toFile();
    //        downloadTarget.deleteOnExit();
    //
    //        final File installDirectory = new File(downloadTarget, SwipDownloadUtility.SWIP_CLI_INSTALL_DIRECTORY);
    //        installDirectory.mkdirs();
    //        installDirectory.deleteOnExit();
    //
    //        // create a directory that should be deleted by the update download/extract code
    //        final File directoryOfPreviousExtraction = new File(installDirectory, "temp_swip_cli_version");
    //        directoryOfPreviousExtraction.mkdirs();
    //        Assert.assertTrue(directoryOfPreviousExtraction.isDirectory());
    //        Assert.assertTrue(directoryOfPreviousExtraction.exists());
    //
    //        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
    //        final SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
    //        final Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();
    //
    //        Assert.assertTrue(swipCliPath.isPresent());
    //        Assert.assertTrue(swipCliPath.get().length() > 0);
    //        Assert.assertFalse(directoryOfPreviousExtraction.exists());
    //        Assert.assertTrue(intLogger.getOutputString().contains("There were items"));
    //        Assert.assertTrue(intLogger.getOutputString().contains("that are being deleted"));
    //    }

}
