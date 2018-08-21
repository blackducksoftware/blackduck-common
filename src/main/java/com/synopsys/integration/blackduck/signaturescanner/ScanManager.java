package com.synopsys.integration.blackduck.signaturescanner;

import java.io.IOException;
import java.util.List;

import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.exception.EncryptionException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class ScanManager {
    private final IntLogger logger;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final ScannerZipInstaller scannerZipInstaller;
    private final ScanPathsUtility scanPathsUtility;
    private final ScanJobRunner scanJobRunner;

    public static ScanManager createDefaultScanManager(final IntLogger logger, final HubServerConfig hubServerConfig) throws EncryptionException {
        final IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables();
        final ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger);
        final ScanJobRunner scanJobRunner = new ScanJobRunner(logger, intEnvironmentVariables, scanPathsUtility);
        final ScannerZipInstaller scannerZipInstaller = ScannerZipInstaller.defaultUtility(logger, hubServerConfig);

        return new ScanManager(logger, intEnvironmentVariables, scannerZipInstaller, scanPathsUtility, scanJobRunner);
    }

    public static ScanManager createOfflineScanManager(final IntLogger logger) {
        final IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables();
        final ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger);
        final ScanJobRunner scanJobRunner = new ScanJobRunner(logger, intEnvironmentVariables, scanPathsUtility);

        return new ScanManager(logger, intEnvironmentVariables, null, scanPathsUtility, scanJobRunner);
    }

    public ScanManager(final IntLogger logger, final IntEnvironmentVariables intEnvironmentVariables, final ScannerZipInstaller scannerZipInstaller, final ScanPathsUtility scanPathsUtility, final ScanJobRunner scanJobRunner) {
        this.logger = logger;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.scannerZipInstaller = scannerZipInstaller;
        this.scanPathsUtility = scanPathsUtility;
        this.scanJobRunner = scanJobRunner;
    }

    public List<ScanCommandOutput> executeScans(final ScanJob scanJob) throws IOException, HubIntegrationException {
        if (scannerZipInstaller != null) {
            scannerZipInstaller.retrieveBlackDuckScanInstallPath(scanJob.getSignatureScannerInstallDirectory());
        }
        return scanJobRunner.executeScans(scanJob);
    }

}
