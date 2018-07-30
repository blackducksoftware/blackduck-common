/**
 * hub-common
 * <p>
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.cli.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.cli.CLILocation;
import com.blackducksoftware.integration.hub.cli.SignatureScanConfig;
import com.blackducksoftware.integration.hub.cli.summary.ScanTargetOutput;
import com.blackducksoftware.integration.hub.configuration.HubScanConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.exception.ScanFailedException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.IntEnvironmentVariables;
import com.google.gson.Gson;

public class ParallelSimpleScanner {
    private final IntLogger logger;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final Gson gson;
    private final Optional<ExecutorService> optionalExecutorService;

    public ParallelSimpleScanner(IntLogger logger, IntEnvironmentVariables intEnvironmentVariables, Gson gson) {
        this.logger = logger;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.gson = gson;
        optionalExecutorService = Optional.empty();
    }

    public ParallelSimpleScanner(IntLogger logger, IntEnvironmentVariables intEnvironmentVariables, Gson gson, ExecutorService executorService) {
        this.logger = logger;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.gson = gson;
        optionalExecutorService = Optional.of(executorService);
    }

    public List<ScanTargetOutput> executeDryRunScans(HubScanConfig hubScanConfig, ProjectRequest projectRequest, CLILocation cliLocation) throws InterruptedException, IntegrationException {
        return executeScans(null, hubScanConfig, projectRequest, cliLocation);
    }

    public List<ScanTargetOutput> executeScans(HubServerConfig hubServerConfig, HubScanConfig hubScanConfig, ProjectRequest projectRequest, CLILocation cliLocation) throws InterruptedException, IntegrationException {
        List<ScanTargetOutput> scanTargetOutputs = new ArrayList<>();
        List<ScanPathCallable> scanPathCallables = createScanPathCallables(hubScanConfig.createSignatureScanConfigs(), logger, hubServerConfig, hubScanConfig.getCommonScanConfig().isDryRun(), projectRequest, cliLocation, gson);

        logger.info("Starting the Hub signature scans");

        try {
            if (optionalExecutorService.isPresent()) {
                ExecutorService executorService = optionalExecutorService.get();
                List<Future<ScanTargetOutput>> submittedScanPathCallables = new ArrayList<>();
                for (ScanPathCallable scanPathCallable : scanPathCallables) {
                    submittedScanPathCallables.add(executorService.submit(scanPathCallable));
                }
                for (Future<ScanTargetOutput> futureScanTargetOutput : submittedScanPathCallables) {
                    ScanTargetOutput scanTargetOutput = futureScanTargetOutput.get();
                    if (scanTargetOutput != null) {
                        scanTargetOutputs.add(scanTargetOutput);
                    }
                }
            } else {
                for (ScanPathCallable scanPathCallable : scanPathCallables) {
                    scanTargetOutputs.add(scanPathCallable.call());
                }
            }
        } catch (ExecutionException e) {
            throw new ScanFailedException(String.format("Encountered a problem waiting for a scan to finish. %s", e.getMessage()), e);
        }
        logger.info("Completed the Hub signature scans");
        return scanTargetOutputs;
    }

    private List<ScanPathCallable> createScanPathCallables(
            List<SignatureScanConfig> signatureScanConfigs, IntLogger logger, HubServerConfig hubServerConfig, boolean dryRun, ProjectRequest projectRequest, CLILocation cliLocation,
            Gson gson) {
        List<ScanPathCallable> scanPathCallables = new ArrayList<>();

        String projectName = null;
        String projectVersionName = null;
        if (null != projectRequest && StringUtils.isNotBlank(projectRequest.name) && null != projectRequest.versionRequest && StringUtils.isNotBlank(projectRequest.versionRequest.versionName)) {
            projectName = projectRequest.name;
            projectVersionName = projectRequest.versionRequest.versionName;
        }

        for (SignatureScanConfig signatureScanConfig : signatureScanConfigs) {
            ScanPathCallable scanPathCallable;
            if (null != hubServerConfig) {
                scanPathCallable = new ScanPathCallable(logger, hubServerConfig, intEnvironmentVariables, signatureScanConfig, projectName, projectVersionName, cliLocation, gson);
            } else {
                scanPathCallable = new ScanPathCallable(logger, intEnvironmentVariables, signatureScanConfig, projectName, projectVersionName, cliLocation, gson);
            }

            scanPathCallables.add(scanPathCallable);
        }
        return scanPathCallables;
    }
}
