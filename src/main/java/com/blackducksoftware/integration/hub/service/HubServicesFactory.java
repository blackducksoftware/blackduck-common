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
package com.blackducksoftware.integration.hub.service;

import java.util.Map;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.aggregate.bom.AggregateBomService;
import com.blackducksoftware.integration.hub.api.bom.BomComponentIssueService;
import com.blackducksoftware.integration.hub.api.bom.BomImportService;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationService;
import com.blackducksoftware.integration.hub.api.component.ComponentService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionConfigService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionUserOptionService;
import com.blackducksoftware.integration.hub.api.group.GroupService;
import com.blackducksoftware.integration.hub.api.license.LicenseService;
import com.blackducksoftware.integration.hub.api.matchedfiles.MatchedFilesService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubRegistrationService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionService;
import com.blackducksoftware.integration.hub.api.notification.NotificationService;
import com.blackducksoftware.integration.hub.api.policy.PolicyService;
import com.blackducksoftware.integration.hub.api.project.ProjectAssignmentService;
import com.blackducksoftware.integration.hub.api.project.ProjectService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionService;
import com.blackducksoftware.integration.hub.api.report.ReportService;
import com.blackducksoftware.integration.hub.api.scan.DryRunUploadService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryService;
import com.blackducksoftware.integration.hub.api.user.UserService;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityService;
import com.blackducksoftware.integration.hub.api.vulnerablebomcomponent.VulnerableBomComponentService;
import com.blackducksoftware.integration.hub.cli.CLIDownloadUtility;
import com.blackducksoftware.integration.hub.cli.SimpleScanUtility;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.component.ComponentDataService;
import com.blackducksoftware.integration.hub.dataservice.extension.ExtensionConfigDataService;
import com.blackducksoftware.integration.hub.dataservice.license.LicenseDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.phonehome.PhoneHomeDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.dataservice.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.user.UserDataService;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.VersionBomComponentDataService;
import com.blackducksoftware.integration.hub.dataservice.vulnerability.VulnerabilityDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.phonehome.PhoneHomeClient;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;
import com.blackducksoftware.integration.util.IntegrationEscapeUtil;

public class HubServicesFactory {
    private final CIEnvironmentVariables ciEnvironmentVariables;
    private final RestConnection restConnection;

    public HubServicesFactory(final RestConnection restConnection) {
        this.ciEnvironmentVariables = new CIEnvironmentVariables();
        ciEnvironmentVariables.putAll(System.getenv());

        this.restConnection = restConnection;
    }

    public void addEnvironmentVariable(final String key, final String value) {
        ciEnvironmentVariables.put(key, value);
    }

    public void addEnvironmentVariables(final Map<String, String> environmentVariables) {
        ciEnvironmentVariables.putAll(environmentVariables);
    }

    public CLIDataService createCLIDataService() {
        return createCLIDataService(120000l);
    }

    public CLIDataService createCLIDataService(final long timeoutInMilliseconds) {
        return new CLIDataService(restConnection.logger, restConnection.gson, ciEnvironmentVariables, createHubVersionRequestService(), createCliDownloadService(), createPhoneHomeDataService(), createProjectRequestService(),
                createProjectVersionRequestService(), createCodeLocationRequestService(), createScanSummaryRequestService(), createScanStatusDataService(timeoutInMilliseconds));
    }

    public PhoneHomeDataService createPhoneHomeDataService() {
        return new PhoneHomeDataService(restConnection.logger, createPhoneHomeClient(), createHubRegistrationRequestService(), createHubVersionRequestService());
    }

    public PhoneHomeClient createPhoneHomeClient() {
        return new PhoneHomeClient(restConnection.logger, restConnection.timeout, restConnection.getProxyInfo(), restConnection.alwaysTrustServerCertificate);
    }

    public RiskReportDataService createRiskReportDataService(final long timeoutInMilliseconds) throws IntegrationException {
        return new RiskReportDataService(restConnection.logger, restConnection, createProjectRequestService(), createProjectVersionRequestService(), createReportRequestService(timeoutInMilliseconds), createAggregateBomRequestService(),
                createCheckedHubSupport(), createIntegrationEscapeUtil());
    }

    public PolicyStatusDataService createPolicyStatusDataService() {
        return new PolicyStatusDataService(restConnection, createProjectRequestService(), createProjectVersionRequestService());
    }

    public ScanStatusDataService createScanStatusDataService(final long timeoutInMilliseconds) {
        return new ScanStatusDataService(restConnection.logger, createProjectRequestService(), createProjectVersionRequestService(), createCodeLocationRequestService(), createScanSummaryRequestService(), timeoutInMilliseconds);
    }

    public NotificationDataService createNotificationDataService() {
        return new NotificationDataService(restConnection.logger, createHubResponseService(), createNotificationRequestService(), createProjectVersionRequestService(), createPolicyRequestService());
    }

    public NotificationDataService createNotificationDataService(final PolicyNotificationFilter policyNotificationFilter) {
        return new NotificationDataService(restConnection.logger, createHubResponseService(), createNotificationRequestService(), createProjectVersionRequestService(), createPolicyRequestService(), policyNotificationFilter);
    }

