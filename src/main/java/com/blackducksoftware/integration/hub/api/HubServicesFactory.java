/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api;

import java.io.File;
import java.util.List;

import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.bom.BomImportRestService;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRestService;
import com.blackducksoftware.integration.hub.api.component.ComponentRestService;
import com.blackducksoftware.integration.hub.api.component.id.ComponentIdRestService;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionConfigRestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionRestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionUserOptionRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusRestService;
import com.blackducksoftware.integration.hub.api.project.ProjectRestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.report.ReportRestService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.api.user.UserRestService;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.vulnerabilities.VulnerabilityRestService;
import com.blackducksoftware.integration.hub.api.vulnerableBomComponent.VulnerableBomComponentRestService;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.cli.SimpleScanService;
import com.blackducksoftware.integration.hub.dataservices.extension.ExtensionConfigDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.vulnerability.VulnerabilityDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class HubServicesFactory {
    private final RestConnection restConnection;

    public HubServicesFactory(final RestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public PolicyStatusDataService createPolicyStatusDataService() {
        return new PolicyStatusDataService(restConnection, createProjectRestService(),
                createProjectVersionRestService(), createPolicyStatusRestService());
    }

    public ScanStatusDataService createScanStatusDataService() {
        return new ScanStatusDataService(restConnection, createProjectRestService(), createProjectVersionRestService(),
                createCodeLocationRestService(), createScanSummaryRestService());
    }

    public NotificationDataService createNotificationDataService(final IntLogger logger) {
        return new NotificationDataService(logger, restConnection, createNotificationRestService(), createProjectVersionRestService(),
                createPolicyRestService(), createVersionBomPolicyRestService(), createComponentVersionRestService());
    }

    public NotificationDataService createNotificationDataService(final IntLogger logger,
            final PolicyNotificationFilter policyNotificationFilter) {
        return new NotificationDataService(logger, restConnection, createNotificationRestService(), createProjectVersionRestService(),
                createPolicyRestService(), createVersionBomPolicyRestService(), createComponentVersionRestService(), policyNotificationFilter);
    }

    public ExtensionConfigDataService createExtensionConfigDataService(final IntLogger logger) {
        return new ExtensionConfigDataService(logger, restConnection, createUserRestService(),
                createExtensionRestService(), createExtensionConfigRestService(), createExtensionUserOptionRestService());
    }

    public VulnerabilityDataService createVulnerabilityDataService() {
        return new VulnerabilityDataService(restConnection, createComponentRestService(), createComponentVersionRestService(), createComponentIdRestService(),
                createVulnerabilityRestService());
    }

    public BomImportRestService createBomImportRestService() {
        return new BomImportRestService(restConnection);
    }

    public CodeLocationRestService createCodeLocationRestService() {
        return new CodeLocationRestService(restConnection);
    }

    public ComponentIdRestService createComponentIdRestService() {
        return new ComponentIdRestService(restConnection);
    }

    public ComponentRestService createComponentRestService() {
        return new ComponentRestService(restConnection);
    }

    public ComponentVersionRestService createComponentVersionRestService() {
        return new ComponentVersionRestService(restConnection);
    }

    public HubVersionRestService createHubVersionRestService() {
        return new HubVersionRestService(restConnection);
    }

    public NotificationRestService createNotificationRestService() {
        return new NotificationRestService(restConnection);
    }

    public PolicyRestService createPolicyRestService() {
        return new PolicyRestService(restConnection);
    }

    public PolicyStatusRestService createPolicyStatusRestService() {
        return new PolicyStatusRestService(restConnection);
    }

    public ProjectRestService createProjectRestService() {
        return new ProjectRestService(restConnection);
    }

    public ProjectVersionRestService createProjectVersionRestService() {
        return new ProjectVersionRestService(restConnection);
    }

    public ScanSummaryRestService createScanSummaryRestService() {
        return new ScanSummaryRestService(restConnection);
    }

    public UserRestService createUserRestService() {
        return new UserRestService(restConnection);
    }

    public VersionBomPolicyRestService createVersionBomPolicyRestService() {
        return new VersionBomPolicyRestService(restConnection);
    }

    public VulnerabilityRestService createVulnerabilityRestService() {
        return new VulnerabilityRestService(restConnection);
    }

    public ExtensionRestService createExtensionRestService() {
        return new ExtensionRestService(restConnection);
    }

    public ExtensionConfigRestService createExtensionConfigRestService() {
        return new ExtensionConfigRestService(restConnection);
    }

    public ExtensionUserOptionRestService createExtensionUserOptionRestService() {
        return new ExtensionUserOptionRestService(restConnection);
    }

    public VulnerableBomComponentRestService createVulnerableBomComponentRestService() {
        return new VulnerableBomComponentRestService(restConnection);
    }

    public CLIDownloadService createCliDownloadService(IntLogger logger) {
        return new CLIDownloadService(logger);
    }

    public SimpleScanService createSimpleScanService(IntLogger logger, RestConnection restConnection, HubServerConfig hubServerConfig,
            HubSupportHelper hubSupportHelper,
            CIEnvironmentVariables ciEnvironmentVariables, final File directoryToInstallTo, int scanMemory, boolean verboseRun, boolean dryRun, String project,
            String version, List<String> scanTargetPaths, String workingDirectoryPath) {
        return new SimpleScanService(logger, restConnection, hubServerConfig, hubSupportHelper, ciEnvironmentVariables, directoryToInstallTo, scanMemory,
                verboseRun, dryRun, project, version, scanTargetPaths, workingDirectoryPath);
    }

    public HubRegistrationRestService createHubRegistrationRestService() {
        return new HubRegistrationRestService(restConnection);
    }

    public ReportRestService createReportRestService(IntLogger logger) {
        return new ReportRestService(restConnection, logger);
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

}
