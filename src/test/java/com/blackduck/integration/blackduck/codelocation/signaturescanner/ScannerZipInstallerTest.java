package com.blackduck.integration.blackduck.codelocation.signaturescanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ZipApiScannerInstaller;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfig;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.http.client.SignatureScannerClient;
import com.blackduck.integration.blackduck.http.client.TestingPropertyKey;
import com.blackduck.integration.blackduck.keystore.KeyStoreHelper;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

@ExtendWith(TimingExtension.class)
@Tag("integration")
class ScannerZipInstallerTest {
    @Test
    void testActualDownload() throws Exception {
        String signatureScannerDownloadPath = TestingPropertyKey.TEST_BLACKDUCK_SIGNATURE_SCANNER_DOWNLOAD_PATH.fromEnvironment();
        String blackDuckUrl = TestingPropertyKey.TEST_BLACK_DUCK_SERVER_URL.fromEnvironment();
        String blackDuckUsername = TestingPropertyKey.TEST_USERNAME.fromEnvironment();
        String blackDuckPassword = TestingPropertyKey.TEST_PASSWORD.fromEnvironment();
        assertTrue(StringUtils.isNotBlank(signatureScannerDownloadPath));

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

        ZipApiScannerInstaller zipApiScannerInstaller = new ZipApiScannerInstaller(logger, new SignatureScannerClient(blackDuckHttpClient), blackDuckRegistrationService, cleanupZipExpander, scanPathsUtility, keyStoreHelper,
            new HttpUrl(blackDuckUrl),
            operatingSystemType,
            downloadTarget);
        zipApiScannerInstaller.installOrUpdateScanner();

        ScanPaths scanPaths = scanPathsUtility.searchForScanPaths(downloadTarget);
        assertTrue(scanPaths.isManagedByLibrary());
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToJavaExecutable()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToOneJar()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToScanExecutable()));
        assertTrue(new File(scanPaths.getPathToJavaExecutable()).canExecute());
        assertTrue(new File(scanPaths.getPathToOneJar()).canExecute());
        assertTrue(new File(scanPaths.getPathToScanExecutable()).canExecute());
    }

}