    public ExtensionConfigDataService createExtensionConfigDataService() {
        return new ExtensionConfigDataService(restConnection.logger, restConnection, createUserRequestService(), createExtensionConfigRequestService(), createExtensionUserOptionRequestService());
    }

    public VulnerabilityDataService createVulnerabilityDataService() {
        return new VulnerabilityDataService(restConnection.logger, createComponentRequestService(), createVulnerabilityRequestService());
    }

    public LicenseDataService createLicenseDataService() {
        return new LicenseDataService(createComponentRequestService(), createLicenseRequestService());
    }

    public BomImportService createBomImportRequestService() {
        return new BomImportService(restConnection);
    }

    public DryRunUploadService createDryRunUploadRequestService() {
        return new DryRunUploadService(restConnection);
    }

    public CodeLocationService createCodeLocationRequestService() {
        return new CodeLocationService(restConnection);
    }

    public ComponentService createComponentRequestService() {
        return new ComponentService(restConnection);
    }

    public HubVersionService createHubVersionRequestService() {
        return new HubVersionService(restConnection);
    }

    public NotificationService createNotificationRequestService() {
        return new NotificationService(restConnection);
    }

    public PolicyService createPolicyRequestService() {
        return new PolicyService(restConnection);
    }

    public ProjectService createProjectRequestService() {
        return new ProjectService(restConnection);
    }

    public ProjectVersionService createProjectVersionRequestService() {
        return new ProjectVersionService(restConnection);
    }

    public LicenseService createLicenseRequestService() {
        return new LicenseService(restConnection);
    }

    public ScanSummaryService createScanSummaryRequestService() {
        return new ScanSummaryService(restConnection);
    }

    public UserService createUserRequestService() {
        return new UserService(restConnection);
    }

    public GroupService createGroupRequestService() {
        return new GroupService(restConnection);
    }

    public VulnerabilityService createVulnerabilityRequestService() {
        return new VulnerabilityService(restConnection);
    }

    public ExtensionConfigService createExtensionConfigRequestService() {
        return new ExtensionConfigService(restConnection);
    }

    public ExtensionUserOptionService createExtensionUserOptionRequestService() {
        return new ExtensionUserOptionService(restConnection);
    }

    public VulnerableBomComponentService createVulnerableBomComponentRequestService() {
        return new VulnerableBomComponentService(restConnection);
    }

    public MatchedFilesService createMatchedFilesRequestService() {
        return new MatchedFilesService(restConnection);
    }

    public CLIDownloadUtility createCliDownloadService() {
        return new CLIDownloadUtility(restConnection.logger, restConnection);
    }

    public IntegrationEscapeUtil createIntegrationEscapeUtil() {
        return new IntegrationEscapeUtil();
    }

    public SimpleScanUtility createSimpleScanService(final RestConnection restConnection, final HubServerConfig hubServerConfig, final HubSupportHelper hubSupportHelper, final HubScanConfig hubScanConfig, final String projectName,
            final String versionName) {
        return new SimpleScanUtility(restConnection.logger, restConnection.gson, hubServerConfig, hubSupportHelper, ciEnvironmentVariables, hubScanConfig, projectName, versionName);
    }

    public HubRegistrationService createHubRegistrationRequestService() {
        return new HubRegistrationService(restConnection);
    }

    public ReportService createReportRequestService(final long timeoutInMilliseconds) {
        return new ReportService(restConnection, restConnection.logger, timeoutInMilliseconds);
    }

    public AggregateBomService createAggregateBomRequestService() {
        return new AggregateBomService(restConnection);
    }

    public HubService createHubResponseService() {
        return new HubService(restConnection);
    }

    public ProjectAssignmentService createProjectAssignmentRequestService() {
        return new ProjectAssignmentService(restConnection);
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public HubSupportHelper createCheckedHubSupport() throws IntegrationException {
        final HubSupportHelper supportHelper = new HubSupportHelper();
        supportHelper.checkHubSupport(createHubVersionRequestService(), restConnection.logger);
        return supportHelper;
    }

    public ComponentDataService createComponentDataService() {
        return new ComponentDataService(restConnection.logger, createProjectRequestService(), createProjectVersionRequestService(), createComponentRequestService());
    }

    public BomComponentIssueService createBomComponentIssueRequestService() {
        return new BomComponentIssueService(restConnection);
    }

    public ProjectDataService createProjectDataService() {
        return new ProjectDataService(restConnection, createProjectRequestService(), createProjectVersionRequestService(), createProjectAssignmentRequestService());
    }

    public VersionBomComponentDataService createVersionBomComponentDataService() {
        return new VersionBomComponentDataService(restConnection.logger, createProjectRequestService(), createProjectVersionRequestService(), createAggregateBomRequestService(), createMatchedFilesRequestService());
    }

    public UserDataService createUserDataService() {
        return new UserDataService(restConnection.logger, createProjectRequestService(), createUserRequestService());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
