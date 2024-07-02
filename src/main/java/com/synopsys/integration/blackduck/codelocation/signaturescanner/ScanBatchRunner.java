/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ExistingScannerInstaller;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommand;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScannerInstaller;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScannerZipInstaller;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.SignatureScannerClient;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

public class ScanBatchRunner {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final ScanPathsUtility scanPathsUtility;
    private final ScanCommandRunner scanCommandRunner;
    private final ScannerInstaller scannerInstaller;

    public static ScanBatchRunner createDefault(
        IntLogger logger,
        BlackDuckHttpClient blackDuckHttpClient,
        BlackDuckRegistrationService blackDuckRegistrationService,
        IntEnvironmentVariables intEnvironmentVariables,
        ExecutorService executorService,
        File signatureScannerInstallDirectory
    ) {
        OperatingSystemType operatingSystemType = OperatingSystemType.determineFromSystem();
        ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, operatingSystemType);
        ScanCommandRunner scanCommandRunner = new ScanCommandRunner(logger, intEnvironmentVariables, scanPathsUtility, executorService);

        return ScanBatchRunner.createDefault(logger, blackDuckHttpClient, blackDuckRegistrationService, intEnvironmentVariables, scanPathsUtility, operatingSystemType, scanCommandRunner, signatureScannerInstallDirectory);
    }

    public static ScanBatchRunner createDefault(
        IntLogger logger,
        BlackDuckHttpClient blackDuckHttpClient,
        BlackDuckRegistrationService blackDuckRegistrationService,
        IntEnvironmentVariables intEnvironmentVariables,
        ScanPathsUtility scanPathsUtility,
        OperatingSystemType operatingSystemType,
        ScanCommandRunner scanCommandRunner,
        File signatureScannerInstallDirectory
    ) {
        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        KeyStoreHelper keyStoreHelper = new KeyStoreHelper(logger);
        ScannerInstaller scannerZipInstaller = new ScannerZipInstaller(
            logger,
            blackDuckHttpClient,
            cleanupZipExpander,
            scanPathsUtility,
            keyStoreHelper,
            blackDuckHttpClient.getBlackDuckUrl(),
            operatingSystemType,
            signatureScannerInstallDirectory
        );

        return new ScanBatchRunner(intEnvironmentVariables, scanPathsUtility, scanCommandRunner, scannerZipInstaller);
    }

    public static ScanBatchRunner createWithNoInstaller(IntEnvironmentVariables intEnvironmentVariables, ScanPathsUtility scanPathsUtility, ScanCommandRunner scanCommandRunner, File existingInstallDirectory) {
        return new ScanBatchRunner(intEnvironmentVariables, scanPathsUtility, scanCommandRunner, new ExistingScannerInstaller(existingInstallDirectory));
    }

    public static ScanBatchRunner createComplete(IntEnvironmentVariables intEnvironmentVariables, ScanPathsUtility scanPathsUtility, ScanCommandRunner scanCommandRunner, ScannerInstaller scannerInstaller) {
        return new ScanBatchRunner(intEnvironmentVariables, scanPathsUtility, scanCommandRunner, scannerInstaller);
    }

    public ScanBatchRunner(IntEnvironmentVariables intEnvironmentVariables, ScanPathsUtility scanPathsUtility, ScanCommandRunner scanCommandRunner, ScannerInstaller scannerInstaller) {
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.scanPathsUtility = scanPathsUtility;
        this.scanCommandRunner = scanCommandRunner;
        this.scannerInstaller = scannerInstaller;
    }

    public ScanBatchOutput executeScans(ScanBatch scanBatch) throws BlackDuckIntegrationException {
        File signatureScannerInstallDirectory = scannerInstaller.installOrUpdateScanner();

        List<ScanCommand> scanCommands = scanBatch.createScanCommands(signatureScannerInstallDirectory, scanPathsUtility, intEnvironmentVariables);
        List<ScanCommandOutput> scanCommandOutputs = scanCommandRunner.executeScans(scanCommands, scanBatch.isCleanupOutput());
        return new ScanBatchOutput(scanCommandOutputs);
    }

}
