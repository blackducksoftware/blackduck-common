/**
 * hub-common
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
package com.blackducksoftware.integration.hub.cli.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    public ParallelSimpleScanner(final IntLogger logger, final IntEnvironmentVariables intEnvironmentVariables, final Gson gson) {
        this.logger = logger;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.gson = gson;
    }

    public List<ScanTargetOutput> executeScans(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest, final CLILocation cliLocation, final int numberOfParallelProcessors)
            throws InterruptedException, IntegrationException {
        final List<ScanTargetOutput> scanTargetOutputs = new ArrayList<>();
        final List<ScanPathCallable> scanPathCallables = createScanPathCallables(hubScanConfig.createSignatureScanConfigs(), logger, hubServerConfig, hubScanConfig.isDryRun(), projectRequest, numberOfParallelProcessors, cliLocation, gson);

        logger.info("Starting the Hub signature scans");
        final ExecutorService pool = Executors.newFixedThreadPool(numberOfParallelProcessors);
        try {
            final List<Future<ScanTargetOutput>> submittedScanPathCallables = new ArrayList<>();
            for (final ScanPathCallable scanPathCallable : scanPathCallables) {
                submittedScanPathCallables.add(pool.submit(scanPathCallable));
            }
            for (final Future<ScanTargetOutput> futureScanTargetOutput : submittedScanPathCallables) {
                final ScanTargetOutput scanTargetOutput = futureScanTargetOutput.get();
                if (scanTargetOutput != null) {
                    scanTargetOutputs.add(scanTargetOutput);
                }
            }
        } catch (final ExecutionException e) {
            throw new ScanFailedException(String.format("Encountered a problem waiting for a scan to finish. %s", e.getMessage()), e);
        } finally {
            // get() was called on every java.util.concurrent.Future, no need to wait any longer
            pool.shutdownNow();
        }
        logger.info("Completed the Hub signature scans");
        return scanTargetOutputs;
    }

    private List<ScanPathCallable> createScanPathCallables(final List<SignatureScanConfig> signatureScanConfigs, final IntLogger logger, final HubServerConfig hubServerConfig, final boolean dryRun,
            final ProjectRequest projectRequest, final int numberOfParallelProcessors,
            final CLILocation cliLocation, final Gson gson) {
        final List<ScanPathCallable> scanPathCallables = new ArrayList<>();

        String projectName = null;
        String projectVersionName = null;
        if (dryRun && null != projectRequest && StringUtils.isNotBlank(projectRequest.name) && null != projectRequest.versionRequest && StringUtils.isNotBlank(projectRequest.versionRequest.versionName)) {
            projectName = projectRequest.name;
            projectVersionName = projectRequest.versionRequest.versionName;
        }
        boolean runningParallelScans = false;
        if (numberOfParallelProcessors > 1) {
            runningParallelScans = true;
        }

        for (final SignatureScanConfig signatureScanConfig : signatureScanConfigs) {
            final ScanPathCallable scanPathCallable = new ScanPathCallable(new ThreadIntLogger(logger), hubServerConfig, intEnvironmentVariables, signatureScanConfig, projectName, projectVersionName, runningParallelScans, cliLocation,
                    gson);
            scanPathCallables.add(scanPathCallable);
        }
        return scanPathCallables;
    }
}
