package com.blackduck.integration.blackduck.comprehensive;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ToolsApiScannerInstaller;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfig;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.blackduck.integration.blackduck.http.client.TestingPropertyKey;
import com.blackduck.integration.blackduck.keystore.KeyStoreHelper;
import com.blackduck.integration.log.BufferedIntLogger;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.util.CleanupZipExpander;
import com.blackduck.integration.util.IntEnvironmentVariables;
import com.blackduck.integration.util.OperatingSystemType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ApiScannerInstaller.BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY;
import static com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ApiScannerInstaller.VERSION_FILENAME;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ToolsApiScannerInstallerTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private static File scannerInstallationDirectory;
    public void setUp() {
        // The provided TEST_BLACKDUCK_SIGNATURE_SCANNER_DOWNLOAD_PATH will be created during this test if it does not exist, including any parent directories in the path. However, in the @AfterAll cleanup, only the child most directory will be deleted.
        scannerInstallationDirectory = new File(intHttpClientTestHelper.getProperty(TestingPropertyKey.TEST_BLACKDUCK_SIGNATURE_SCANNER_DOWNLOAD_PATH));
    }

    @AfterAll
    public static void deleteTemporarySignatureScannerInstallDirectory() {
        FileUtils.deleteQuietly(scannerInstallationDirectory);
    }
    @Test
    void testFreshDownload_followedByAnUpdate() throws Exception {
        setUp();
        downloadSignatureScanner();
        // Tweak version file so we pretend the installed version in part 1 of this test is a lower major version than the BD server this IT is connected to
        decrementMajorVersionOfInstalledSignatureScanner();
        // Attempt a subsequent download request that will upgrade always
        downloadSignatureScanner();
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