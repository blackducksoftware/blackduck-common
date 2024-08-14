package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ToolsApiScannerInstaller;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.http.client.*;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ApiScannerInstaller.VERSION_FILENAME;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ToolsApiScannerInstallerTest {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private static File scannerInstallationDirectory;
    @BeforeEach
    public void setUp() throws IOException {
        scannerInstallationDirectory = new File(intHttpClientTestHelper.getProperty(TestingPropertyKey.TEST_BLACKDUCK_SIGNATURE_SCANNER_DOWNLOAD_PATH)); // TOME test case will create any parent directories if needed, but note that only the child most dir will be cleaned up...
    }

    @AfterAll
    public static void deleteTemporarySignatureScannerInstallDirectory() {
        FileUtils.deleteQuietly(scannerInstallationDirectory);
    }
    @Test
    void testFreshDownload() throws Exception {
        downloadSignatureScanner();
        testSubsequentDownload_differentMajorVersion();
    }

    private void downloadSignatureScanner() throws BlackDuckIntegrationException {
        IntLogger logger = new BufferedIntLogger();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = intHttpClientTestHelper.getBlackDuckServerConfigBuilder();

        blackDuckServerConfigBuilder.setLogger(logger);
        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfig.createBlackDuckHttpClient(logger);

        OperatingSystemType operatingSystemType = OperatingSystemType.determineFromSystem();
        ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, IntEnvironmentVariables.includeSystemEnv(), operatingSystemType);
        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        HttpUrl blackDuckServerUrl = blackDuckHttpClient.getBlackDuckUrl();

        ToolsApiScannerInstaller toolsApiScannerInstaller = new ToolsApiScannerInstaller(
                logger,
                blackDuckHttpClient,
                cleanupZipExpander,
                scanPathsUtility,
                new KeyStoreHelper(logger),
                blackDuckServerUrl,
                operatingSystemType,
                scannerInstallationDirectory);
        toolsApiScannerInstaller.installOrUpdateScanner();

        ScanPaths scanPaths = scanPathsUtility.searchForScanPaths(scannerInstallationDirectory);
        assertTrue(scanPaths.isManagedByLibrary());
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToJavaExecutable()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToOneJar()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToScanExecutable()));
        assertTrue(new File(scanPaths.getPathToJavaExecutable()).canExecute());
        assertTrue(new File(scanPaths.getPathToOneJar()).canExecute());
        assertTrue(new File(scanPaths.getPathToScanExecutable()).canExecute());
    }

    private void testSubsequentDownload_differentMajorVersion() throws IOException, BlackDuckIntegrationException {
        File versionFile = new File(scannerInstallationDirectory, VERSION_FILENAME);
        String localScannerVersion = FileUtils.readFileToString(versionFile, Charset.defaultCharset());
        String[] semVerParts = localScannerVersion.split("\\.");
        int majorVersion = Integer.parseInt(semVerParts[0]);
        int previousMajorVersion =- majorVersion;
        semVerParts[0] = String.valueOf(previousMajorVersion);
        String pretendInstalledScannedVersion = String.join(".", semVerParts);
        FileUtils.writeStringToFile(versionFile, pretendInstalledScannedVersion, Charset.defaultCharset());
        //do a subsequent download
        downloadSignatureScanner();
    }

}
