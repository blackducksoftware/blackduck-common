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
package com.blackducksoftware.integration.hub.service;

import java.io.File;
import java.util.List;

import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.bom.BomImportRequestService;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRequestService;
import com.blackducksoftware.integration.hub.api.component.ComponentRequestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionConfigRequestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionRequestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionUserOptionRequestService;
import com.blackducksoftware.integration.hub.api.item.HubItemFilter;
import com.blackducksoftware.integration.hub.api.nonpublic.HubRegistrationRequestService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusRequestService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.report.ReportRequestService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRequestService;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRequestService;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityRequestService;
import com.blackducksoftware.integration.hub.api.vulnerableBomComponent.VulnerableBomComponentRequestService;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.cli.SimpleScanService;
import com.blackducksoftware.integration.hub.dataservice.extension.ExtensionConfigDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.dataservice.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.vulnerability.VulnerabilityDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class HubServicesFactory {
    private final RestConnection restConnection;

    private final HubItemFilter hubItemFilter;

    public HubServicesFactory(final RestConnection restConnection) {
        this.restConnection = restConnection;
        this.hubItemFilter = new HubItemFilter();
    }

    public HubRequestService createHubRequestService() {
        return new HubRequestService(restConnection);
    }

    public RiskReportDataService createRiskReportDataService(final IntLogger logger) {
        return new RiskReportDataService(restConnection, createProjectRequestService(),
                createProjectVersionRequestService(), createReportRequestService(logger));
    }

    public PolicyStatusDataService createPolicyStatusDataService() {
        return new PolicyStatusDataService(restConnection, createProjectRequestService(),
                createProjectVersionRequestService(), createPolicyStatusRequestService());
    }

    public ScanStatusDataService createScanStatusDataService() {
        return new ScanStatusDataService(restConnection, createProjectRequestService(), createProjectVersionRequestService(),
                createCodeLocationRequestService(), createScanSummaryRequestService());
    }

    public NotificationDataService createNotificationDataService(final IntLogger logger) {
        return new NotificationDataService(logger, restConnection, createNotificationRequestService(), createProjectVersionRequestService(),
                createPolicyRequestService(), createVersionBomPolicyRequestService(), createHubRequestService());
    }

    public NotificationDataService createNotificationDataService(final IntLogger logger,
            final PolicyNotificationFilter policyNotificationFilter) {
        return new NotificationDataService(logger, restConnection, createNotificationRequestService(), createProjectVersionRequestService(),
                createPolicyRequestService(), createVersionBomPolicyRequestService(), createHubRequestService(), policyNotificationFilter);
    }

    public ExtensionConfigDataService createExtensionConfigDataService(final IntLogger logger) {
        return new ExtensionConfigDataService(logger, restConnection, createUserRequestService(),
                createExtensionRequestService(), createExtensionConfigRequestService(), createExtensionUserOptionRequestService());
    }

    public VulnerabilityDataService createVulnerabilityDataService() {
        return new VulnerabilityDataService(restConnection, createComponentRequestService(), createHubRequestService(),
                createVulnerabilityRequestService());
    }

    public BomImportRequestService createBomImportRequestService() {
        return new BomImportRequestService(restConnection);
    }

    public CodeLocationRequestService createCodeLocationRequestService() {
        return new CodeLocationRequestService(restConnection);
    }

    public ComponentRequestService createComponentRequestService() {
        return new ComponentRequestService(restConnection);
    }

    public HubVersionRequestService createHubVersionRequestService() {
        return new HubVersionRequestService(restConnection);
    }

    public NotificationRequestService createNotificationRequestService() {
        return new NotificationRequestService(restConnection);
    }

    public PolicyRequestService createPolicyRequestService() {
        return new PolicyRequestService(restConnection);
    }

    public PolicyStatusRequestService createPolicyStatusRequestService() {
        return new PolicyStatusRequestService(restConnection);
    }

    public ProjectRequestService createProjectRequestService() {
        return new ProjectRequestService(restConnection);
    }

    public ProjectVersionRequestService createProjectVersionRequestService() {
        return new ProjectVersionRequestService(restConnection);
    }

    public ScanSummaryRequestService createScanSummaryRequestService() {
        return new ScanSummaryRequestService(restConnection);
    }

    public UserRequestService createUserRequestService() {
        return new UserRequestService(restConnection);
    }

    public VersionBomPolicyRequestService createVersionBomPolicyRequestService() {
        return new VersionBomPolicyRequestService(restConnection);
    }

    public VulnerabilityRequestService createVulnerabilityRequestService() {
        return new VulnerabilityRequestService(restConnection);
    }

    public ExtensionRequestService createExtensionRequestService() {
        return new ExtensionRequestService(restConnection);
    }

    public ExtensionConfigRequestService createExtensionConfigRequestService() {
        return new ExtensionConfigRequestService(restConnection);
    }

    public ExtensionUserOptionRequestService createExtensionUserOptionRequestService() {
        return new ExtensionUserOptionRequestService(restConnection);
    }

    public VulnerableBomComponentRequestService createVulnerableBomComponentRequestService() {
        return new VulnerableBomComponentRequestService(restConnection);
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

    public HubRegistrationRequestService createHubRegistrationRequestService() {
        return new HubRegistrationRequestService(restConnection);
    }

    public ReportRequestService createReportRequestService(IntLogger logger) {
        return new ReportRequestService(restConnection, logger);
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

}
