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

import java.io.File;
import java.util.Map;
import java.util.Set;

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
import com.blackducksoftware.integration.hub.api.nonpublic.HubRegistrationRequestService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
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
import com.blackducksoftware.integration.hub.dataservice.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.dataservice.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.vulnerability.VulnerabilityDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

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

    public CLIDataService createCLIDataService(final IntLogger logger) {
        return createCLIDataService(logger, 120000l);
    }

    public CLIDataService createCLIDataService(final IntLogger logger, final long timeoutInMilliseconds) {
        return new CLIDataService(logger, restConnection.gson, ciEnvironmentVariables, createHubVersionRequestService(), createCliDownloadService(logger),
                createPhoneHomeDataService(logger), createProjectRequestService(logger), createProjectVersionRequestService(logger),
                createDryRunUploadRequestService(), createCodeLocationRequestService(logger), createScanSummaryRequestService(),
                createScanStatusDataService(logger, timeoutInMilliseconds));
    }

    public PhoneHomeDataService createPhoneHomeDataService(final IntLogger logger) {
        return new PhoneHomeDataService(logger, restConnection, createHubRegistrationRequestService(), createHubVersionRequestService());
    }

    public RiskReportDataService createRiskReportDataService(final IntLogger logger,
            final long timeoutInMilliseconds) throws IntegrationException {
        return new RiskReportDataService(logger, restConnection, createProjectRequestService(logger),
                createProjectVersionRequestService(logger), createReportRequestService(logger, timeoutInMilliseconds), createAggregateBomRequestService(logger),
                createMetaService(logger), createCheckedHubSupport(logger));
    }

    public PolicyStatusDataService createPolicyStatusDataService(final IntLogger logger) {
        return new PolicyStatusDataService(restConnection, createProjectRequestService(logger),
                createProjectVersionRequestService(logger), createMetaService(logger));
    }

    public ScanStatusDataService createScanStatusDataService(final IntLogger logger,
            final long timeoutInMilliseconds) {
        return new ScanStatusDataService(logger, createProjectRequestService(logger), createProjectVersionRequestService(logger),
                createCodeLocationRequestService(logger), createScanSummaryRequestService(), createMetaService(logger),
                timeoutInMilliseconds);
    }

    public NotificationDataService createNotificationDataService(final IntLogger logger) {
        return new NotificationDataService(logger, createHubResponseService(), createNotificationRequestService(logger),
                createProjectVersionRequestService(logger),
                createPolicyRequestService(), createMetaService(logger));
    }

    public NotificationDataService createNotificationDataService(final IntLogger logger,
            final PolicyNotificationFilter policyNotificationFilter) {
        return new NotificationDataService(logger, createHubResponseService(), createNotificationRequestService(logger),
                createProjectVersionRequestService(logger),
                createPolicyRequestService(), policyNotificationFilter,
                createMetaService(logger));
    }

    public ExtensionConfigDataService createExtensionConfigDataService(final IntLogger logger) {
        return new ExtensionConfigDataService(logger, restConnection, createUserRequestService(),
                createExtensionConfigRequestService(), createExtensionUserOptionRequestService(), createMetaService(logger));
    }

    public VulnerabilityDataService createVulnerabilityDataService(final IntLogger logger) {
        return new VulnerabilityDataService(restConnection, createComponentRequestService(),
                createVulnerabilityRequestService(), createMetaService(logger));
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

    public CodeLocationRequestService createCodeLocationRequestService(final IntLogger logger) {
        return new CodeLocationRequestService(restConnection, createMetaService(logger));
    }

    public ComponentRequestService createComponentRequestService() {
        return new ComponentRequestService(restConnection);
    }

    public HubVersionRequestService createHubVersionRequestService() {
        return new HubVersionRequestService(restConnection);
    }

    public NotificationRequestService createNotificationRequestService(final IntLogger logger) {
        return new NotificationRequestService(logger, restConnection, createMetaService(logger));
    }

    public PolicyRequestService createPolicyRequestService() {
        return new PolicyRequestService(restConnection);
    }

    public ProjectRequestService createProjectRequestService(final IntLogger logger) {
        return new ProjectRequestService(restConnection, createMetaService(logger));
    }

    public ProjectVersionRequestService createProjectVersionRequestService(final IntLogger logger) {
        return new ProjectVersionRequestService(restConnection, createMetaService(logger));
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

    public CLIDownloadService createCliDownloadService(final IntLogger logger) {
        return new CLIDownloadService(logger, restConnection);
    }

    /**
     * @deprecated You should create HubScanConfig, rather than pass in each field
     */
    @Deprecated
    public SimpleScanService createSimpleScanService(final IntLogger logger, final RestConnection restConnection, final HubServerConfig hubServerConfig,
            final HubSupportHelper hubSupportHelper,
            final File directoryToInstallTo, final int scanMemory, final boolean dryRun, final String project,
            final String version, final Set<String> scanTargetPaths, final File workingDirectory, final String[] excludePatterns) {
        return new SimpleScanService(logger, restConnection.gson, hubServerConfig, hubSupportHelper, ciEnvironmentVariables, directoryToInstallTo,
                scanMemory,
                dryRun, project, version, scanTargetPaths, workingDirectory, excludePatterns);
    }

    public SimpleScanService createSimpleScanService(final IntLogger logger, final RestConnection restConnection, final HubServerConfig hubServerConfig,
            final HubSupportHelper hubSupportHelper, final HubScanConfig hubScanConfig, final String projectName, final String versionName) {
        return new SimpleScanService(logger, restConnection.gson, hubServerConfig, hubSupportHelper, ciEnvironmentVariables, hubScanConfig, projectName,
                versionName);
    }

    public HubRegistrationRequestService createHubRegistrationRequestService() {
        return new HubRegistrationRequestService(restConnection);
    }

    public ReportRequestService createReportRequestService(final IntLogger logger, final long timeoutInMilliseconds) {
        return new ReportRequestService(restConnection, logger, createMetaService(logger), timeoutInMilliseconds);
    }

    public AggregateBomRequestService createAggregateBomRequestService(final IntLogger logger) {
        return new AggregateBomRequestService(restConnection, createMetaService(logger));
    }

    public MetaService createMetaService(final IntLogger logger) {
        return new MetaService(logger, restConnection.jsonParser);
    }

    public HubResponseService createHubResponseService() {
        return new HubResponseService(restConnection);
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public HubSupportHelper createCheckedHubSupport(final IntLogger logger) throws IntegrationException {
        final HubSupportHelper supportHelper = new HubSupportHelper();
        supportHelper.checkHubSupport(createHubVersionRequestService(), logger);
        return supportHelper;
    }

    public ComponentDataService createComponentDataService(final IntLogger logger) {
        return new ComponentDataService(logger, createComponentRequestService(), createMetaService(logger));
    }

    public BomComponentIssueRequestService createBomComponentIssueRequestService(final IntLogger logger) {
        return new BomComponentIssueRequestService(restConnection, createMetaService(logger));
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
