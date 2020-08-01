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
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.dataservice.*;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.support.UrlSupport;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.IntegrationEscapeUtil;
import com.synopsys.integration.util.NoThreadExecutorService;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.concurrent.ExecutorService;

public class BlackDuckServicesFactory {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final Gson gson;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final IntLogger logger;
    private final RequestFactory requestFactory;

    private final BlackDuckJsonTransformer blackDuckJsonTransformer;
    private final BlackDuckResponseTransformer blackDuckResponseTransformer;
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer;
    private final BlackDuckService blackDuckService;

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

    public static RequestFactory createDefaultRequestFactory() {
        return new RequestFactory(new MediaTypeDiscovery());
    }

    public static UrlSupport createDefaultUrlSupport() {
        return new UrlSupport();
    }

    public BlackDuckServicesFactory(
            IntEnvironmentVariables intEnvironmentVariables, Gson gson, ObjectMapper objectMapper, ExecutorService executorService, BlackDuckHttpClient blackDuckHttpClient, IntLogger logger, RequestFactory requestFactory, UrlSupport urlSupport) {
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.executorService = executorService;
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.logger = logger;
        this.requestFactory = requestFactory;

        blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);

        blackDuckService = new BlackDuckService(blackDuckHttpClient, gson, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer, requestFactory, urlSupport);
    }

    public BdioUploadService createBdioUploadService() {
        return new BdioUploadService(blackDuckService, requestFactory, logger, new UploadBatchRunner(logger, blackDuckService, requestFactory, executorService), createCodeLocationCreationService());
    }

    public Bdio2UploadService createBdio2UploadService() {
        return new Bdio2UploadService(blackDuckService, requestFactory, logger, new UploadBdio2BatchRunner(logger, blackDuckService, requestFactory, executorService), createCodeLocationCreationService());
    }

    public BlackDuckBucketService createBlackDuckBucketService() {
        return new BlackDuckBucketService(blackDuckService, requestFactory, logger, executorService);
    }

    public SignatureScannerService createSignatureScannerService() {
        ScanBatchRunner scanBatchRunner = ScanBatchRunner.createDefault(logger, blackDuckHttpClient, intEnvironmentVariables, executorService);
        return createSignatureScannerService(scanBatchRunner);
    }

    public SignatureScannerService createSignatureScannerService(ScanBatchRunner scanBatchRunner) {
        return new SignatureScannerService(blackDuckService, requestFactory, logger, scanBatchRunner, createCodeLocationCreationService());
    }

    public BinaryScanUploadService createBinaryScanUploadService() {
        return new BinaryScanUploadService(blackDuckService, requestFactory, logger, new BinaryScanBatchRunner(logger, blackDuckService, requestFactory, executorService), createCodeLocationCreationService());
    }

    public CodeLocationCreationService createCodeLocationCreationService() {
        ProjectService projectService = createProjectService();
        NotificationService notificationService = createNotificationService();
        CodeLocationWaiter codeLocationWaiter = new CodeLocationWaiter(logger, blackDuckService, projectService, notificationService);

        return new CodeLocationCreationService(blackDuckService, requestFactory, logger, codeLocationWaiter, notificationService);
    }

    public CodeLocationService createCodeLocationService() {
        return new CodeLocationService(blackDuckService, requestFactory, logger);
    }

    public ComponentService createComponentService() {
        return new ComponentService(blackDuckService, requestFactory, logger);
    }

    public BlackDuckRegistrationService createBlackDuckRegistrationService() {
        return new BlackDuckRegistrationService(blackDuckService, requestFactory, logger, blackDuckHttpClient.getBaseUrl());
    }

    public LicenseService createLicenseService() {
        return new LicenseService(blackDuckService, requestFactory, logger, createComponentService());
    }

    public NotificationService createNotificationService() {
        return new NotificationService(blackDuckService, requestFactory, logger);
    }

    public PolicyRuleService createPolicyRuleService() {
        return new PolicyRuleService(blackDuckService, requestFactory, logger);
    }

    public ProjectService createProjectService() {
        ProjectGetService projectGetService = new ProjectGetService(blackDuckService, requestFactory, logger);
        return new ProjectService(blackDuckService, requestFactory, logger, projectGetService);
    }

    public ProjectBomService createProjectBomService() {
        return new ProjectBomService(blackDuckService, requestFactory, logger, createComponentService());
    }

    public ProjectUsersService createProjectUsersService() {
        UserGroupService userGroupService = createUserGroupService();
        return new ProjectUsersService(blackDuckService, requestFactory, logger, userGroupService);
    }

    public ReportService createReportService(long timeoutInMilliseconds) {
        return new ReportService(gson, blackDuckHttpClient.getBaseUrl(), blackDuckService, requestFactory, logger, createProjectService(), createIntegrationEscapeUtil(), timeoutInMilliseconds);
    }

    public UserGroupService createUserGroupService() {
        return new UserGroupService(blackDuckService, requestFactory, logger);
    }

    public ProjectMappingService createProjectMappingService() {
        return new ProjectMappingService(blackDuckService, requestFactory, logger);
    }

    public TagService createTagService() {
        return new TagService(blackDuckService, requestFactory, logger);
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

    public BlackDuckService getBlackDuckService() {
        return blackDuckService;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }

}
