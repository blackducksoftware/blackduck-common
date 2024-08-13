package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ToolsApiScannerInstaller;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.http.client.*;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ZipApiScannerInstaller;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.TestingPropertyKey;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ToolsApiScannerInstallerTest {

    @Test
    void testActualDownload() throws Exception {
//        String signatureScannerDownloadPath = "/Users/shanty/blackduck/scan-outputs/test-bd-common";
//        String blackDuckUrl = "https://us03-int-butler.nprd.sig.synopsys.com/";
//        String blackDuckUsername = "sysadmin";
//        String blackDuckPassword = "blackduck";

        String signatureScannerDownloadPath = TestingPropertyKey.TEST_BLACKDUCK_SIGNATURE_SCANNER_DOWNLOAD_PATH.fromEnvironment();
        String blackDuckUrl = TestingPropertyKey.TEST_BLACK_DUCK_SERVER_URL.fromEnvironment();
        System.out.println("blackduck url from system.getEnv() is:" + blackDuckUrl);
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
        File downloadTarget = new File(signatureScannerDownloadPath);

        ToolsApiScannerInstaller toolsApiScannerInstaller = new ToolsApiScannerInstaller(logger, blackDuckHttpClient, cleanupZipExpander, scanPathsUtility, new KeyStoreHelper(logger), new HttpUrl(blackDuckUrl),
                operatingSystemType,
                downloadTarget);
        toolsApiScannerInstaller.installOrUpdateScanner();

        ScanPaths scanPaths = scanPathsUtility.searchForScanPaths(downloadTarget);
        assertTrue(scanPaths.isManagedByLibrary());
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToJavaExecutable()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToOneJar()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToScanExecutable()));
        assertTrue(new File(scanPaths.getPathToJavaExecutable()).canExecute());
        assertTrue(new File(scanPaths.getPathToOneJar()).canExecute());
        assertTrue(new File(scanPaths.getPathToScanExecutable()).canExecute());
        // TODO should clean up test-bd-common after test runs
    }

}
