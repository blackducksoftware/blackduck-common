/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import com.synopsys.integration.blackduck.api.generated.discovery.MediaTypeDiscovery;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.CodeLocationWaiter;
import com.synopsys.integration.blackduck.codelocation.bdio2upload.Bdio2UploadService;
import com.synopsys.integration.blackduck.codelocation.bdio2upload.UploadBdio2BatchRunner;
import com.synopsys.integration.blackduck.codelocation.bdioupload.BdioUploadService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchRunner;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanBatchRunner;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanUploadService;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.SignatureScannerService;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.IntegrationEscapeUtil;
import com.synopsys.integration.util.NoThreadExecutorService;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BlackDuckServicesFactory {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final Gson gson;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final IntLogger logger;
    private final MediaTypeDiscovery mediaTypeDiscovery;

    public static final ExecutorService NO_THREAD_EXECUTOR_SERVICE = new NoThreadExecutorService();

    public static Gson createDefaultGson() {
        return BlackDuckServicesFactory.createDefaultGsonBuilder().create();
    }

    public static ObjectMapper createDefaultObjectMapper() {
        return new ObjectMapper();
    }

    public static GsonBuilder createDefaultGsonBuilder() {
        return new GsonBuilder().setDateFormat(RestConstants.JSON_DATE_FORMAT);
    }

    public static MediaTypeDiscovery createDefaultMediaTypeDiscovery() {
        return new MediaTypeDiscovery();
    }

    public BlackDuckServicesFactory(
        IntEnvironmentVariables intEnvironmentVariables, Gson gson, ObjectMapper objectMapper, ExecutorService executorService, BlackDuckHttpClient blackDuckHttpClient, IntLogger logger, MediaTypeDiscovery mediaTypeDiscovery) {
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.executorService = executorService;
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.logger = logger;
        this.mediaTypeDiscovery = mediaTypeDiscovery;
    }

    /**
     * @deprecated Please provide an ExecutorService - for no change, you can provide an instance of NoThreadExecutorService
     */
    @Deprecated
    public BlackDuckServicesFactory(IntEnvironmentVariables intEnvironmentVariables, Gson gson, ObjectMapper objectMapper, BlackDuckHttpClient blackDuckHttpClient, IntLogger logger, MediaTypeDiscovery mediaTypeDiscovery) {
        this(intEnvironmentVariables, gson, objectMapper, null, blackDuckHttpClient, logger, mediaTypeDiscovery);
    }

    public BdioUploadService createBdioUploadService() {
        BlackDuckService blackDuckService = createBlackDuckService();
        if (null == executorService) {
            return new BdioUploadService(blackDuckService, logger, new UploadBatchRunner(logger, blackDuckService), createCodeLocationCreationService());
        } else {
            return new BdioUploadService(blackDuckService, logger, new UploadBatchRunner(logger, blackDuckService, executorService), createCodeLocationCreationService());
        }
    }

    public Bdio2UploadService createBdio2UploadService() {
        final BlackDuckService blackDuckService = createBlackDuckService();
        return new Bdio2UploadService(blackDuckService, logger, new UploadBdio2BatchRunner(logger, blackDuckService, executorService), createCodeLocationCreationService());
    }

    /**
     * @deprecated Please supply the ExecutorService during construction and use createBdioUploadService()
     */
    @Deprecated
    public BdioUploadService createBdioUploadService(ExecutorService executorService) {
        BlackDuckService blackDuckService = createBlackDuckService();
        return new BdioUploadService(blackDuckService, logger, new UploadBatchRunner(logger, blackDuckService, executorService), createCodeLocationCreationService());
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
            ScanBatchRunner scanBatchRunner = ScanBatchRunner.createDefault(logger, blackDuckHttpClient, intEnvironmentVariables, new NoThreadExecutorService());
            return createSignatureScannerService(scanBatchRunner);
        } else {
            ScanBatchRunner scanBatchRunner = ScanBatchRunner.createDefault(logger, blackDuckHttpClient, intEnvironmentVariables, executorService);
            return createSignatureScannerService(scanBatchRunner);
        }
    }

    public SignatureScannerService createSignatureScannerService(ScanBatchRunner scanBatchRunner) {
        return new SignatureScannerService(createBlackDuckService(), logger, scanBatchRunner, createCodeLocationCreationService());
    }

    /**
     * @deprecated Please use createBinaryScanUploadService instead.
     */
    @Deprecated
    public BinaryScannerService createBinaryScannerService() {
        return new BinaryScannerService(createBlackDuckService(), logger);
    }

    public BinaryScanUploadService createBinaryScanUploadService() {
        BlackDuckService blackDuckService = createBlackDuckService();
        if (null == executorService) {
            return new BinaryScanUploadService(blackDuckService, logger, new BinaryScanBatchRunner(logger, blackDuckService), createCodeLocationCreationService());
        } else {
            return new BinaryScanUploadService(blackDuckService, logger, new BinaryScanBatchRunner(logger, blackDuckService, executorService), createCodeLocationCreationService());
        }
    }

    public CodeLocationCreationService createCodeLocationCreationService() {
        BlackDuckService blackDuckService = createBlackDuckService();
        ProjectService projectService = createProjectService();
        NotificationService notificationService = createNotificationService();
        CodeLocationWaiter codeLocationWaiter = new CodeLocationWaiter(logger, blackDuckService, projectService, notificationService);

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
        return new BlackDuckService(logger, blackDuckHttpClient, gson, objectMapper, mediaTypeDiscovery);
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
        return new ProjectService(blackDuckService, logger, projectGetService);
    }

    public ProjectBomService createProjectBomService() {
        BlackDuckService blackDuckService = createBlackDuckService();
        return new ProjectBomService(blackDuckService, logger, createComponentService());
    }

    public ProjectUsersService createProjectUsersService() {
        BlackDuckService blackDuckService = createBlackDuckService();
        UserGroupService userGroupService = createUserGroupService();
        return new ProjectUsersService(blackDuckService, userGroupService, logger);
    }

    public ReportService createReportService(long timeoutInMilliseconds) {
        return new ReportService(gson, blackDuckHttpClient.getBaseUrl(), createBlackDuckService(), logger, createProjectService(), createIntegrationEscapeUtil(), timeoutInMilliseconds);
    }

    public UserGroupService createUserGroupService() {
        return new UserGroupService(createBlackDuckService(), logger);
    }

    public ProjectMappingService createProjectMappingService() {
        return new ProjectMappingService(createBlackDuckService(), logger);
    }

    public TagService createTagService() {
        return new TagService(createBlackDuckService(), logger);
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
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }

}
