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

import java.util.Map;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.nonpublic.HubRegistrationService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionService;
import com.blackducksoftware.integration.hub.api.report.ReportService;
import com.blackducksoftware.integration.hub.cli.CLIDownloadUtility;
import com.blackducksoftware.integration.hub.cli.SimpleScanUtility;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.codelocation.CodeLocationDataService;
import com.blackducksoftware.integration.hub.dataservice.component.ComponentDataService;
import com.blackducksoftware.integration.hub.dataservice.extension.ExtensionConfigDataService;
import com.blackducksoftware.integration.hub.dataservice.issue.IssueDataService;
import com.blackducksoftware.integration.hub.dataservice.license.LicenseDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.phonehome.PhoneHomeDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.report.ReportDataService;
import com.blackducksoftware.integration.hub.dataservice.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.user.UserGroupDataService;
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
        return new CLIDataService(restConnection, ciEnvironmentVariables, createHubVersionService(), createCliDownloadUtility(), createPhoneHomeDataService(), createProjectDataService(),
                createCodeLocationDataService(), createScanStatusDataService(timeoutInMilliseconds));
    }

    public PhoneHomeDataService createPhoneHomeDataService() {
        return new PhoneHomeDataService(restConnection.logger, createPhoneHomeClient(), createHubRegistrationService(), createHubVersionService());
    }

    public PhoneHomeClient createPhoneHomeClient() {
        return new PhoneHomeClient(restConnection.logger, restConnection.timeout, restConnection.getProxyInfo(), restConnection.alwaysTrustServerCertificate);
    }

    public ReportDataService createReportDataService(final long timeoutInMilliseconds) throws IntegrationException {
        return new ReportDataService(restConnection, createReportService(timeoutInMilliseconds), createProjectDataService(), createCheckedHubSupport(),
                createIntegrationEscapeUtil());
    }

    public PolicyStatusDataService createPolicyStatusDataService() {
        return new PolicyStatusDataService(restConnection, createProjectDataService());
    }

    public ScanStatusDataService createScanStatusDataService(final long timeoutInMilliseconds) {
        return new ScanStatusDataService(restConnection, createProjectDataService(), createCodeLocationDataService(), timeoutInMilliseconds);
    }

    public NotificationDataService createNotificationDataService() {
        return new NotificationDataService(restConnection, createHubService());
    }

    public NotificationDataService createNotificationDataService(final PolicyNotificationFilter policyNotificationFilter) {
        return new NotificationDataService(restConnection, createHubService(), policyNotificationFilter);
    }

    public ExtensionConfigDataService createExtensionConfigDataService() {
        return new ExtensionConfigDataService(restConnection.logger, restConnection, createHubService());
    }

    public LicenseDataService createLicenseDataService() {
        return new LicenseDataService(restConnection, createComponentDataService());
    }

    public CodeLocationDataService createBdioUploadDataService() {
        return new CodeLocationDataService(restConnection);
    }

    public CodeLocationDataService createCodeLocationDataService() {
        return new CodeLocationDataService(restConnection);
    }

    public HubVersionService createHubVersionService() {
        return new HubVersionService(restConnection);
    }

    public CLIDownloadUtility createCliDownloadUtility() {
        return new CLIDownloadUtility(restConnection.logger, restConnection);
    }

    public IntegrationEscapeUtil createIntegrationEscapeUtil() {
        return new IntegrationEscapeUtil();
    }

    public SimpleScanUtility createSimpleScanUtility(final RestConnection restConnection, final HubServerConfig hubServerConfig, final HubSupportHelper hubSupportHelper, final HubScanConfig hubScanConfig, final String projectName,
            final String versionName) {
        return new SimpleScanUtility(restConnection.logger, restConnection.gson, hubServerConfig, hubSupportHelper, ciEnvironmentVariables, hubScanConfig, projectName, versionName);
    }

    public HubRegistrationService createHubRegistrationService() {
        return new HubRegistrationService(restConnection);
    }

    public ReportService createReportService(final long timeoutInMilliseconds) {
        return new ReportService(restConnection, restConnection.logger, timeoutInMilliseconds);
    }

    public HubService createHubService() {
        return new HubService(restConnection);
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public HubSupportHelper createCheckedHubSupport() throws IntegrationException {
        final HubSupportHelper supportHelper = new HubSupportHelper();
        supportHelper.checkHubSupport(createHubVersionService(), restConnection.logger);
        return supportHelper;
    }

    public ComponentDataService createComponentDataService() {
        return new ComponentDataService(restConnection);
    }

    public IssueDataService createIssueDataService() {
        return new IssueDataService(restConnection);
    }

    public ProjectDataService createProjectDataService() {
        return new ProjectDataService(restConnection, createComponentDataService());
    }

    public UserGroupDataService createUserGroupDataService() {
        return new UserGroupDataService(restConnection);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
