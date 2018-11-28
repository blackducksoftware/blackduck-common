/**
 * blackduck-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import java.io.File;
import java.util.List;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommand;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScannerZipInstaller;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

public class ScanBatchManager {
    private final IntLogger logger;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final ScannerZipInstaller scannerZipInstaller;
    private final ScanPathsUtility scanPathsUtility;
    private final ScanCommandRunner scanCommandRunner;
    private final File defaultInstallDirectory;

    public static ScanBatchManager createDefaultScanManager(final IntLogger logger, final BlackDuckServerConfig blackDuckServerConfig) {
        final IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables();
        final OperatingSystemType operatingSystemType = OperatingSystemType.determineFromSystem();
        final ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, operatingSystemType);
        final ScanCommandRunner scanCommandRunner = new ScanCommandRunner(logger, intEnvironmentVariables, scanPathsUtility);
        final ScannerZipInstaller scannerZipInstaller = ScannerZipInstaller.defaultUtility(logger, blackDuckServerConfig, scanPathsUtility, operatingSystemType);

        return new ScanBatchManager(logger, intEnvironmentVariables, scannerZipInstaller, scanPathsUtility, scanCommandRunner, null);
    }

    public static ScanBatchManager createScanManagerWithNoInstaller(final IntLogger logger, final IntEnvironmentVariables intEnvironmentVariables, final File defaultInstallDirectory, final ScanPathsUtility scanPathsUtility,
            final ScanCommandRunner scanCommandRunner) {
        return new ScanBatchManager(logger, intEnvironmentVariables, null, scanPathsUtility, scanCommandRunner, defaultInstallDirectory);
    }

    public static ScanBatchManager createFullScanManager(final IntLogger logger, final IntEnvironmentVariables intEnvironmentVariables, final ScannerZipInstaller scannerZipInstaller, final ScanPathsUtility scanPathsUtility,
            final ScanCommandRunner scanCommandRunner) {
        return new ScanBatchManager(logger, intEnvironmentVariables, scannerZipInstaller, scanPathsUtility, scanCommandRunner, null);
    }

    public ScanBatchManager(final IntLogger logger, final IntEnvironmentVariables intEnvironmentVariables, final ScannerZipInstaller scannerZipInstaller, final ScanPathsUtility scanPathsUtility, final ScanCommandRunner scanCommandRunner,
            final File defaultInstallDirectory) {
        this.logger = logger;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.scannerZipInstaller = scannerZipInstaller;
        this.scanPathsUtility = scanPathsUtility;
        this.scanCommandRunner = scanCommandRunner;
        this.defaultInstallDirectory = defaultInstallDirectory;
    }

    public ScanBatchOutput executeScans(final ScanBatch scanBatch) throws BlackDuckIntegrationException {
        if (scannerZipInstaller != null) {
            // if an installer is specified, it will be used to install/update the scanner
            final File installDirectory = scanBatch.getSignatureScannerInstallDirectory();
            if (!installDirectory.exists()) {
                scannerZipInstaller.installOrUpdateScanner(installDirectory);
            } else {
                try {
                    final ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(installDirectory);
                    if (scanPaths.isManagedByLibrary()) {
                        scannerZipInstaller.installOrUpdateScanner(installDirectory);
                    }
                } catch (final BlackDuckIntegrationException e) {
                    // a valid scanPaths could not be found so we will need to attempt an install
                    scannerZipInstaller.installOrUpdateScanner(installDirectory);
                }
            }
        }

        final List<ScanCommand> scanCommands = scanBatch.createScanCommands(defaultInstallDirectory, scanPathsUtility, intEnvironmentVariables);
        final List<ScanCommandOutput> scanCommandOutputs = scanCommandRunner.executeScans(scanCommands, scanBatch.isCleanupOutput());
        return new ScanBatchOutput(scanCommandOutputs);
    }

}
