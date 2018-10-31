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
package com.synopsys.integration.blackduck.service;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetailFactory;
import com.synopsys.integration.blackduck.phonehome.BlackDuckPhoneHomeCallable;
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.blackduck.service.bucket.HubBucketService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jsonfield.JsonFieldResolver;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.phonehome.PhoneHomeClient;
import com.synopsys.integration.phonehome.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.PhoneHomeService;
import com.synopsys.integration.phonehome.google.analytics.GoogleAnalyticsConstants;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.IntegrationEscapeUtil;

public class HubServicesFactory {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final Gson gson;
    private final JsonParser jsonParser;
    private final JsonFieldResolver jsonFieldResolver;
    private final BlackDuckRestConnection restConnection;
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

    // NOTE
    //
    // The 'create' methods are alphabetical by return type - please keep this pattern consistent.
    public HubServicesFactory(final Gson gson, final JsonParser jsonParser, final JsonFieldResolver jsonFieldResolver, final BlackDuckRestConnection restConnection, final IntLogger logger) {
        intEnvironmentVariables = new IntEnvironmentVariables();

        this.gson = gson;
        this.jsonParser = jsonParser;
        this.jsonFieldResolver = jsonFieldResolver;
        this.restConnection = restConnection;
        this.logger = logger;
    }

    public BinaryScannerService createBinaryScannerService() {
        return new BinaryScannerService(createHubService(), logger);
    }

    public CodeLocationCreationService createCodeLocationCreationService() {
        final HubService hubService = createHubService();
        final CodeLocationService codeLocationService = createCodeLocationService();
        final NotificationService notificationService = createNotificationService();

        return new CodeLocationCreationService(hubService, logger, jsonFieldResolver, codeLocationService, notificationService);
    }

    public CodeLocationService createCodeLocationService() {
        return new CodeLocationService(createHubService(), logger);
    }

    public CommonNotificationService createCommonNotificationService(final NotificationContentDetailFactory notificationContentDetailFactory, final boolean oldestFirst) {
        return new CommonNotificationService(notificationContentDetailFactory, oldestFirst);
    }

    public ComponentService createComponentService() {
        return new ComponentService(createHubService(), logger);
    }

    public HubRegistrationService createHubRegistrationService() {
        return new HubRegistrationService(createHubService(), logger);
    }

    public HubService createHubService() {
        return new HubService(logger, restConnection, gson, jsonParser);
    }

    public HubBucketService createHubBucketService() {
        return new HubBucketService(createHubService(), logger);
    }

    public HubBucketService createHubBucketService(final ExecutorService executorService) {
        return new HubBucketService(createHubService(), logger, executorService);
    }

    public IntegrationEscapeUtil createIntegrationEscapeUtil() {
        return new IntegrationEscapeUtil();
    }

    public IssueService createIssueService() {
        return new IssueService(createHubService(), logger);
    }

    public LicenseService createLicenseService() {
        return new LicenseService(createHubService(), logger, createComponentService());
    }

    public NotificationService createNotificationService() {
        return new NotificationService(createHubService(), logger);
    }

    public PhoneHomeService createPhoneHomeService(final ExecutorService executorService) {
        return new PhoneHomeService(logger, executorService);
    }

    public BlackDuckPhoneHomeCallable createBlackDuckPhoneHomeCallable(final URL productURL, final String artifactId, final String artifactVersion) {
        final BlackDuckPhoneHomeCallable phoneHomeCallable = new BlackDuckPhoneHomeCallable(logger, createPhoneHomeClient(), productURL, artifactId, artifactVersion, intEnvironmentVariables, createHubService(),
                createHubRegistrationService());
        return phoneHomeCallable;
    }

    public BlackDuckPhoneHomeCallable createBlackDuckPhoneHomeCallable(final URL productURL, final String artifactId, final String artifactVersion, final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder) {
        final BlackDuckPhoneHomeCallable phoneHomeCallable = new BlackDuckPhoneHomeCallable(logger, createPhoneHomeClient(), productURL, artifactId, artifactVersion, intEnvironmentVariables, createHubService(),
                createHubRegistrationService(),
                phoneHomeRequestBodyBuilder);
        return phoneHomeCallable;
    }

    public PhoneHomeClient createPhoneHomeClient() {
        final String googleAnalyticsTrackingId = GoogleAnalyticsConstants.PRODUCTION_INTEGRATIONS_TRACKING_ID;
        final HttpClientBuilder httpClientBuilder = restConnection.getClientBuilder();
        return new PhoneHomeClient(googleAnalyticsTrackingId, logger, httpClientBuilder, gson);
    }

    public PolicyRuleService createPolicyRuleService() {
        return new PolicyRuleService(createHubService());
    }

    public ProjectService createProjectService() {
        final HubService hubService = createHubService();
        final ProjectGetService projectGetService = new ProjectGetService(hubService, logger);
        final ProjectUpdateService projectUpdateService = new ProjectUpdateService(hubService, logger, projectGetService);
        return new ProjectService(hubService, logger, projectGetService, projectUpdateService, createComponentService());
    }

    public ReportService createReportService(final long timeoutInMilliseconds) throws IntegrationException {
        return new ReportService(createHubService(), logger, createProjectService(), createIntegrationEscapeUtil(), timeoutInMilliseconds);
    }

    public UserGroupService createUserGroupService() {
        return new UserGroupService(createHubService(), logger);
    }
    // NOTE
    //
    // The 'create' methods are alphabetical by return type - please keep this pattern consistent.

    public void addEnvironmentVariable(final String key, final String value) {
        intEnvironmentVariables.put(key, value);
    }

    public void addEnvironmentVariables(final Map<String, String> environmentVariables) {
        intEnvironmentVariables.putAll(environmentVariables);
    }

    public BlackDuckRestConnection getRestConnection() {
        return restConnection;
    }

    public IntLogger getLogger() {
        return logger;
    }

    public JsonFieldResolver getJsonFieldResolver() {
        return jsonFieldResolver;
    }

    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public Gson getGson() {
        return gson;
    }

    public IntEnvironmentVariables getEnvironmentVariables() {
        return intEnvironmentVariables;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
