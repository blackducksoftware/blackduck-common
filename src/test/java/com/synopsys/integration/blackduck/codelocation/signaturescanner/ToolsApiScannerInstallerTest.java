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

import static com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ApiScannerInstaller.BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ApiScannerInstaller.VERSION_FILENAME;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ToolsApiScannerInstallerTest {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private static File scannerInstallationDirectory;
    public void setUp() {
        scannerInstallationDirectory = new File(intHttpClientTestHelper.getProperty(TestingPropertyKey.TEST_BLACKDUCK_SIGNATURE_SCANNER_DOWNLOAD_PATH)); // TOME test case will create any parent directories if needed, but note that only the child most dir will be cleaned up...
    }

//    @AfterAll
    public static void deleteTemporarySignatureScannerInstallDirectory() {
        FileUtils.deleteQuietly(scannerInstallationDirectory);
    }
    @Test
    void testFreshDownload() throws Exception {
        setUp();
        downloadSignatureScanner();
        // Tweak version file so we pretend the installed version in part 1 of this test is a different major version than the BD server the test is connected to
        decrementMajorVersionOfInstalledSignatureScanner();
        // Attempt a subsequent download request that will upgrade always
        downloadSignatureScanner();
        deleteTemporarySignatureScannerInstallDirectory();
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

    /**
     * Takes a version string like "2024.7.0" and changes it to "2023.7.0". This is so we can confirm the api/tools
     * download request is successful even when upgrading/downgrading from a different major version.
     * We use the term "major version" loosely here since BD server versions do not follow semantic versioning but
     * this matches the logic on the server side since "major version" upgrades/downgrades fail unless our request
     * includes the Accept-Version: ANY header. Confirms fix for IDETECT-4455 and would highlight potential breaking
     * scan-cli versioning changes on the server side.
     * @throws IOException, BlackDuckIntegrationException
     */
    private void decrementMajorVersionOfInstalledSignatureScanner() {
        try {
            File versionFile = new File(scannerInstallationDirectory.toString() + "/" + BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY, VERSION_FILENAME);
            String localScannerVersion = FileUtils.readFileToString(versionFile, Charset.defaultCharset());
            String[] semVerParts = localScannerVersion.split("\\.");
            int majorVersion = Integer.parseInt(semVerParts[0]);
            int previousMajorVersion = majorVersion - 1;
            semVerParts[0] = String.valueOf(previousMajorVersion);
            String pretendInstalledScannedVersion = String.join(".", semVerParts);
            FileUtils.writeStringToFile(versionFile, pretendInstalledScannedVersion, Charset.defaultCharset());
        } catch (IOException e) {

        }
    }
}
