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
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetailFactory;
import com.blackducksoftware.integration.hub.rest.BlackduckRestConnection;
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phonehome.PhoneHomeClient;
import com.blackducksoftware.integration.phonehome.google.analytics.GoogleAnalyticsConstants;
import com.blackducksoftware.integration.rest.RestConstants;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.util.IntEnvironmentVariables;
import com.blackducksoftware.integration.util.IntegrationEscapeUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class HubServicesFactory {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final Gson gson;
    private final JsonParser jsonParser;
    private final BlackduckRestConnection restConnection;
    private final IntLogger logger;

    public static Gson createDefaultGson() {
        return createDefaultGsonBuilder().create();
    }

    public static GsonBuilder createDefaultGsonBuilder() {
        return new GsonBuilder().setDateFormat(RestConstants.JSON_DATE_FORMAT);
    }

    public static JsonParser createDefaultJsonParser() {
        return new JsonParser();
    }

    public HubServicesFactory(final Gson gson, final JsonParser jsonParser, final BlackduckRestConnection restConnection, final IntLogger logger) {
        this.intEnvironmentVariables = new IntEnvironmentVariables();

        this.gson = gson;
        this.jsonParser = jsonParser;
        this.restConnection = restConnection;
        this.logger = logger;
    }

    public void addEnvironmentVariable(final String key, final String value) {
        intEnvironmentVariables.put(key, value);
    }

    public void addEnvironmentVariables(final Map<String, String> environmentVariables) {
        intEnvironmentVariables.putAll(environmentVariables);
    }

    public SignatureScannerService createSignatureScannerService(final ExecutorService executorService) {
        return new SignatureScannerService(createHubService(), logger, intEnvironmentVariables, createCliDownloadUtility(), createProjectService(), createCodeLocationService(), executorService);
    }

    public SignatureScannerService createSignatureScannerService() {
        return new SignatureScannerService(createHubService(), logger, intEnvironmentVariables, createCliDownloadUtility(), createProjectService(), createCodeLocationService());
    }

    public PhoneHomeService createPhoneHomeService() {
        return new PhoneHomeService(createHubService(), logger, createPhoneHomeClient(), createHubRegistrationService(), intEnvironmentVariables);
    }

    public PhoneHomeClient createPhoneHomeClient() {
        final String googleAnalyticsTrackingId = GoogleAnalyticsConstants.PRODUCTION_INTEGRATIONS_TRACKING_ID;
        final HttpClientBuilder httpClientBuilder = restConnection.getClientBuilder();
        return new PhoneHomeClient(googleAnalyticsTrackingId, httpClientBuilder, gson);
    }

    public PhoneHomeClient createPhoneHomeClient(final Logger logger) {
        final String googleAnalyticsTrackingId = GoogleAnalyticsConstants.PRODUCTION_INTEGRATIONS_TRACKING_ID;
        final HttpClientBuilder httpClientBuilder = restConnection.getClientBuilder();
        return new PhoneHomeClient(googleAnalyticsTrackingId, httpClientBuilder, logger, gson);
    }

    public ReportService createReportService(final long timeoutInMilliseconds) throws IntegrationException {
        return new ReportService(createHubService(), logger, createProjectService(), createIntegrationEscapeUtil(), timeoutInMilliseconds);
    }

    public PolicyRuleService createPolicyRuleService() {
        return new PolicyRuleService(createHubService());
    }

    public ScanStatusService createScanStatusService(final long timeoutInMilliseconds) {
        return new ScanStatusService(createHubService(), logger, createProjectService(), createCodeLocationService(), timeoutInMilliseconds);
    }

    public NotificationService createNotificationService() {
        return new NotificationService(createHubService(), logger);
    }

    public CommonNotificationService createCommonNotificationService(final NotificationContentDetailFactory notificationContentDetailFactory, final boolean oldestFirst) {
        return new CommonNotificationService(notificationContentDetailFactory, oldestFirst);
    }

    public LicenseService createLicenseService() {
        return new LicenseService(createHubService(), logger, createComponentService());
    }

    public CodeLocationService createCodeLocationService() {
        return new CodeLocationService(createHubService(), logger);
    }

    public CLIDownloadUtility createCliDownloadUtility() {
        return new CLIDownloadUtility(logger, restConnection);
    }

    public IntegrationEscapeUtil createIntegrationEscapeUtil() {
        return new IntegrationEscapeUtil();
    }

    public SimpleScanUtility createSimpleScanUtility(final HubServerConfig hubServerConfig, final SignatureScanConfig signatureScanConfig, final String projectName, final String versionName) {
        return new SimpleScanUtility(logger, gson, hubServerConfig, intEnvironmentVariables, signatureScanConfig, projectName, versionName);
    }

    public HubRegistrationService createHubRegistrationService() {
        return new HubRegistrationService(createHubService(), logger);
    }

    public HubService createHubService() {
        return new HubService(logger, restConnection, gson, jsonParser);
    }

    public ComponentService createComponentService() {
        return new ComponentService(createHubService(), logger);
    }

    public IssueService createIssueService() {
        return new IssueService(createHubService(), logger);
    }

    public ProjectService createProjectService() {
        return new ProjectService(createHubService(), logger, createComponentService());
    }

    public UserGroupService createUserGroupService() {
        return new UserGroupService(createHubService(), logger);
    }

    public HubBucketService createHubBucketService() {
        return new HubBucketService(createHubService(), logger);
    }

    public HubBucketService createHubBucketService(final ExecutorService executorService) {
        return new HubBucketService(createHubService(), logger, executorService);
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public IntLogger getLogger() {
        return logger;
    }

    public Gson getGson() {
        return gson;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
