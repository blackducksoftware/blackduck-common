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
package com.blackducksoftware.integration.hub.dataservice.cli;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.model.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.view.CodeLocationView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionService;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.cli.CLIDownloadUtility;
import com.blackducksoftware.integration.hub.cli.SimpleScanUtility;
import com.blackducksoftware.integration.hub.dataservice.codelocation.CodeLocationDataService;
import com.blackducksoftware.integration.hub.dataservice.phonehome.PhoneHomeDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.dataservice.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBodyBuilder;
import com.blackducksoftware.integration.phonehome.enums.ThirdPartyName;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;
import com.google.gson.Gson;

public class CLIDataService extends HubService {

    private final Gson gson;
    private final IntLogger logger;
    private final CIEnvironmentVariables ciEnvironmentVariables;
    private final HubVersionService hubVersionRequestService;
    private final CLIDownloadUtility cliDownloadService;
    private final PhoneHomeDataService phoneHomeDataService;
    private final ProjectDataService projectDataService;
    private final CodeLocationDataService codeLocationDataService;
    private final ScanStatusDataService scanStatusDataService;
    private final MetaHandler metaService;

    private ProjectVersionWrapper projectVersionWrapper;

    public CLIDataService(final RestConnection restConnection, final CIEnvironmentVariables ciEnvironmentVariables, final HubVersionService hubVersionRequestService, final CLIDownloadUtility cliDownloadService,
            final PhoneHomeDataService phoneHomeDataService, final ProjectDataService projectDataService, final CodeLocationDataService codeLocationDataService,
            final ScanStatusDataService scanStatusDataService) {
        super(restConnection);
        this.gson = restConnection.gson;
        this.logger = restConnection.logger;
        this.ciEnvironmentVariables = ciEnvironmentVariables;
        this.hubVersionRequestService = hubVersionRequestService;
        this.cliDownloadService = cliDownloadService;
        this.phoneHomeDataService = phoneHomeDataService;
        this.projectDataService = projectDataService;
        this.codeLocationDataService = codeLocationDataService;
        this.scanStatusDataService = scanStatusDataService;
        this.metaService = new MetaHandler(logger);
    }

    public ProjectVersionWrapper installAndRunControlledScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest, final boolean shouldWaitForScansFinished,
            final ThirdPartyName thirdPartyName, final String thirdPartyVersion, final String pluginVersion) throws IntegrationException {
        return installAndRunControlledScan(hubServerConfig, hubScanConfig, projectRequest, shouldWaitForScansFinished, thirdPartyName.getName(), thirdPartyVersion, pluginVersion);
    }

    public ProjectVersionWrapper installAndRunControlledScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest, final boolean shouldWaitForScansFinished,
            final String thirdPartyName,
            final String thirdPartyVersion, final String pluginVersion) throws IntegrationException {
        PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = null;
        try {
            phoneHomeRequestBodyBuilder = phoneHomeDataService.createInitialPhoneHomeRequestBodyBuilder(thirdPartyName, thirdPartyVersion, pluginVersion);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
        }
        preScan(hubServerConfig, hubScanConfig, projectRequest, phoneHomeRequestBodyBuilder);
        final SimpleScanUtility simpleScanService = createScanService(hubServerConfig, hubScanConfig, projectRequest);
        final File[] scanSummaryFiles = runScan(simpleScanService);
        postScan(hubScanConfig, scanSummaryFiles, projectRequest, shouldWaitForScansFinished, simpleScanService);
        return projectVersionWrapper;
    }

    private SimpleScanUtility createScanService(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) {
        final HubScanConfig controlledConfig = getControlledScanConfig(hubScanConfig);
        if (hubScanConfig.isDryRun()) {
            return new SimpleScanUtility(logger, gson, hubServerConfig, ciEnvironmentVariables, controlledConfig, projectRequest.name, projectRequest.versionRequest.versionName);
        } else {
            return new SimpleScanUtility(logger, gson, hubServerConfig, ciEnvironmentVariables, controlledConfig, null, null);
        }
    }

    /**
     * This should only be invoked directly when dryRun == true. Otherwise, installAndRunControlledScan should be used.
     */
    public File[] runControlledScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) throws IntegrationException {
        final SimpleScanUtility simpleScanService = createScanService(hubServerConfig, hubScanConfig, projectRequest);
        final File[] scanSummaryFiles = runScan(simpleScanService);
        if (hubScanConfig.isCleanupLogsOnSuccess()) {
            cleanUpLogFiles(simpleScanService);
        }
        return scanSummaryFiles;
    }

    private File[] runScan(final SimpleScanUtility simpleScanService) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
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
        logger.alwaysLog(String.format("--> Using Hub Project Name : %s, Version : %s, Phase : %s, Distribution : %s", projectName, projectVersionName, projectVersionPhase, projectVersionDistribution));
        hubScanConfig.print(logger);
    }

    private void preScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest, final PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder) throws IntegrationException {
        final String localHostName = HostnameHelper.getMyHostname();
        logger.info("Running on machine : " + localHostName);
        printConfiguration(hubScanConfig, projectRequest);
        final String hubVersion = hubVersionRequestService.getHubVersion();
        cliDownloadService.performInstallation(hubScanConfig.getToolsDir(), ciEnvironmentVariables, hubServerConfig.getHubUrl().toString(), hubVersion, localHostName);
        phoneHomeDataService.phoneHome(phoneHomeRequestBodyBuilder);

        if (!hubScanConfig.isDryRun()) {
            projectVersionWrapper = projectDataService.getProjectVersionAndCreateIfNeeded(projectRequest);
        }
    }

    private void postScan(final HubScanConfig hubScanConfig, final File[] scanSummaryFiles, final ProjectRequest projectRequest, final boolean shouldWaitForScansFinished, final SimpleScanUtility simpleScanService)
            throws IntegrationException {
        logger.trace("Scan is dry run ${hubScanConfig.isDryRun()}");
        if (hubScanConfig.isCleanupLogsOnSuccess()) {
            cleanUpLogFiles(simpleScanService);
        }

        if (!hubScanConfig.isDryRun()) {
            final List<CodeLocationView> codeLocationViews = new ArrayList<>();
            final List<ScanSummaryView> scanSummaries = new ArrayList<>();
            logger.trace("Found ${scanSummaryFiles.length} scan summary files");
            for (final File scanSummaryFile : scanSummaryFiles) {
                ScanSummaryView scanSummary;
                try {
                    scanSummary = getScanSummaryFromFile(scanSummaryFile);
                    scanSummaries.add(scanSummary);
                    scanSummaryFile.delete();
                    final String codeLocationUrl = metaService.getFirstLinkSafely(scanSummary, MetaHandler.CODE_LOCATION_BOM_STATUS_LINK);

                    final CodeLocationView codeLocationView = codeLocationDataService.getResponse(codeLocationUrl, CodeLocationView.class);
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
        final ScanSummaryView scanSummaryView = gson.fromJson(scanSummaryJson, ScanSummaryView.class);
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
        final List<CodeLocationView> codeLocationsMappedToVersion = getAllResponsesFromLink(version, ProjectVersionView.CODELOCATIONS_LINK, CodeLocationView.class);
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
