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
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRequestService;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationView;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.api.project.ProjectView;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryView;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.cli.SimpleScanService;
import com.blackducksoftware.integration.hub.dataservice.phonehome.PhoneHomeDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.phonehome.IntegrationInfo;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;
import com.google.gson.Gson;

public class CLIDataService {
    private final Gson gson;

    private final IntLogger logger;

    private final CIEnvironmentVariables ciEnvironmentVariables;

    private final HubVersionRequestService hubVersionRequestService;

    private final CLIDownloadService cliDownloadService;

    private final PhoneHomeDataService phoneHomeDataService;

    private final ProjectRequestService projectRequestService;

    private final ProjectVersionRequestService projectVersionRequestService;

    private final CodeLocationRequestService codeLocationRequestService;

    private final MetaService metaService;

    public CLIDataService(final IntLogger logger, final Gson gson, final CIEnvironmentVariables ciEnvironmentVariables,
            final HubVersionRequestService hubVersionRequestService,
            final CLIDownloadService cliDownloadService, final PhoneHomeDataService phoneHomeDataService,
            final ProjectRequestService projectRequestService, final ProjectVersionRequestService projectVersionRequestService,
            final CodeLocationRequestService codeLocationRequestService, final MetaService metaService) {
        this.gson = gson;
        this.logger = logger;
        this.ciEnvironmentVariables = ciEnvironmentVariables;
        this.hubVersionRequestService = hubVersionRequestService;
        this.cliDownloadService = cliDownloadService;
        this.phoneHomeDataService = phoneHomeDataService;
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.codeLocationRequestService = codeLocationRequestService;
        this.metaService = metaService;
    }

    public List<ScanSummaryView> installAndRunScan(final HubServerConfig hubServerConfig,
            final HubScanConfig hubScanConfig, final IntegrationInfo integrationInfo)
            throws IntegrationException {
        final String localHostName = HostnameHelper.getMyHostname();
        logger.info("Running on machine : " + localHostName);
        printConfiguration(hubScanConfig);
        final String hubVersion = hubVersionRequestService.getHubVersion();
        cliDownloadService.performInstallation(hubScanConfig.getToolsDir(), ciEnvironmentVariables,
                hubServerConfig.getHubUrl().toString(),
                hubVersion, localHostName);

        phoneHomeDataService.phoneHome(hubServerConfig, integrationInfo, hubVersion);

        final HubSupportHelper hubSupportHelper = new HubSupportHelper();
        hubSupportHelper.checkHubSupport(hubVersionRequestService, logger);
        final SimpleScanService simpleScanService = new SimpleScanService(logger, gson, hubServerConfig, hubSupportHelper,
                ciEnvironmentVariables, hubScanConfig);
        simpleScanService.setupAndExecuteScan();

        if (hubScanConfig.isCleanupLogsOnSuccess()) {
            cleanUpLogFiles(simpleScanService);
        }
        final List<ScanSummaryView> scanSummaries = simpleScanService.getScanSummaryItems();
        cleanupCodeLocations(scanSummaries, hubScanConfig);
        return scanSummaries;
    }

    public void printConfiguration(final HubScanConfig hubScanConfig) {
        logger.alwaysLog("--> Log Level : " + logger.getLogLevel().name());
        hubScanConfig.print(logger);
    }

    private void cleanUpLogFiles(final SimpleScanService simpleScanService) {
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

    private void cleanupCodeLocations(final List<ScanSummaryView> scans, final HubScanConfig hubScanConfig) throws IntegrationException {
        if (hubScanConfig.isDeletePreviousCodeLocations() || hubScanConfig.isUnmapPreviousCodeLocations()) {
            final ProjectView project = projectRequestService.getProjectByName(hubScanConfig.getProjectName());
            final ProjectVersionView version = projectVersionRequestService.getProjectVersion(project, hubScanConfig.getVersion());
            final List<CodeLocationView> codeLocationsFromCurentScan = getCodeLocationsFromScanSummaries(scans);

            final List<CodeLocationView> codeLocationsNotJustScanned = getCodeLocationsNotJustScanned(version, codeLocationsFromCurentScan);
            if (hubScanConfig.isDeletePreviousCodeLocations()) {
                codeLocationRequestService.deleteCodeLocations(codeLocationsNotJustScanned);
            } else if (hubScanConfig.isUnmapPreviousCodeLocations()) {
                codeLocationRequestService.unmapCodeLocations(codeLocationsNotJustScanned);
            }
        }
    }

    private List<CodeLocationView> getCodeLocationsFromScanSummaries(final List<ScanSummaryView> scans) throws IntegrationException {
        final List<CodeLocationView> codeLocations = new ArrayList<>();
        for (final ScanSummaryView scan : scans) {
            final CodeLocationView codeLocation = codeLocationRequestService
                    .getItem(metaService.getFirstLink(scan, MetaService.CODE_LOCATION_BOM_STATUS_LINK), CodeLocationView.class);
            codeLocations.add(codeLocation);
        }
        return codeLocations;
    }

    private List<CodeLocationView> getCodeLocationsNotJustScanned(final ProjectVersionView version,
            final List<CodeLocationView> codeLocationsFromCurentScan) throws IntegrationException {
        final List<CodeLocationView> codeLocationsMappedToVersion = codeLocationRequestService.getAllCodeLocationsForProjectVersion(version);
        return getCodeLocationsNotJustScanned(codeLocationsMappedToVersion, codeLocationsFromCurentScan);
    }

    private List<CodeLocationView> getCodeLocationsNotJustScanned(final List<CodeLocationView> codeLocationsMappedToVersion,
            final List<CodeLocationView> codeLocationsFromCurentScan) {
        final List<CodeLocationView> codeLocationsNotJustScanned = new ArrayList<>();
        for (final CodeLocationView codeLocationItemMappedToVersion : codeLocationsMappedToVersion) {
            boolean partOfCurrentScan = false;
            for (final CodeLocationView codeLocationFromCurentScan : codeLocationsFromCurentScan) {
                if (codeLocationItemMappedToVersion.getUrl().equals(codeLocationFromCurentScan.getUrl())) {
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
