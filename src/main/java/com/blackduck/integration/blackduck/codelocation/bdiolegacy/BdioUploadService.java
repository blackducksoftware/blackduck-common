/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.bdiolegacy;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationData;
import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationService;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatch;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.blackduck.integration.blackduck.codelocation.upload.UploadTarget;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.blackduck.service.model.NotificationTaskRange;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.util.NameVersion;

import java.util.Set;

public class BdioUploadService extends DataService {
    private final UploadBatchRunner uploadBatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public BdioUploadService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger, UploadBatchRunner uploadBatchRunner, CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.uploadBatchRunner = uploadBatchRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public BdioUploadCodeLocationCreationRequest createUploadRequest(UploadBatch uploadBatch) {
        return new BdioUploadCodeLocationCreationRequest(uploadBatchRunner, uploadBatch);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(BdioUploadCodeLocationCreationRequest uploadRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(uploadRequest);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(UploadBatch uploadBatch) throws IntegrationException {
        BdioUploadCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBdio(uploadRequest);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(UploadTarget uploadTarget) throws IntegrationException {
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(uploadTarget);
        BdioUploadCodeLocationCreationRequest uploadRequest = new BdioUploadCodeLocationCreationRequest(uploadBatchRunner, uploadBatch);

        return uploadBdio(uploadRequest);
    }

    public UploadBatchOutput uploadBdioAndWait(BdioUploadCodeLocationCreationRequest uploadRequest, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(uploadRequest, timeoutInSeconds);
    }

    public UploadBatchOutput uploadBdioAndWait(UploadBatch uploadBatch, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        BdioUploadCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBdioAndWait(uploadRequest, timeoutInSeconds);
    }

    public UploadBatchOutput uploadBdioAndWait(UploadTarget uploadTarget, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(uploadTarget);
        BdioUploadCodeLocationCreationRequest uploadRequest = new BdioUploadCodeLocationCreationRequest(uploadBatchRunner, uploadBatch);

        return uploadBdioAndWait(uploadRequest, timeoutInSeconds);
    }

    public void waitForBdioUpload(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames, int expectedNotificationCount, long timeoutInSeconds)
        throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds);
    }

}
