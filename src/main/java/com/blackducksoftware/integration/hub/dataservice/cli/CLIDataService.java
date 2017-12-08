/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationService;
import com.blackducksoftware.integration.hub.api.item.MetaUtility;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionService;
import com.blackducksoftware.integration.hub.api.project.ProjectService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryService;
import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.cli.CLIDownloadUtility;
import com.blackducksoftware.integration.hub.cli.SimpleScanUtility;
import com.blackducksoftware.integration.hub.dataservice.phonehome.PhoneHomeDataService;
import com.blackducksoftware.integration.hub.dataservice.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.CodeLocationView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBodyBuilder;
import com.blackducksoftware.integration.phonehome.enums.ThirdPartyName;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;
import com.google.gson.Gson;

public class CLIDataService {
    private final Gson gson;
    private final IntLogger logger;
    private final CIEnvironmentVariables ciEnvironmentVariables;
    private final HubVersionService hubVersionRequestService;
    private final CLIDownloadUtility cliDownloadService;
    private final PhoneHomeDataService phoneHomeDataService;
    private final ProjectService projectRequestService;
    private final ProjectVersionService projectVersionRequestService;
    private final CodeLocationService codeLocationRequestService;
    private final ScanSummaryService scanSummaryRequestService;
    private final ScanStatusDataService scanStatusDataService;
    private final MetaUtility metaService;

    private HubSupportHelper hubSupportHelper;
    private ProjectVersionView version;

    public CLIDataService(final IntLogger logger, final Gson gson, final CIEnvironmentVariables ciEnvironmentVariables, final HubVersionService hubVersionRequestService, final CLIDownloadUtility cliDownloadService,
            final PhoneHomeDataService phoneHomeDataService, final ProjectService projectRequestService, final ProjectVersionService projectVersionRequestService, final CodeLocationService codeLocationRequestService,
            final ScanSummaryService scanSummaryRequestService, final ScanStatusDataService scanStatusDataService) {
        this.gson = gson;
        this.logger = logger;
        this.ciEnvironmentVariables = ciEnvironmentVariables;
        this.hubVersionRequestService = hubVersionRequestService;
        this.cliDownloadService = cliDownloadService;
        this.phoneHomeDataService = phoneHomeDataService;
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.codeLocationRequestService = codeLocationRequestService;
        this.scanSummaryRequestService = scanSummaryRequestService;
        this.scanStatusDataService = scanStatusDataService;
        this.metaService = new MetaUtility(logger);
    }

    public ProjectVersionView installAndRunControlledScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest, final boolean shouldWaitForScansFinished,
            final ThirdPartyName thirdPartyName, final String thirdPartyVersion, final String pluginVersion) throws IntegrationException {
        return installAndRunControlledScan(hubServerConfig, hubScanConfig, projectRequest, shouldWaitForScansFinished, thirdPartyName.getName(), thirdPartyVersion, pluginVersion);
    }

    public ProjectVersionView installAndRunControlledScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest, final boolean shouldWaitForScansFinished, final String thirdPartyName,
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
        return version;
    }

    private SimpleScanUtility createScanService(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) {
        final HubScanConfig controlledConfig = getControlledScanConfig(hubScanConfig);
        if (hubScanConfig.isDryRun()) {
            return new SimpleScanUtility(logger, gson, hubServerConfig, hubSupportHelper, ciEnvironmentVariables, controlledConfig, projectRequest.getName(), projectRequest.getVersionRequest().getVersionName());
        } else {
            return new SimpleScanUtility(logger, gson, hubServerConfig, hubSupportHelper, ciEnvironmentVariables, controlledConfig, null, null);
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
            projectName = projectRequest.getName();
            if (projectRequest.getVersionRequest() != null) {
                projectVersionName = projectRequest.getVersionRequest().getVersionName();
                projectVersionPhase = projectRequest.getVersionRequest().getPhase() == null ? null : projectRequest.getVersionRequest().getPhase().toString();
                projectVersionDistribution = projectRequest.getVersionRequest().getDistribution() == null ? null : projectRequest.getVersionRequest().getDistribution().toString();
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

        hubSupportHelper = new HubSupportHelper();
        hubSupportHelper.checkHubSupport(hubVersionRequestService, logger);

        if (!hubScanConfig.isDryRun()) {
            getProjectVersion(projectRequest);
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
                    final String codeLocationUrl = metaService.getFirstLinkSafely(scanSummary, MetaUtility.CODE_LOCATION_BOM_STATUS_LINK);

                    final CodeLocationView codeLocationView = codeLocationRequestService.getView(codeLocationUrl, CodeLocationView.class);
                    codeLocationViews.add(codeLocationView);
                    codeLocationRequestService.mapCodeLocation(codeLocationView, version);
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
            final List<CodeLocationView> codeLocationsNotJustScanned = getCodeLocationsNotJustScanned(version, codeLocationsFromCurentScan);
            if (hubScanConfig.isDeletePreviousCodeLocations()) {
                codeLocationRequestService.deleteCodeLocations(codeLocationsNotJustScanned);
            } else if (hubScanConfig.isUnmapPreviousCodeLocations()) {
                codeLocationRequestService.unmapCodeLocations(codeLocationsNotJustScanned);
            }
        }
    }

    private List<CodeLocationView> getCodeLocationsNotJustScanned(final ProjectVersionView version, final List<CodeLocationView> codeLocationsFromCurentScan) throws IntegrationException {
        final List<CodeLocationView> codeLocationsMappedToVersion = codeLocationRequestService.getAllCodeLocationsForProjectVersion(version);
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

    private void getProjectVersion(final ProjectRequest projectRequest) throws IntegrationException {
        ProjectView project = null;
        try {
            project = projectRequestService.getProjectByName(projectRequest.getName());
        } catch (final DoesNotExistException e) {
            final String projectURL = projectRequestService.createHubProject(projectRequest);
            project = projectRequestService.getView(projectURL, ProjectView.class);
        }
        try {
            version = projectVersionRequestService.getProjectVersion(project, projectRequest.getVersionRequest().getVersionName());
        } catch (final DoesNotExistException e) {
            final String versionURL = projectVersionRequestService.createHubVersion(project, projectRequest.getVersionRequest());
            version = projectVersionRequestService.getView(versionURL, ProjectVersionView.class);
        }
    }

    private void cleanupScanSummaryFile(final File scanSummaryFile) {
        scanSummaryFile.delete();
        final File parentDirectory = scanSummaryFile.getParentFile();
        final File[] fileList = parentDirectory.listFiles();

        if (fileList != null && fileList.length == 0) {
            parentDirectory.delete();
        }
    }
}
