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
package com.blackducksoftware.integration.hub.dataservice.cli

import com.blackducksoftware.integration.exception.IntegrationException
import com.blackducksoftware.integration.hub.HubSupportHelper
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRequestService
import com.blackducksoftware.integration.hub.api.item.MetaService
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRequestService
import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder
import com.blackducksoftware.integration.hub.cli.CLIDownloadService
import com.blackducksoftware.integration.hub.cli.SimpleScanService
import com.blackducksoftware.integration.hub.dataservice.phonehome.PhoneHomeDataService
import com.blackducksoftware.integration.hub.dataservice.scan.ScanStatusDataService
import com.blackducksoftware.integration.hub.exception.DoesNotExistException
import com.blackducksoftware.integration.hub.global.HubServerConfig
import com.blackducksoftware.integration.hub.model.request.ProjectRequest
import com.blackducksoftware.integration.hub.model.view.CodeLocationView
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView
import com.blackducksoftware.integration.hub.model.view.ProjectView
import com.blackducksoftware.integration.hub.model.view.ScanSummaryView
import com.blackducksoftware.integration.hub.scan.HubScanConfig
import com.blackducksoftware.integration.hub.util.HostnameHelper
import com.blackducksoftware.integration.log.IntLogger
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBodyBuilder
import com.blackducksoftware.integration.phonehome.enums.ThirdPartyName
import com.blackducksoftware.integration.util.CIEnvironmentVariables
import com.google.gson.Gson

public class CLIDataService {
    private final Gson gson

    private final IntLogger logger

    private final CIEnvironmentVariables ciEnvironmentVariables

    private final HubVersionRequestService hubVersionRequestService

    private final CLIDownloadService cliDownloadService

    private final PhoneHomeDataService phoneHomeDataService

    private final ProjectRequestService projectRequestService

    private final ProjectVersionRequestService projectVersionRequestService

    private final CodeLocationRequestService codeLocationRequestService

    private final ScanSummaryRequestService scanSummaryRequestService

    private final ScanStatusDataService scanStatusDataService

    private final MetaService metaService

    private HubSupportHelper hubSupportHelper

    private ProjectVersionView version

    public CLIDataService(final IntLogger logger, final Gson gson, final CIEnvironmentVariables ciEnvironmentVariables,
    final HubVersionRequestService hubVersionRequestService,
    final CLIDownloadService cliDownloadService, final PhoneHomeDataService phoneHomeDataService,
    final ProjectRequestService projectRequestService, final ProjectVersionRequestService projectVersionRequestService,
    final CodeLocationRequestService codeLocationRequestService, final ScanSummaryRequestService scanSummaryRequestService,
    final ScanStatusDataService scanStatusDataService, final MetaService metaService) {
        this.gson = gson
        this.logger = logger
        this.ciEnvironmentVariables = ciEnvironmentVariables
        this.hubVersionRequestService = hubVersionRequestService
        this.cliDownloadService = cliDownloadService
        this.phoneHomeDataService = phoneHomeDataService
        this.projectRequestService = projectRequestService
        this.projectVersionRequestService = projectVersionRequestService
        this.codeLocationRequestService = codeLocationRequestService
        this.scanSummaryRequestService = scanSummaryRequestService
        this.scanStatusDataService = scanStatusDataService
        this.metaService = metaService
    }

    public ProjectVersionView installAndRunControlledScan(final HubServerConfig hubServerConfig,
            final HubScanConfig hubScanConfig, final ProjectRequest projectRequest, boolean shouldWaitForScansFinished, final ThirdPartyName thirdPartyName, final String thirdPartyVersion, final String pluginVersion)
    throws IntegrationException {
        PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = phoneHomeDataService.createInitialPhoneHomeRequestBodyBuilder()
        phoneHomeRequestBodyBuilder.setThirdPartyName(thirdPartyName)
        phoneHomeRequestBodyBuilder.setThirdPartyVersion(thirdPartyVersion)
        phoneHomeRequestBodyBuilder.setPluginVersion(pluginVersion)
        PhoneHomeRequestBody phoneHomeRequestBody = PhoneHomeRequestBody.DO_NOT_PHONE_HOME
        try{
            phoneHomeRequestBody = phoneHomeRequestBodyBuilder.build()
        }catch(Exception e){
            logger.debug(e.getMessage())
        }
        preScan(hubServerConfig, hubScanConfig, projectRequest, phoneHomeRequestBody)
        final File[] scanSummaryFiles = runControlledScan(hubServerConfig, hubScanConfig)
        postScan(hubScanConfig, scanSummaryFiles, projectRequest, shouldWaitForScansFinished)
        return version
    }

