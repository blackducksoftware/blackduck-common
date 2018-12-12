/**
 * blackduck-common
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

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.BdioUploadService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.SignatureScannerService;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetailFactory;
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.IntegrationEscapeUtil;

public class BlackDuckServicesFactory {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final Gson gson;
    private final ObjectMapper objectMapper;
    private final BlackDuckRestConnection restConnection;
    private final IntLogger logger;

    public static Gson createDefaultGson() {
        return createDefaultGsonBuilder().create();
    }

    public static ObjectMapper createDefaultObjectMapper() {
        return new ObjectMapper();
    }

    public static GsonBuilder createDefaultGsonBuilder() {
        return new GsonBuilder().setDateFormat(RestConstants.JSON_DATE_FORMAT);
    }

    // NOTE
    //
    // The 'post' methods are alphabetical by return type - please keep this pattern consistent.
    public BlackDuckServicesFactory(final Gson gson, final ObjectMapper objectMapper, final BlackDuckRestConnection restConnection, final IntLogger logger) {
        intEnvironmentVariables = new IntEnvironmentVariables();

        this.gson = gson;
        this.objectMapper = objectMapper;
        this.restConnection = restConnection;
        this.logger = logger;
    }

    public BdioUploadService createBdioUploadService() {
        final BlackDuckService blackDuckService = createBlackDuckService();
        return new BdioUploadService(blackDuckService, logger, new UploadRunner(logger, blackDuckService), createCodeLocationCreationService());
    }

    public BdioUploadService createBdioUploadService(final ExecutorService executorService) {
        final BlackDuckService blackDuckService = createBlackDuckService();
        return new BdioUploadService(blackDuckService, logger, new UploadRunner(logger, blackDuckService, executorService), createCodeLocationCreationService());
    }

    public BinaryScannerService createBinaryScannerService() {
        return new BinaryScannerService(createBlackDuckService(), logger);
    }

    public CodeLocationCreationService createCodeLocationCreationService() {
        final BlackDuckService blackDuckService = createBlackDuckService();
        final CodeLocationService codeLocationService = createCodeLocationService();
        final NotificationService notificationService = createNotificationService();

        return new CodeLocationCreationService(blackDuckService, logger, codeLocationService, notificationService);
    }

    public CodeLocationService createCodeLocationService() {
        return new CodeLocationService(createBlackDuckService(), logger);
    }

    public CommonNotificationService createCommonNotificationService(final NotificationContentDetailFactory notificationContentDetailFactory, final boolean oldestFirst) {
        return new CommonNotificationService(notificationContentDetailFactory, oldestFirst);
    }

    public ComponentService createComponentService() {
        return new ComponentService(createBlackDuckService(), logger);
    }

    public BlackDuckRegistrationService createBlackDuckRegistrationService() {
        return new BlackDuckRegistrationService(createBlackDuckService(), logger);
    }

    public BlackDuckService createBlackDuckService() {
        return new BlackDuckService(logger, restConnection, gson, objectMapper);
    }

    public BlackDuckBucketService createBlackDuckBucketService() {
        return new BlackDuckBucketService(createBlackDuckService(), logger);
    }

    public BlackDuckBucketService createBlackDuckBucketService(final ExecutorService executorService) {
        return new BlackDuckBucketService(createBlackDuckService(), logger, executorService);
    }

    public IntegrationEscapeUtil createIntegrationEscapeUtil() {
        return new IntegrationEscapeUtil();
    }

    public LicenseService createLicenseService() {
        return new LicenseService(createBlackDuckService(), logger, createComponentService());
    }

    public NotificationService createNotificationService() {
        return new NotificationService(createBlackDuckService(), logger);
    }

    public PolicyRuleService createPolicyRuleService() {
        return new PolicyRuleService(createBlackDuckService());
    }

    public ProjectService createProjectService() {
        final BlackDuckService blackDuckService = createBlackDuckService();
        final ProjectGetService projectGetService = new ProjectGetService(blackDuckService, logger);
        return new ProjectService(blackDuckService, logger, projectGetService, createComponentService());
    }

    public ReportService createReportService(final long timeoutInMilliseconds) throws IntegrationException {
        return new ReportService(createBlackDuckService(), logger, createProjectService(), createIntegrationEscapeUtil(), timeoutInMilliseconds);
    }

    public SignatureScannerService createSignatureScannerService(final ScanBatchRunner scanBatchRunner) {
        return new SignatureScannerService(createBlackDuckService(), logger, scanBatchRunner, createCodeLocationCreationService());
    }

    public UserGroupService createUserGroupService() {
        return new UserGroupService(createBlackDuckService(), logger);
    }
    // NOTE
    //
    // The 'post' methods are alphabetical by return type - please keep this pattern consistent.

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

    public Gson getGson() {
        return gson;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public IntEnvironmentVariables getEnvironmentVariables() {
        return intEnvironmentVariables;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
