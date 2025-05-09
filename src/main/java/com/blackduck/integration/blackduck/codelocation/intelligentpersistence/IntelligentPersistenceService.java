/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.intelligentpersistence;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationData;
import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationRequest;
import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationService;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatch;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.blackduck.service.model.NotificationTaskRange;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.util.NameVersion;

import java.util.Set;

public class IntelligentPersistenceService extends DataService {
    public static final String CONTENT_TYPE = "application/vnd.blackducksoftware.intelligent-persistence-scan-2-ld-2+json";
    private final IntelligentPersistenceBatchRunner uploadBatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public IntelligentPersistenceService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger, IntelligentPersistenceBatchRunner uploadBatchRunner, CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.uploadBatchRunner = uploadBatchRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public IntelligentPersistenceCodeLocationCreationRequest createUploadRequest(UploadBatch uploadBatch, long timeoutInSeconds, long clientStartTime) {
        return new IntelligentPersistenceCodeLocationCreationRequest(uploadBatchRunner, uploadBatch, timeoutInSeconds, clientStartTime);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(CodeLocationCreationRequest<UploadBatchOutput> uploadRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(uploadRequest);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(UploadBatch uploadBatch, long timeoutInSeconds, long clientStartTime) throws IntegrationException {
        IntelligentPersistenceCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch, timeoutInSeconds, clientStartTime);
        return uploadBdio(uploadRequest);
    }

    public UploadBatchOutput uploadBdioAndWait(CodeLocationCreationRequest<UploadBatchOutput> uploadRequest, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(uploadRequest, timeoutInSeconds);
    }

    public UploadBatchOutput uploadBdioAndWait(UploadBatch uploadBatch, long timeoutInSeconds, long clientStartTime) throws IntegrationException, InterruptedException {
        IntelligentPersistenceCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch, (int) timeoutInSeconds, clientStartTime);
        return uploadBdioAndWait(uploadRequest, timeoutInSeconds);
    }

    public void waitForBdioUpload(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames, int expectedNotificationCount, long timeoutInSeconds)
        throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds);
    }
}
