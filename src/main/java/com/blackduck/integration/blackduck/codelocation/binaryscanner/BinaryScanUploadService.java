/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.binaryscanner;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationData;
import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationService;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.blackduck.service.model.NotificationTaskRange;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.util.NameVersion;

import java.util.Set;

public class BinaryScanUploadService extends DataService {
    private final BinaryScanBatchRunner binaryScanBatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public BinaryScanUploadService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger, BinaryScanBatchRunner binaryScanBatchRunner, CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.binaryScanBatchRunner = binaryScanBatchRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public BinaryScanCodeLocationCreationRequest createUploadRequest(BinaryScanBatch uploadBatch) {
        return new BinaryScanCodeLocationCreationRequest(binaryScanBatchRunner, uploadBatch);
    }

    public CodeLocationCreationData<BinaryScanBatchOutput> uploadBinaryScan(BinaryScanCodeLocationCreationRequest uploadRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(uploadRequest);
    }

    public CodeLocationCreationData<BinaryScanBatchOutput> uploadBinaryScan(BinaryScanBatch uploadBatch) throws IntegrationException {
        BinaryScanCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBinaryScan(uploadRequest);
    }

    public CodeLocationCreationData<BinaryScanBatchOutput> uploadBinaryScan(BinaryScan binaryScan) throws IntegrationException {
        BinaryScanBatch uploadBatch = new BinaryScanBatch();
        uploadBatch.addBinaryScan(binaryScan);
        BinaryScanCodeLocationCreationRequest uploadRequest = new BinaryScanCodeLocationCreationRequest(binaryScanBatchRunner, uploadBatch);

        return uploadBinaryScan(uploadRequest);
    }

    public BinaryScanBatchOutput uploadBinaryScanAndWait(BinaryScanCodeLocationCreationRequest uploadRequest, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(uploadRequest, timeoutInSeconds);
    }

    public BinaryScanBatchOutput uploadBinaryScanAndWait(BinaryScanBatch uploadBatch, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        BinaryScanCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBinaryScanAndWait(uploadRequest, timeoutInSeconds);
    }

    public BinaryScanBatchOutput uploadBinaryScanAndWait(BinaryScan binaryScan, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        BinaryScanBatch uploadBatch = new BinaryScanBatch();
        uploadBatch.addBinaryScan(binaryScan);
        BinaryScanCodeLocationCreationRequest uploadRequest = new BinaryScanCodeLocationCreationRequest(binaryScanBatchRunner, uploadBatch);

        return uploadBinaryScanAndWait(uploadRequest, timeoutInSeconds);
    }

    public void waitForBinaryScanUpload(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames, int expectedNotificationCount, long timeoutInSeconds)
        throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds);
    }

}