    private void printConfiguration(final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) {
        logger.alwaysLog("--> Log Level : ${logger.getLogLevel().name()}")
        logger.alwaysLog("--> Using Hub Project Name : ${projectRequest?.getName()}, Version : ${projectRequest?.getVersionRequest()?.getVersionName()}, Phase : ${projectRequest?.getVersionRequest()?.getPhase()}, Distribution : ${projectRequest?.getVersionRequest()?.getDistribution()}")
        hubScanConfig.print(logger)
    }

    private void preScan(final HubServerConfig hubServerConfig,
            final HubScanConfig hubScanConfig, final ProjectRequest projectRequest, final PhoneHomeRequestBody phoneHomeRequestBody) throws IntegrationException {
        final String localHostName = HostnameHelper.getMyHostname()
        logger.info("Running on machine : " + localHostName)
        printConfiguration(hubScanConfig, projectRequest)
        final String hubVersion = hubVersionRequestService.getHubVersion()
        cliDownloadService.performInstallation(hubScanConfig.getToolsDir(), ciEnvironmentVariables,
                hubServerConfig.getHubUrl().toString(),
                hubVersion, localHostName)
        phoneHomeDataService.phoneHome(phoneHomeRequestBody)

        hubSupportHelper = new HubSupportHelper()
        hubSupportHelper.checkHubSupport(hubVersionRequestService, logger)

        if (!hubScanConfig.isDryRun()) {
            getProjectVersion(projectRequest)
        }
    }

    private void postScan(final HubScanConfig hubScanConfig, final File[] scanSummaryFiles, final ProjectRequest projectRequest, boolean shouldWaitForScansFinished)
    throws IntegrationException {
        logger.trace("Scan is dry run ${hubScanConfig.isDryRun()}")
        if (!hubScanConfig.isDryRun()) {
            final List<CodeLocationView> codeLocationViews = new ArrayList<>()
            final List<ScanSummaryView> scanSummaries = new ArrayList<>()
            logger.trace("Found ${scanSummaryFiles.length} scan summary files")
            for(File scanSummaryFile : scanSummaryFiles) {
                ScanSummaryView scanSummary = getScanSummaryFromFile(scanSummaryFile)
                scanSummaries.add(scanSummary)
                String codeLocationUrl = metaService.getFirstLinkSafely(scanSummary, MetaService.CODE_LOCATION_BOM_STATUS_LINK)

                final CodeLocationView codeLocationView = codeLocationRequestService.getItem(codeLocationUrl, CodeLocationView.class)
                codeLocationViews.add(codeLocationView)
                codeLocationRequestService.mapCodeLocation(codeLocationView, version)
            }
            cleanupCodeLocations(codeLocationViews, hubScanConfig)
            if (shouldWaitForScansFinished) {
                logger.debug("Waiting for the Bom to be updated.")
                scanStatusDataService.assertBomImportScansFinished(scanSummaries)
            }
        }
    }

    private ScanSummaryView getScanSummaryFromFile(File scanSummaryFile) {
        String scanSummaryJson = scanSummaryFile.text
        ScanSummaryView scanSummaryView = gson.fromJson(scanSummaryJson, ScanSummaryView.class)
        scanSummaryView.json = scanSummaryJson
        return scanSummaryView
    }

    private File[] runControlledScan(final HubServerConfig hubServerConfig,
            final HubScanConfig hubScanConfig) throws IntegrationException {
        final SimpleScanService simpleScanService = new SimpleScanService(logger, gson, hubServerConfig, hubSupportHelper,
                ciEnvironmentVariables, getControlledScanConfig(hubScanConfig), null, null)
        simpleScanService.setupAndExecuteScan()
        if (hubScanConfig.isCleanupLogsOnSuccess()) {
            cleanUpLogFiles(simpleScanService)
        }
        return simpleScanService.getScanSummaryFiles()
    }

