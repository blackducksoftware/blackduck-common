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
import com.blackducksoftware.integration.hub.api.aggregate.bom.AggregateBomRequestService;
import com.blackducksoftware.integration.hub.api.bom.BomComponentIssueRequestService;
import com.blackducksoftware.integration.hub.api.bom.BomImportRequestService;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRequestService;
import com.blackducksoftware.integration.hub.api.component.ComponentRequestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionConfigRequestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionUserOptionRequestService;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.matchedfiles.MatchedFilesRequestService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubRegistrationRequestService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.project.ProjectAssignmentRequestService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.report.ReportRequestService;
import com.blackducksoftware.integration.hub.api.scan.DryRunUploadRequestService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRequestService;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityRequestService;
import com.blackducksoftware.integration.hub.api.vulnerablebomcomponent.VulnerableBomComponentRequestService;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.cli.SimpleScanService;
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
                createProjectVersionRequestService(), createCodeLocationRequestService(), createScanSummaryRequestService(), createScanStatusDataService(timeoutInMilliseconds), createMetaService());
    }

    public PhoneHomeDataService createPhoneHomeDataService() {
        return new PhoneHomeDataService(restConnection.logger, createPhoneHomeClient(), createHubRegistrationRequestService(), createHubVersionRequestService());
    }

    public PhoneHomeClient createPhoneHomeClient() {
        return new PhoneHomeClient(restConnection.logger, restConnection.timeout, restConnection.getProxyInfo(), restConnection.alwaysTrustServerCertificate);
    }

    public RiskReportDataService createRiskReportDataService(final long timeoutInMilliseconds) throws IntegrationException {
        return new RiskReportDataService(restConnection.logger, restConnection, createProjectRequestService(), createProjectVersionRequestService(), createReportRequestService(timeoutInMilliseconds), createAggregateBomRequestService(),
                createMetaService(), createCheckedHubSupport(), createIntegrationEscapeUtil());
    }

    public PolicyStatusDataService createPolicyStatusDataService() {
        return new PolicyStatusDataService(restConnection, createProjectRequestService(), createProjectVersionRequestService(), createMetaService());
    }

    public ScanStatusDataService createScanStatusDataService(final long timeoutInMilliseconds) {
        return new ScanStatusDataService(restConnection.logger, createProjectRequestService(), createProjectVersionRequestService(), createCodeLocationRequestService(), createScanSummaryRequestService(), createMetaService(),
                timeoutInMilliseconds);
    }

    public NotificationDataService createNotificationDataService() {
        return new NotificationDataService(restConnection.logger, createHubResponseService(), createNotificationRequestService(), createProjectVersionRequestService(), createPolicyRequestService(), createMetaService());
    }

    public NotificationDataService createNotificationDataService(final PolicyNotificationFilter policyNotificationFilter) {
        return new NotificationDataService(restConnection.logger, createHubResponseService(), createNotificationRequestService(), createProjectVersionRequestService(), createPolicyRequestService(), policyNotificationFilter,
                createMetaService());
    }

    public ExtensionConfigDataService createExtensionConfigDataService() {
        return new ExtensionConfigDataService(restConnection.logger, restConnection, createUserRequestService(), createExtensionConfigRequestService(), createExtensionUserOptionRequestService(), createMetaService());
    }

    public VulnerabilityDataService createVulnerabilityDataService() {
        return new VulnerabilityDataService(restConnection, createComponentRequestService(), createVulnerabilityRequestService(), createMetaService());
    }

    public LicenseDataService createLicenseDataService() {
        return new LicenseDataService(createComponentRequestService());
    }

    public BomImportRequestService createBomImportRequestService() {
        return new BomImportRequestService(restConnection);
    }

    public DryRunUploadRequestService createDryRunUploadRequestService() {
        return new DryRunUploadRequestService(restConnection);
    }

    public CodeLocationRequestService createCodeLocationRequestService() {
        return new CodeLocationRequestService(restConnection, createMetaService());
    }

    public ComponentRequestService createComponentRequestService() {
        return new ComponentRequestService(restConnection);
    }

    public HubVersionRequestService createHubVersionRequestService() {
        return new HubVersionRequestService(restConnection);
    }

    public NotificationRequestService createNotificationRequestService() {
        return new NotificationRequestService(restConnection.logger, restConnection, createMetaService());
    }

    public PolicyRequestService createPolicyRequestService() {
        return new PolicyRequestService(restConnection);
    }

    public ProjectRequestService createProjectRequestService() {
        return new ProjectRequestService(restConnection, createMetaService());
    }

    public ProjectVersionRequestService createProjectVersionRequestService() {
        return new ProjectVersionRequestService(restConnection, createMetaService());
    }

    public ScanSummaryRequestService createScanSummaryRequestService() {
        return new ScanSummaryRequestService(restConnection);
    }

    public UserRequestService createUserRequestService() {
        return new UserRequestService(restConnection);
    }

    public VulnerabilityRequestService createVulnerabilityRequestService() {
        return new VulnerabilityRequestService(restConnection);
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

    public MatchedFilesRequestService createMatchedFilesRequestService() {
        return new MatchedFilesRequestService(restConnection);
    }

    public CLIDownloadService createCliDownloadService() {
        return new CLIDownloadService(restConnection.logger, restConnection);
    }

    public IntegrationEscapeUtil createIntegrationEscapeUtil() {
        return new IntegrationEscapeUtil();
    }

    public SimpleScanService createSimpleScanService(final RestConnection restConnection, final HubServerConfig hubServerConfig, final HubSupportHelper hubSupportHelper, final HubScanConfig hubScanConfig, final String projectName,
            final String versionName) {
        return new SimpleScanService(restConnection.logger, restConnection.gson, hubServerConfig, hubSupportHelper, ciEnvironmentVariables, hubScanConfig, projectName, versionName);
    }

    public HubRegistrationRequestService createHubRegistrationRequestService() {
        return new HubRegistrationRequestService(restConnection);
    }

    public ReportRequestService createReportRequestService(final long timeoutInMilliseconds) {
        return new ReportRequestService(restConnection, restConnection.logger, createMetaService(), timeoutInMilliseconds);
    }

    public AggregateBomRequestService createAggregateBomRequestService() {
        return new AggregateBomRequestService(restConnection, createMetaService());
    }

    public MetaService createMetaService() {
        return new MetaService(restConnection.logger);
    }

    public HubResponseService createHubResponseService() {
        return new HubResponseService(restConnection);
    }

    public ProjectAssignmentRequestService createProjectAssignmentRequestService() {
        return new ProjectAssignmentRequestService(restConnection, createMetaService());
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
        return new ComponentDataService(restConnection.logger, createComponentRequestService(), createMetaService());
    }

    public BomComponentIssueRequestService createBomComponentIssueRequestService() {
        return new BomComponentIssueRequestService(restConnection, createMetaService());
    }

    public ProjectDataService createProjectDataService() {
        return new ProjectDataService(createProjectRequestService(), createProjectVersionRequestService(), createProjectAssignmentRequestService());
    }

    public UserDataService createUserDataService() {
        return new UserDataService(restConnection, createUserRequestService(), createMetaService());
    }

    public VersionBomComponentDataService createVersionBomComponentDataService() {
        return new VersionBomComponentDataService(createProjectRequestService(), createProjectVersionRequestService(), createAggregateBomRequestService(), createMatchedFilesRequestService(), createMetaService());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
