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
package com.blackducksoftware.integration.hub.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.response.CurrentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.CodeLocationView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.cli.CLIDownloadUtility;
import com.blackducksoftware.integration.hub.cli.SimpleScanUtility;
import com.blackducksoftware.integration.hub.configuration.HubScanConfig;
import com.blackducksoftware.integration.hub.configuration.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.model.HostNameHelper;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class SignatureScannerService extends DataService {
    private final CIEnvironmentVariables ciEnvironmentVariables;
    private final CLIDownloadUtility cliDownloadService;
    private final ProjectService projectDataService;
    private final CodeLocationService codeLocationDataService;
    private final ScanStatusService scanStatusDataService;

    private ProjectVersionWrapper projectVersionWrapper;

    public SignatureScannerService(final HubService hubService, final CIEnvironmentVariables ciEnvironmentVariables, final CLIDownloadUtility cliDownloadService, final ProjectService projectDataService,
            final CodeLocationService codeLocationDataService, final ScanStatusService scanStatusDataService) {
        super(hubService);
        this.ciEnvironmentVariables = ciEnvironmentVariables;
        this.cliDownloadService = cliDownloadService;
        this.projectDataService = projectDataService;
        this.codeLocationDataService = codeLocationDataService;
        this.scanStatusDataService = scanStatusDataService;
    }

    public ProjectVersionWrapper installAndRunControlledScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest, final boolean shouldWaitForScansFinished)
            throws InterruptedException, IntegrationException {
        preScan(hubServerConfig, hubScanConfig, projectRequest);
        final SimpleScanUtility simpleScanService = createScanService(hubServerConfig, hubScanConfig, projectRequest);
        final File[] scanSummaryFiles = runScan(simpleScanService);
        postScan(hubScanConfig, scanSummaryFiles, projectRequest, shouldWaitForScansFinished, simpleScanService);
        return projectVersionWrapper;
    }

    private SimpleScanUtility createScanService(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) {
        final HubScanConfig controlledConfig = getControlledScanConfig(hubScanConfig);
        if (hubScanConfig.isDryRun()) {
            return new SimpleScanUtility(logger, hubService.getGson(), hubServerConfig, ciEnvironmentVariables, controlledConfig, projectRequest.name, projectRequest.versionRequest.versionName);
        } else {
            return new SimpleScanUtility(logger, hubService.getGson(), hubServerConfig, ciEnvironmentVariables, controlledConfig, null, null);
        }
    }

    /**
     * This should only be invoked directly when dryRun == true. Otherwise, installAndRunControlledScan should be used.
     */
    public File[] runControlledScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) throws InterruptedException, IntegrationException {
        final SimpleScanUtility simpleScanService = createScanService(hubServerConfig, hubScanConfig, projectRequest);
        final File[] scanSummaryFiles = runScan(simpleScanService);
        if (hubScanConfig.isCleanupLogsOnSuccess()) {
            cleanUpLogFiles(simpleScanService);
        }
        return scanSummaryFiles;
    }

    private File[] runScan(final SimpleScanUtility simpleScanService) throws IllegalArgumentException, EncryptionException, InterruptedException, HubIntegrationException {
        simpleScanService.setupAndExecuteScan();
        final File[] scanSummaryFiles = simpleScanService.getScanSummaryFiles();
        return scanSummaryFiles;
    }

    private void printConfiguration(final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) {
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
        logger.alwaysLog(String.format("--> Using Hub Project Name: %s, Version: %s, Phase: %s, Distribution: %s", projectName, projectVersionName, projectVersionPhase, projectVersionDistribution));
        hubScanConfig.print(logger);
    }

    private void preScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) throws IntegrationException {
        final Optional<String> localHostName = HostNameHelper.getMyHostName();
        logger.info("Running on machine : " + localHostName);
        printConfiguration(hubScanConfig, projectRequest);
        final CurrentVersionView currentVersion = hubService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
        if (localHostName.isPresent()) {
            cliDownloadService.performInstallation(hubScanConfig.getToolsDir(), ciEnvironmentVariables, hubServerConfig.getHubUrl().toString(), currentVersion.version, localHostName.get());
        } else {
            // TODO more specific?
            throw new IntegrationException("Could not resolve the host name.");
        }

        if (!hubScanConfig.isDryRun()) {
            projectVersionWrapper = projectDataService.getProjectVersionAndCreateIfNeeded(projectRequest);
        }
    }

    private void postScan(final HubScanConfig hubScanConfig, final File[] scanSummaryFiles, final ProjectRequest projectRequest, final boolean shouldWaitForScansFinished, final SimpleScanUtility simpleScanService)
            throws InterruptedException, IntegrationException {
        logger.trace(String.format("Scan is dry run %s", hubScanConfig.isDryRun()));
        if (hubScanConfig.isCleanupLogsOnSuccess()) {
            cleanUpLogFiles(simpleScanService);
        }

        if (!hubScanConfig.isDryRun()) {
            final List<CodeLocationView> codeLocationViews = new ArrayList<>();
            final List<ScanSummaryView> scanSummaries = new ArrayList<>();
            logger.trace(String.format("Found %s scan summary files", scanSummaryFiles.length));
            for (final File scanSummaryFile : scanSummaryFiles) {
                final ScanSummaryView scanSummary;
                try {
                    scanSummary = getScanSummaryFromFile(scanSummaryFile);
                    scanSummaries.add(scanSummary);
                    scanSummaryFile.delete();

                    // TODO update when ScanSummaryView is part of the swagger
                    final String codeLocationUrl = hubService.getFirstLinkSafely(scanSummary, ScanSummaryView.CODELOCATION_LINK);

                    final CodeLocationView codeLocationView = hubService.getResponse(codeLocationUrl, CodeLocationView.class);
                    codeLocationViews.add(codeLocationView);
                    codeLocationDataService.mapCodeLocation(codeLocationView, projectVersionWrapper.getProjectVersionView());
                } catch (final IOException ex) {
                    logger.trace("Error reading scan summary file", ex);
                }
            }
            simpleScanService.getStatusDirectory().delete();

            cleanupCodeLocations(codeLocationViews, hubScanConfig);
            if (shouldWaitForScansFinished) {
                logger.debug("Waiting for the Bom to be updated.");
                scanStatusDataService.assertScansFinished(scanSummaries);
            }
        }
    }

    private ScanSummaryView getScanSummaryFromFile(final File scanSummaryFile) throws IOException {
        final String scanSummaryJson = FileUtils.readFileToString(scanSummaryFile, Charset.forName("UTF8"));
        final ScanSummaryView scanSummaryView = hubService.getGson().fromJson(scanSummaryJson, ScanSummaryView.class);
        scanSummaryView.json = scanSummaryJson;
        return scanSummaryView;
    }

    private HubScanConfig getControlledScanConfig(final HubScanConfig originalHubScanConfig) {
        final HubScanConfigBuilder builder = new HubScanConfigBuilder();
        builder.setCodeLocationAlias(originalHubScanConfig.getCodeLocationAlias());
        builder.setVerbose(originalHubScanConfig.isVerbose());
        builder.setDryRun(originalHubScanConfig.isDryRun());
        builder.setExcludePatterns(originalHubScanConfig.getExcludePatterns());
        builder.setScanMemory(originalHubScanConfig.getScanMemory());
        builder.setToolsDir(originalHubScanConfig.getToolsDir());
        builder.setWorkingDirectory(originalHubScanConfig.getWorkingDirectory());
        builder.addAllScanTargetPaths(new ArrayList<>(originalHubScanConfig.getScanTargetPaths()));
        builder.setSnippetModeEnabled(originalHubScanConfig.isSnippetModeEnabled());
        return builder.build();
    }

    private void cleanUpLogFiles(final SimpleScanUtility simpleScanService) {
        final File standardOutputFile = simpleScanService.getStandardOutputFile();
        if (standardOutputFile != null && standardOutputFile.exists()) {
            standardOutputFile.delete();
        }
        final File cliLogDirectory = simpleScanService.getCLILogDirectory();
        if (cliLogDirectory != null && cliLogDirectory.exists()) {
            for (final File log : cliLogDirectory.listFiles()) {
                log.delete();
            }
            cliLogDirectory.delete();
        }
    }

    private void cleanupCodeLocations(final List<CodeLocationView> codeLocationsFromCurentScan, final HubScanConfig hubScanConfig) throws IntegrationException {
        if (hubScanConfig.isDeletePreviousCodeLocations() || hubScanConfig.isUnmapPreviousCodeLocations()) {
            final List<CodeLocationView> codeLocationsNotJustScanned = getCodeLocationsNotJustScanned(projectVersionWrapper.getProjectVersionView(), codeLocationsFromCurentScan);
            if (hubScanConfig.isDeletePreviousCodeLocations()) {
                codeLocationDataService.deleteCodeLocations(codeLocationsNotJustScanned);
            } else if (hubScanConfig.isUnmapPreviousCodeLocations()) {
                codeLocationDataService.unmapCodeLocations(codeLocationsNotJustScanned);
            }
        }
    }

    private List<CodeLocationView> getCodeLocationsNotJustScanned(final ProjectVersionView version, final List<CodeLocationView> codeLocationsFromCurentScan) throws IntegrationException {
        final List<CodeLocationView> codeLocationsMappedToVersion = hubService.getAllResponses(version, ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        return getCodeLocationsNotJustScanned(codeLocationsMappedToVersion, codeLocationsFromCurentScan);
    }

    private List<CodeLocationView> getCodeLocationsNotJustScanned(final List<CodeLocationView> codeLocationsMappedToVersion, final List<CodeLocationView> codeLocationsFromCurentScan) {
        final List<CodeLocationView> codeLocationsNotJustScanned = new ArrayList<>();
        for (final CodeLocationView codeLocationItemMappedToVersion : codeLocationsMappedToVersion) {
            boolean partOfCurrentScan = false;
            for (final CodeLocationView codeLocationFromCurentScan : codeLocationsFromCurentScan) {
                if (codeLocationItemMappedToVersion.url.equals(codeLocationFromCurentScan.url)) {
                    partOfCurrentScan = true;
                    break;
                }
            }
            if (!partOfCurrentScan) {
                codeLocationsNotJustScanned.add(codeLocationItemMappedToVersion);
            }
        }
        return codeLocationsNotJustScanned;
    }

}
