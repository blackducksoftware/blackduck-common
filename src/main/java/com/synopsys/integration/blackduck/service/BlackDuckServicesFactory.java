/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.blackduck.bdio2.Bdio2FileUploadService;
import com.synopsys.integration.blackduck.bdio2.Bdio2StreamUploader;
import com.synopsys.integration.blackduck.bdio2.util.Bdio2ContentExtractor;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.CodeLocationWaiter;
import com.synopsys.integration.blackduck.codelocation.bdio2legacy.Bdio2UploadService;
import com.synopsys.integration.blackduck.codelocation.bdio2legacy.UploadBdio2BatchRunner;
import com.synopsys.integration.blackduck.codelocation.bdiolegacy.BdioUploadService;
import com.synopsys.integration.blackduck.codelocation.bdiolegacy.UploadBatchRunner;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanBatchRunner;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanUploadService;
import com.synopsys.integration.blackduck.codelocation.intelligentpersistence.IntelligentPersistenceBatchRunner;
import com.synopsys.integration.blackduck.codelocation.intelligentpersistence.IntelligentPersistenceService;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.SignatureScannerService;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.synopsys.integration.blackduck.scan.RapidScanService;
import com.synopsys.integration.blackduck.scan.RapidScanWaiter;
import com.synopsys.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.dataservice.CodeLocationService;
import com.synopsys.integration.blackduck.service.dataservice.ComponentService;
import com.synopsys.integration.blackduck.service.dataservice.IssueService;
import com.synopsys.integration.blackduck.service.dataservice.LicenseService;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.dataservice.PolicyRuleService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectBomService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectGetService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectMappingService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectUsersService;
import com.synopsys.integration.blackduck.service.dataservice.RoleService;
import com.synopsys.integration.blackduck.service.dataservice.TagService;
import com.synopsys.integration.blackduck.service.dataservice.UserGroupService;
import com.synopsys.integration.blackduck.service.dataservice.UserRoleService;
import com.synopsys.integration.blackduck.service.dataservice.UserService;
import com.synopsys.integration.blackduck.service.request.NotificationRequestFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.IntegrationEscapeUtil;
import com.synopsys.integration.util.NoThreadExecutorService;

public class BlackDuckServicesFactory {
    public static final ExecutorService NO_THREAD_EXECUTOR_SERVICE = new NoThreadExecutorService();

    private final IntEnvironmentVariables intEnvironmentVariables;
    private final ExecutorService executorService;
    private final IntLogger logger;
    private final BlackDuckHttpClient blackDuckHttpClient;

    private final Gson gson;
    private final ObjectMapper objectMapper;
    private final BlackDuckRequestFactory blackDuckRequestFactory;

    private final BlackDuckResponseResolver blackDuckResponseResolver;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;
    private final BlackDuckResponseTransformer blackDuckResponseTransformer;
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer;
    private final BlackDuckApiClient blackDuckApiClient;

    public static Gson createDefaultGson() {
        return createDefaultGsonBuilder().create();
    }

    public static ObjectMapper createDefaultObjectMapper() {
        return new ObjectMapper();
    }

    public static GsonBuilder createDefaultGsonBuilder() {
        return new GsonBuilder()
                   .setDateFormat(RestConstants.JSON_DATE_FORMAT);
    }

    public BlackDuckServicesFactory(IntEnvironmentVariables intEnvironmentVariables, ExecutorService executorService, IntLogger logger, BlackDuckHttpClient blackDuckHttpClient) {
        this(intEnvironmentVariables, executorService, logger, blackDuckHttpClient, createDefaultGson(), createDefaultObjectMapper(), BlackDuckRequestFactory::new);
    }

    public BlackDuckServicesFactory(IntEnvironmentVariables intEnvironmentVariables, ExecutorService executorService, IntLogger logger, BlackDuckHttpClient blackDuckHttpClient, Gson gson,
        ObjectMapper objectMapper, Function<Gson, BlackDuckRequestFactory> blackDuckRequestFactoryCreator) {
        this(intEnvironmentVariables, executorService, logger, blackDuckHttpClient, gson, objectMapper, blackDuckRequestFactoryCreator.apply(gson));
    }

    public BlackDuckServicesFactory(IntEnvironmentVariables intEnvironmentVariables, ExecutorService executorService, IntLogger logger, BlackDuckHttpClient blackDuckHttpClient, Gson gson,
        ObjectMapper objectMapper, BlackDuckRequestFactory blackDuckRequestFactory) {
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.executorService = executorService;
        this.logger = logger;
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.blackDuckRequestFactory = blackDuckRequestFactory;

        blackDuckResponseResolver = new BlackDuckResponseResolver(gson);
        blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, blackDuckResponseResolver, logger);
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

    public IssueService createIssueService() {
        return new IssueService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public LicenseService createLicenseService() {
        return new LicenseService(blackDuckApiClient, blackDuckRequestFactory, logger, createComponentService());
    }

    public NotificationService createNotificationService() {
        return new NotificationService(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public NotificationRequestFactory createNotificationRequestFactory() {
        return new NotificationRequestFactory(blackDuckRequestFactory);
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

    public RapidScanService createRapidScanService() {
        RapidScanWaiter rapidScanWaiter = new RapidScanWaiter(logger, blackDuckApiClient);
        Bdio2StreamUploader bdio2Uploader = new Bdio2StreamUploader(blackDuckApiClient, blackDuckRequestFactory, logger, BlackDuckApiClient.SCAN_DEVELOPER_MODE_PATH, RapidScanService.CONTENT_TYPE);
        Bdio2FileUploadService bdio2FileUploadService = new Bdio2FileUploadService(blackDuckApiClient, blackDuckRequestFactory, logger, new Bdio2ContentExtractor(), bdio2Uploader);
        return new RapidScanService(bdio2FileUploadService, rapidScanWaiter);
    }

    public IntelligentPersistenceService createIntelligentPersistenceService() {
        Bdio2StreamUploader bdio2Uploader = new Bdio2StreamUploader(blackDuckApiClient, blackDuckRequestFactory, logger, BlackDuckApiClient.SCAN_INTELLIGENT_PERSISTENCE_MODE_PATH, IntelligentPersistenceService.CONTENT_TYPE);
        Bdio2FileUploadService bdio2FileUploadService = new Bdio2FileUploadService(blackDuckApiClient, blackDuckRequestFactory, logger, new Bdio2ContentExtractor(), bdio2Uploader);
        IntelligentPersistenceBatchRunner batchRunner = new IntelligentPersistenceBatchRunner(logger, executorService, bdio2FileUploadService);
        return new IntelligentPersistenceService(blackDuckApiClient, blackDuckRequestFactory, logger, batchRunner, createCodeLocationCreationService());
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
