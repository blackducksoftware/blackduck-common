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

import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.dataservice.CodeLocationService;
import com.synopsys.integration.blackduck.service.dataservice.ComponentService;
import com.synopsys.integration.blackduck.service.dataservice.LicenseService;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.dataservice.PolicyRuleService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectBomService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectGetService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectMappingService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectUsersService;
import com.synopsys.integration.blackduck.service.dataservice.ReportService;
import com.synopsys.integration.blackduck.service.dataservice.RoleService;
import com.synopsys.integration.blackduck.service.dataservice.TagService;
import com.synopsys.integration.blackduck.service.dataservice.UserGroupService;
import com.synopsys.integration.blackduck.service.dataservice.UserRoleService;
import com.synopsys.integration.blackduck.service.dataservice.UserService;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.IntegrationEscapeUtil;
import com.synopsys.integration.util.NoThreadExecutorService;

public class BlackDuckServicesFactory {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final Gson gson;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final IntLogger logger;
    private final BlackDuckRequestFactory blackDuckRequestFactory;

    private final BlackDuckJsonTransformer blackDuckJsonTransformer;
    private final BlackDuckResponseTransformer blackDuckResponseTransformer;
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer;
    private final BlackDuckApiClient blackDuckApiClient;

    public static final ExecutorService NO_THREAD_EXECUTOR_SERVICE = new NoThreadExecutorService();

    public static Gson createDefaultGson() {
        return BlackDuckServicesFactory.createDefaultGsonBuilder().create();
    }

    public static ObjectMapper createDefaultObjectMapper() {
        return new ObjectMapper();
    }

    public static GsonBuilder createDefaultGsonBuilder() {
        return new GsonBuilder()
                   .setDateFormat(RestConstants.JSON_DATE_FORMAT);
    }

    public static BlackDuckRequestFactory createDefaultRequestFactory() {
        return new BlackDuckRequestFactory();
    }

    public BlackDuckServicesFactory(
        IntEnvironmentVariables intEnvironmentVariables, Gson gson, ObjectMapper objectMapper, ExecutorService executorService, BlackDuckHttpClient blackDuckHttpClient, IntLogger logger, BlackDuckRequestFactory blackDuckRequestFactory) {
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.executorService = executorService;
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.logger = logger;
        this.blackDuckRequestFactory = blackDuckRequestFactory;

        blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);

        blackDuckApiClient = new BlackDuckApiClient(blackDuckHttpClient, gson, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer, blackDuckRequestFactory);
    }

    public BdioUploadService createBdioUploadService() {
        return new BdioUploadService(blackDuckApiClient, blackDuckRequestFactory, logger, new UploadBatchRunner(logger, blackDuckApiClient, blackDuckRequestFactory, executorService), createCodeLocationCreationService());
    }

    public Bdio2UploadService createBdio2UploadService() {
        return new Bdio2UploadService(blackDuckApiClient, blackDuckRequestFactory, logger, new UploadBdio2BatchRunner(logger, blackDuckApiClient, blackDuckRequestFactory, executorService), createCodeLocationCreationService());
    }

    public BlackDuckBucketService createBlackDuckBucketService() {
        return new BlackDuckBucketService(blackDuckApiClient, blackDuckRequestFactory, logger, executorService);
    }

    public SignatureScannerService createSignatureScannerService() {
        ScanBatchRunner scanBatchRunner = ScanBatchRunner.createDefault(logger, blackDuckHttpClient, intEnvironmentVariables, executorService);
        return createSignatureScannerService(scanBatchRunner);
    }

    public SignatureScannerService createSignatureScannerService(ScanBatchRunner scanBatchRunner) {
        return new SignatureScannerService(blackDuckApiClient, blackDuckRequestFactory, logger, scanBatchRunner, createCodeLocationCreationService());
    }

    public BinaryScanUploadService createBinaryScanUploadService() {
        return new BinaryScanUploadService(blackDuckApiClient, blackDuckRequestFactory, logger, new BinaryScanBatchRunner(logger, blackDuckApiClient, blackDuckRequestFactory, executorService), createCodeLocationCreationService());
    }

    public CodeLocationCreationService createCodeLocationCreationService() {
        ProjectService projectService = createProjectService();
        NotificationService notificationService = createNotificationService();
        CodeLocationWaiter codeLocationWaiter = new CodeLocationWaiter(logger, blackDuckApiClient, projectService, notificationService);

        return new CodeLocationCreationService(blackDuckApiClient, blackDuckRequestFactory, logger, codeLocationWaiter, notificationService);
    }

    public CodeLocationService createCodeLocationService() {
        return new CodeLocationService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public ComponentService createComponentService() {
        return new ComponentService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public BlackDuckRegistrationService createBlackDuckRegistrationService() {
        return new BlackDuckRegistrationService(blackDuckApiClient, blackDuckRequestFactory, logger, blackDuckHttpClient.getBaseUrl());
    }

    public LicenseService createLicenseService() {
        return new LicenseService(blackDuckApiClient, blackDuckRequestFactory, logger, createComponentService());
    }

    public NotificationService createNotificationService() {
        return new NotificationService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public PolicyRuleService createPolicyRuleService() {
        return new PolicyRuleService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public ProjectService createProjectService() {
        ProjectGetService projectGetService = new ProjectGetService(blackDuckApiClient, blackDuckRequestFactory, logger);
        return new ProjectService(blackDuckApiClient, blackDuckRequestFactory, logger, projectGetService);
    }

    public ProjectBomService createProjectBomService() {
        return new ProjectBomService(blackDuckApiClient, blackDuckRequestFactory, logger, createComponentService());
    }

    public ProjectUsersService createProjectUsersService() {
        UserGroupService userGroupService = createUserGroupService();
        return new ProjectUsersService(blackDuckApiClient, blackDuckRequestFactory, logger, userGroupService);
    }

    public ReportService createReportService(long timeoutInMilliseconds) {
        return new ReportService(gson, blackDuckHttpClient.getBaseUrl(), blackDuckApiClient, blackDuckRequestFactory, logger, createProjectService(), createIntegrationEscapeUtil(), timeoutInMilliseconds);
    }

    public UserService createUserService() {
        return new UserService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public RoleService createRoleService() {
        return new RoleService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public UserRoleService createUserRoleService() {
        return new UserRoleService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public UserGroupService createUserGroupService() {
        return new UserGroupService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public ProjectMappingService createProjectMappingService() {
        return new ProjectMappingService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public TagService createTagService() {
        return new TagService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public IntegrationEscapeUtil createIntegrationEscapeUtil() {
        return new IntegrationEscapeUtil();
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

    @Deprecated
    /**
     * @deprecated Please use getBlackDuckApiClient().
     */
    public BlackDuckApiClient getBlackDuckService() {
        return blackDuckApiClient;
    }

    public BlackDuckApiClient getBlackDuckApiClient() {
        return blackDuckApiClient;
    }

    public BlackDuckRequestFactory getRequestFactory() {
        return blackDuckRequestFactory;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }

}
