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
package com.blackducksoftware.integration.hub.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.response.CurrentVersionView;
import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.cli.CLIDownloadUtility;
import com.blackducksoftware.integration.hub.cli.CLILocation;
import com.blackducksoftware.integration.hub.cli.parallel.ParallelSimpleScanner;
import com.blackducksoftware.integration.hub.cli.summary.ScanServiceOutput;
import com.blackducksoftware.integration.hub.cli.summary.ScanTargetOutput;
import com.blackducksoftware.integration.hub.configuration.HubScanConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.summary.Result;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.IntEnvironmentVariables;

public class SignatureScannerService extends DataService {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final CLIDownloadUtility cliDownloadService;
    private final ProjectService projectDataService;
    private final CodeLocationService codeLocationService;
    private final Optional<ExecutorService> optionalExecutorService;

    private ProjectVersionWrapper projectVersionWrapper;

    public SignatureScannerService(HubService hubService, IntLogger logger, IntEnvironmentVariables intEnvironmentVariables, CLIDownloadUtility cliDownloadService, ProjectService projectDataService,
            CodeLocationService codeLocationService) {
        super(hubService, logger);
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.cliDownloadService = cliDownloadService;
        this.projectDataService = projectDataService;
        this.codeLocationService = codeLocationService;
        optionalExecutorService = Optional.empty();
    }

    public SignatureScannerService(HubService hubService, IntLogger logger, IntEnvironmentVariables intEnvironmentVariables, CLIDownloadUtility cliDownloadService, ProjectService projectDataService, CodeLocationService codeLocationService,
            ExecutorService executorService) {
        super(hubService, logger);
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.cliDownloadService = cliDownloadService;
        this.projectDataService = projectDataService;
        this.codeLocationService = codeLocationService;
        optionalExecutorService = Optional.of(executorService);
    }

    public ScanServiceOutput executeScans(HubServerConfig hubServerConfig, HubScanConfig hubScanConfig, ProjectRequest projectRequest) throws InterruptedException, IntegrationException {
        CLILocation cliLocation = preScan(hubServerConfig, hubScanConfig, projectRequest);

        ParallelSimpleScanner parallelSimpleScanner;
        if (optionalExecutorService.isPresent()) {
            parallelSimpleScanner = new ParallelSimpleScanner(logger, intEnvironmentVariables, hubService.getGson(), optionalExecutorService.get());
        } else {
            parallelSimpleScanner = new ParallelSimpleScanner(logger, intEnvironmentVariables, hubService.getGson());
        }

        List<ScanTargetOutput> scanTargetOutputs = parallelSimpleScanner.executeScans(hubServerConfig, hubScanConfig, projectRequest, cliLocation);

        logger.info("Starting the post scan steps");
        List<ScanSummaryView> scanSummaryViews = new ArrayList<>();
        List<File> logDirectories = new ArrayList<>();
        for (ScanTargetOutput scanTargetOutput : scanTargetOutputs) {
            if (scanTargetOutput.getResult() == Result.SUCCESS) {
                scanSummaryViews.add(scanTargetOutput.getScanSummaryView());
                logDirectories.add(scanTargetOutput.getLogDirectory());
            }
        }
        cleanLogs(hubScanConfig.isCleanupLogsOnSuccess(), logDirectories);
        logger.info("Completed the post scan steps");
        return new ScanServiceOutput(projectVersionWrapper, scanTargetOutputs);
    }

    private CLILocation preScan(HubServerConfig hubServerConfig, HubScanConfig hubScanConfig, ProjectRequest projectRequest) throws IntegrationException {
        printConfiguration(hubScanConfig, projectRequest);
        CurrentVersionView currentVersion = hubService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
        File directoryToInstallTo = hubScanConfig.getCommonScanConfig().getToolsDir();
        CLILocation cliLocation = cliDownloadService.performInstallation(directoryToInstallTo, hubServerConfig.getHubUrl().toString(), currentVersion.version);

        if (!hubScanConfig.getCommonScanConfig().isDryRun()) {
            projectVersionWrapper = projectDataService.getProjectVersionAndCreateIfNeeded(projectRequest);
        }
        return cliLocation;
    }

    private void printConfiguration(HubScanConfig hubScanConfig, ProjectRequest projectRequest) {
        logger.alwaysLog(String.format("--> Log Level : %s", logger.getLogLevel().name()));
        String projectName = null;
        String projectVersionName = null;
        String projectVersionPhase = null;
        String projectVersionDistribution = null;
        if (projectRequest != null) {
            projectName = projectRequest.name;
            if (projectRequest.versionRequest != null) {
                projectVersionName = projectRequest.versionRequest.versionName;
                projectVersionPhase = projectRequest.versionRequest.phase == null ? null : projectRequest.versionRequest.phase.toString();
                projectVersionDistribution = projectRequest.versionRequest.distribution == null ? null : projectRequest.versionRequest.distribution.toString();
            }
        }
        logger.alwaysLog(String.format("--> Using Hub Project Name : %s, Version : %s, Phase : %s, Distribution : %s", projectName, projectVersionName, projectVersionPhase, projectVersionDistribution));
        hubScanConfig.print(logger);
    }

    private void cleanLogs(boolean cleanupLogDirectories, List<File> logDirectories) {
        if (cleanupLogDirectories) {
            if (null != logDirectories && !logDirectories.isEmpty()) {
                for (File logDirectory : logDirectories) {
                    if (null != logDirectory && logDirectory.isDirectory()) {
                        try {
                            FileUtils.deleteDirectory(logDirectory);
                        } catch (IOException e) {
                            logger.error(String.format("Could not delete the directory '%s' because: %s", logDirectory.getAbsolutePath(), e.getMessage()), e);
                        }
                    }
                }
            }
        }
    }

}
