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
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.cli.CLIDownloadUtility;
import com.blackducksoftware.integration.hub.cli.SignatureScanConfig;
import com.blackducksoftware.integration.hub.cli.SimpleScanUtility;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phonehome.PhoneHomeClient;
import com.blackducksoftware.integration.phonehome.google.analytics.GoogleAnalyticsConstants;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.util.IntEnvironmentVariables;
import com.blackducksoftware.integration.util.IntegrationEscapeUtil;
import com.google.gson.Gson;

public class HubServicesFactory {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final RestConnection restConnection;

    public HubServicesFactory(final RestConnection restConnection) {
        this.intEnvironmentVariables = new IntEnvironmentVariables();

        this.restConnection = restConnection;
    }

    public void addEnvironmentVariable(final String key, final String value) {
        intEnvironmentVariables.put(key, value);
    }

    public void addEnvironmentVariables(final Map<String, String> environmentVariables) {
        intEnvironmentVariables.putAll(environmentVariables);
    }

    public SignatureScannerService createSignatureScannerService(final ExecutorService executorService) {
        return new SignatureScannerService(createHubService(), intEnvironmentVariables, createCliDownloadUtility(), createProjectService(), createCodeLocationService(), executorService);
    }

    public PhoneHomeService createPhoneHomeService() {
        return new PhoneHomeService(createHubService(), createPhoneHomeClient(), createHubRegistrationService(), intEnvironmentVariables);
    }

    public PhoneHomeClient createPhoneHomeClient() {
        final String googleAnalyticsTrackingId = GoogleAnalyticsConstants.PRODUCTION_INTEGRATIONS_TRACKING_ID;
        final HttpClientBuilder httpClientBuilder = restConnection.getClientBuilder();
        final Gson gson = restConnection.gson;
        return new PhoneHomeClient(googleAnalyticsTrackingId, httpClientBuilder, gson);
    }

    public PhoneHomeClient createPhoneHomeClient(final Logger logger) {
        final String googleAnalyticsTrackingId = GoogleAnalyticsConstants.PRODUCTION_INTEGRATIONS_TRACKING_ID;
        final HttpClientBuilder httpClientBuilder = restConnection.getClientBuilder();
        final Gson gson = restConnection.gson;
        return new PhoneHomeClient(googleAnalyticsTrackingId, httpClientBuilder, logger, gson);
    }

    public ReportService createReportService(final long timeoutInMilliseconds) throws IntegrationException {
        return new ReportService(createHubService(), createProjectService(), createIntegrationEscapeUtil(), timeoutInMilliseconds);
    }

    public PolicyRuleService createPolicyRuleService() {
        return new PolicyRuleService(createHubService());
    }

    public ScanStatusService createScanStatusService(final long timeoutInMilliseconds) {
        return new ScanStatusService(createHubService(), createProjectService(), createCodeLocationService(), timeoutInMilliseconds);
    }

    public NotificationService createNotificationService() {
        return new NotificationService(createHubService(), createHubBucketService());
    }

    public NotificationService createNotificationService(final boolean oldestFirst) {
        return new NotificationService(createHubService(), createHubBucketService(), oldestFirst);
    }

    public NotificationService createNotificationService(final ExecutorService executorService) {
        return new NotificationService(createHubService(), createHubBucketService(executorService));
    }

    public NotificationService createNotificationService(final ExecutorService executorService, final boolean oldestFirst) {
        return new NotificationService(createHubService(), createHubBucketService(executorService), oldestFirst);
    }

    public LicenseService createLicenseService() {
        return new LicenseService(createHubService(), createComponentService());
    }

    public CodeLocationService createCodeLocationService() {
        return new CodeLocationService(createHubService());
    }

    public CLIDownloadUtility createCliDownloadUtility() {
        return new CLIDownloadUtility(restConnection.logger, restConnection);
    }

    public IntegrationEscapeUtil createIntegrationEscapeUtil() {
        return new IntegrationEscapeUtil();
    }

    public SimpleScanUtility createSimpleScanUtility(final HubServerConfig hubServerConfig, final SignatureScanConfig signatureScanConfig, final String projectName, final String versionName) {
        return new SimpleScanUtility(restConnection.logger, restConnection.gson, hubServerConfig, intEnvironmentVariables, signatureScanConfig, projectName, versionName);
    }

    public HubRegistrationService createHubRegistrationService() {
        return new HubRegistrationService(createHubService());
    }

    public HubService createHubService() {
        return new HubService(restConnection);
    }

    public ComponentService createComponentService() {
        return new ComponentService(createHubService());
    }

    public IssueService createIssueService() {
        return new IssueService(createHubService());
    }

    public ProjectService createProjectService() {
        return new ProjectService(createHubService(), createComponentService());
    }

    public UserGroupService createUserGroupService() {
        return new UserGroupService(createHubService());
    }

    public HubBucketService createHubBucketService() {
        return new HubBucketService(createHubService());
    }

    public HubBucketService createHubBucketService(final ExecutorService executorService) {
        return new HubBucketService(createHubService(), executorService);
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public IntLogger getLogger() {
        return restConnection.logger;
    }

    public Gson getGson() {
        return restConnection.gson;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
