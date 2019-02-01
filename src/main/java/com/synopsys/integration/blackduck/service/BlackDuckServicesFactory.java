/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
import com.synopsys.integration.blackduck.codelocation.CodeLocationWaiter;
import com.synopsys.integration.blackduck.codelocation.bdioupload.BdioUploadService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.SignatureScannerService;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
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
    private final ExecutorService executorService;
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final IntLogger logger;

    public static Gson createDefaultGson() {
        return BlackDuckServicesFactory.createDefaultGsonBuilder().create();
    }

    public static ObjectMapper createDefaultObjectMapper() {
        return new ObjectMapper();
    }

    public static GsonBuilder createDefaultGsonBuilder() {
        return new GsonBuilder().setDateFormat(RestConstants.JSON_DATE_FORMAT);
    }

    public BlackDuckServicesFactory(Gson gson, ObjectMapper objectMapper, ExecutorService executorService, BlackDuckHttpClient blackDuckHttpClient, IntLogger logger) {
        intEnvironmentVariables = new IntEnvironmentVariables();

        this.gson = gson;
        this.objectMapper = objectMapper;
        this.executorService = executorService;
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.logger = logger;
    }

    public BlackDuckServicesFactory(Gson gson, ObjectMapper objectMapper, BlackDuckHttpClient blackDuckHttpClient, IntLogger logger) {
        this(gson, objectMapper, null, blackDuckHttpClient, logger);
    }

    public BdioUploadService createBdioUploadService() {
        BlackDuckService blackDuckService = createBlackDuckService();
        if (null == executorService) {
            return new BdioUploadService(blackDuckService, logger, new UploadRunner(logger, blackDuckService), createCodeLocationCreationService());
        } else {
            return new BdioUploadService(blackDuckService, logger, new UploadRunner(logger, blackDuckService, executorService), createCodeLocationCreationService());
        }
    }

    /**
     * @deprecated Please supply the ExecutorService during construction and use createBdioUploadService()
     */
    @Deprecated
    public BdioUploadService createBdioUploadService(ExecutorService executorService) {
        BlackDuckService blackDuckService = createBlackDuckService();
        return new BdioUploadService(blackDuckService, logger, new UploadRunner(logger, blackDuckService, executorService), createCodeLocationCreationService());
    }

    public BlackDuckBucketService createBlackDuckBucketService() {
        if (null == executorService) {
            return new BlackDuckBucketService(createBlackDuckService(), logger);
        } else {
            return new BlackDuckBucketService(createBlackDuckService(), logger, executorService);
        }
    }

    /**
     * @deprecated Please supply the ExecutorService during construction and use createBlackDuckBucketService()
     */
    @Deprecated
    public BlackDuckBucketService createBlackDuckBucketService(ExecutorService executorService) {
        return new BlackDuckBucketService(createBlackDuckService(), logger, executorService);
    }

    public SignatureScannerService createSignatureScannerService() {
        if (null == executorService) {
            ScanBatchRunner scanBatchRunner = ScanBatchRunner.createDefault(logger, blackDuckHttpClient, intEnvironmentVariables);
            return createSignatureScannerService(scanBatchRunner);
        } else {
            ScanBatchRunner scanBatchRunner = ScanBatchRunner.createDefault(logger, blackDuckHttpClient, intEnvironmentVariables, executorService);
            return createSignatureScannerService(scanBatchRunner);
        }
    }

    public SignatureScannerService createSignatureScannerService(ScanBatchRunner scanBatchRunner) {
        return new SignatureScannerService(createBlackDuckService(), logger, scanBatchRunner, createCodeLocationCreationService());
    }

    public BinaryScannerService createBinaryScannerService() {
        return new BinaryScannerService(createBlackDuckService(), logger);
    }

    public CodeLocationCreationService createCodeLocationCreationService() {
        BlackDuckService blackDuckService = createBlackDuckService();
        CodeLocationService codeLocationService = createCodeLocationService();
        NotificationService notificationService = createNotificationService();
        CodeLocationWaiter codeLocationWaiter = new CodeLocationWaiter(logger, codeLocationService, notificationService);

        return new CodeLocationCreationService(blackDuckService, logger, codeLocationWaiter, notificationService);
    }

    public CodeLocationService createCodeLocationService() {
        return new CodeLocationService(createBlackDuckService(), logger);
    }

    public ComponentService createComponentService() {
        return new ComponentService(createBlackDuckService(), logger);
    }

    public BlackDuckRegistrationService createBlackDuckRegistrationService() {
        return new BlackDuckRegistrationService(createBlackDuckService(), logger);
    }

    public BlackDuckService createBlackDuckService() {
        return new BlackDuckService(logger, blackDuckHttpClient, gson, objectMapper);
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
        BlackDuckService blackDuckService = createBlackDuckService();
        ProjectGetService projectGetService = new ProjectGetService(blackDuckService, logger);
        return new ProjectService(blackDuckService, logger, projectGetService, createComponentService());
    }

    public ReportService createReportService(long timeoutInMilliseconds) throws IntegrationException {
        return new ReportService(createBlackDuckService(), logger, createProjectService(), createIntegrationEscapeUtil(), timeoutInMilliseconds);
    }

    public UserGroupService createUserGroupService() {
        return new UserGroupService(createBlackDuckService(), logger);
    }

    public ProjectMappingService createProjectMappingService() {
        return new ProjectMappingService(createBlackDuckService(), logger);
    }

    public IntegrationEscapeUtil createIntegrationEscapeUtil() {
        return new IntegrationEscapeUtil();
    }

    public void addEnvironmentVariable(String key, String value) {
        intEnvironmentVariables.put(key, value);
    }

    public void addEnvironmentVariables(Map<String, String> environmentVariables) {
        intEnvironmentVariables.putAll(environmentVariables);
    }

    public BlackDuckHttpClient getBlackDuckHttpClient() {
        return blackDuckHttpClient;
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
