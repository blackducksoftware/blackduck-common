/**
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import java.util.concurrent.ExecutorService;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommand;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScannerZipInstaller;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.SignatureScannerCertificateClient;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

public class ScanBatchRunner {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final ScannerZipInstaller scannerZipInstaller;
    private final ScanPathsUtility scanPathsUtility;
    private final ScanCommandRunner scanCommandRunner;
    private final File defaultInstallDirectory;

    public static ScanBatchRunner createDefault(IntLogger logger, BlackDuckHttpClient blackDuckHttpClient, IntEnvironmentVariables intEnvironmentVariables, ExecutorService executorService) {
        OperatingSystemType operatingSystemType = OperatingSystemType.determineFromSystem();
        ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, operatingSystemType);
        ScanCommandRunner scanCommandRunner = new ScanCommandRunner(logger, intEnvironmentVariables, scanPathsUtility, executorService);

        return ScanBatchRunner.createDefault(logger, blackDuckHttpClient, intEnvironmentVariables, scanPathsUtility, operatingSystemType, scanCommandRunner);
    }

    public static ScanBatchRunner createDefault(IntLogger logger, BlackDuckHttpClient blackDuckHttpClient, IntEnvironmentVariables intEnvironmentVariables, ScanPathsUtility scanPathsUtility,
        OperatingSystemType operatingSystemType, ScanCommandRunner scanCommandRunner) {
        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        BlackDuckRequestFactory blackDuckRequestFactory = new BlackDuckRequestFactory();
        SignatureScannerCertificateClient signatureScannerCertificateClient = new SignatureScannerCertificateClient(blackDuckHttpClient);
        KeyStoreHelper keyStoreHelper = new KeyStoreHelper(logger, signatureScannerCertificateClient, blackDuckRequestFactory);
        ScannerZipInstaller scannerZipInstaller = new ScannerZipInstaller(logger, blackDuckHttpClient, cleanupZipExpander, scanPathsUtility, keyStoreHelper, blackDuckHttpClient.getBaseUrl(), operatingSystemType);

        return new ScanBatchRunner(intEnvironmentVariables, scannerZipInstaller, scanPathsUtility, scanCommandRunner, null);
    }

    public static ScanBatchRunner createWithNoInstaller(IntEnvironmentVariables intEnvironmentVariables, File defaultInstallDirectory, ScanPathsUtility scanPathsUtility,
        ScanCommandRunner scanCommandRunner) {
        return new ScanBatchRunner(intEnvironmentVariables, null, scanPathsUtility, scanCommandRunner, defaultInstallDirectory);
    }

    public static ScanBatchRunner createComplete(IntEnvironmentVariables intEnvironmentVariables, ScannerZipInstaller scannerZipInstaller, ScanPathsUtility scanPathsUtility,
        ScanCommandRunner scanCommandRunner) {
        return new ScanBatchRunner(intEnvironmentVariables, scannerZipInstaller, scanPathsUtility, scanCommandRunner, null);
    }

    public ScanBatchRunner(IntEnvironmentVariables intEnvironmentVariables, ScannerZipInstaller scannerZipInstaller, ScanPathsUtility scanPathsUtility, ScanCommandRunner scanCommandRunner,
        File defaultInstallDirectory) {
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.scannerZipInstaller = scannerZipInstaller;
        this.scanPathsUtility = scanPathsUtility;
        this.scanCommandRunner = scanCommandRunner;
        this.defaultInstallDirectory = defaultInstallDirectory;
    }

    public ScanBatchOutput executeScans(ScanBatch scanBatch) throws BlackDuckIntegrationException {
        if (scannerZipInstaller != null) {
            // if an installer is specified, it will be used to install/update the scanner
            File installDirectory = scanBatch.getSignatureScannerInstallDirectory();
            if (!installDirectory.exists()) {
                scannerZipInstaller.installOrUpdateScanner(installDirectory);
            } else {
                try {
                    ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(installDirectory);
                    if (scanPaths.isManagedByLibrary()) {
                        scannerZipInstaller.installOrUpdateScanner(installDirectory);
                    }
                } catch (BlackDuckIntegrationException e) {
                    // a valid scanPaths could not be found so we will need to attempt an install
                    scannerZipInstaller.installOrUpdateScanner(installDirectory);
                }
            }
        }

        List<ScanCommand> scanCommands = scanBatch.createScanCommands(defaultInstallDirectory, scanPathsUtility, intEnvironmentVariables);
        List<ScanCommandOutput> scanCommandOutputs = scanCommandRunner.executeScans(scanCommands, scanBatch.isCleanupOutput());
        return new ScanBatchOutput(scanCommandOutputs);
    }

}