    private HubScanConfig getControlledScanConfig(final HubScanConfig originalHubScanConfig) {
        final HubScanConfigBuilder builder = new HubScanConfigBuilder()
        builder.setCodeLocationAlias(originalHubScanConfig.getCodeLocationAlias())
        builder.setVerbose(originalHubScanConfig.isVerbose())
        builder.setDryRun(originalHubScanConfig.isDryRun())
        builder.setExcludePatterns(originalHubScanConfig.getExcludePatterns())
        builder.setScanMemory(originalHubScanConfig.getScanMemory())
        builder.setToolsDir(originalHubScanConfig.getToolsDir())
        builder.setWorkingDirectory(originalHubScanConfig.getWorkingDirectory())
        builder.addAllScanTargetPaths(new ArrayList<>(originalHubScanConfig.getScanTargetPaths()))
        return builder.build()
    }

    private void cleanUpLogFiles(final SimpleScanService simpleScanService) {
        final File standardOutputFile = simpleScanService.getStandardOutputFile()
        if (standardOutputFile != null && standardOutputFile.exists()) {
            standardOutputFile.delete()
        }
        final File cliLogDirectory = simpleScanService.getCLILogDirectory()
        if (cliLogDirectory != null && cliLogDirectory.exists()) {
            for (final File log : cliLogDirectory.listFiles()) {
                log.delete()
            }
            cliLogDirectory.delete()
        }
    }

    private void cleanupCodeLocations(final List<CodeLocationView> codeLocationsFromCurentScan, final HubScanConfig hubScanConfig) throws IntegrationException {
        if (hubScanConfig.isDeletePreviousCodeLocations() || hubScanConfig.isUnmapPreviousCodeLocations()) {
            final List<CodeLocationView> codeLocationsNotJustScanned = getCodeLocationsNotJustScanned(version, codeLocationsFromCurentScan)
            if (hubScanConfig.isDeletePreviousCodeLocations()) {
                codeLocationRequestService.deleteCodeLocations(codeLocationsNotJustScanned)
            } else if (hubScanConfig.isUnmapPreviousCodeLocations()) {
                codeLocationRequestService.unmapCodeLocations(codeLocationsNotJustScanned)
            }
        }
    }

    private List<CodeLocationView> getCodeLocationsNotJustScanned(final ProjectVersionView version,
            final List<CodeLocationView> codeLocationsFromCurentScan) throws IntegrationException {
        final List<CodeLocationView> codeLocationsMappedToVersion = codeLocationRequestService.getAllCodeLocationsForProjectVersion(version)
        return getCodeLocationsNotJustScanned(codeLocationsMappedToVersion, codeLocationsFromCurentScan)
    }

    private List<CodeLocationView> getCodeLocationsNotJustScanned(final List<CodeLocationView> codeLocationsMappedToVersion,
            final List<CodeLocationView> codeLocationsFromCurentScan) {
        final List<CodeLocationView> codeLocationsNotJustScanned = new ArrayList<>()
        for (final CodeLocationView codeLocationItemMappedToVersion : codeLocationsMappedToVersion) {
            boolean partOfCurrentScan = false
            for (final CodeLocationView codeLocationFromCurentScan : codeLocationsFromCurentScan) {
                if (codeLocationItemMappedToVersion.url.equals(codeLocationFromCurentScan.url)) {
                    partOfCurrentScan = true
                    break
                }
            }
            if (!partOfCurrentScan) {
                codeLocationsNotJustScanned.add(codeLocationItemMappedToVersion)
            }
        }
        return codeLocationsNotJustScanned
    }

    private void getProjectVersion( final ProjectRequest projectRequest) throws IntegrationException {
        ProjectView project = null
        try {
            project = projectRequestService.getProjectByName(projectRequest.getName())
        } catch (final DoesNotExistException e) {
            final String projectURL = projectRequestService.createHubProject(projectRequest)
            project = projectRequestService.getItem(projectURL, ProjectView.class)
        }
        try {
            version = projectVersionRequestService.getProjectVersion(project, projectRequest.getVersionRequest().getVersionName())
        } catch (final DoesNotExistException e) {
            final String versionURL = projectVersionRequestService.createHubVersion(project, projectRequest.getVersionRequest())
            version = projectVersionRequestService.getItem(versionURL, ProjectVersionView.class)
        }
    }
}
