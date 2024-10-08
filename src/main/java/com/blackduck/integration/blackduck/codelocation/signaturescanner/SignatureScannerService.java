/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.signaturescanner;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationData;
import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationService;
import com.blackduck.integration.blackduck.codelocation.CodeLocationWaitResult;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.blackduck.service.model.NotificationTaskRange;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.util.NameVersion;

import java.util.Set;

public class SignatureScannerService extends DataService {
    private final ScanBatchRunner scanBatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public SignatureScannerService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger,
                                   ScanBatchRunner scanBatchRunner, CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.scanBatchRunner = scanBatchRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public SignatureScannerCodeLocationCreationRequest createScanRequest(ScanBatch scanBatch) {
        return new SignatureScannerCodeLocationCreationRequest(scanBatchRunner, scanBatch);
    }

    public CodeLocationCreationData<ScanBatchOutput> performSignatureScan(SignatureScannerCodeLocationCreationRequest scanRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(scanRequest);
    }

    public CodeLocationCreationData<ScanBatchOutput> performSignatureScan(ScanBatch scanBatch) throws IntegrationException {
        SignatureScannerCodeLocationCreationRequest scanRequest = new SignatureScannerCodeLocationCreationRequest(scanBatchRunner, scanBatch);

        return performSignatureScan(scanRequest);
    }

    public ScanBatchOutput performSignatureScanAndWait(SignatureScannerCodeLocationCreationRequest scanRequest, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(scanRequest, timeoutInSeconds);
    }

    public ScanBatchOutput performSignatureScanAndWait(ScanBatch scanBatch, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        SignatureScannerCodeLocationCreationRequest scanRequest = new SignatureScannerCodeLocationCreationRequest(scanBatchRunner, scanBatch);

        return performSignatureScanAndWait(scanRequest, timeoutInSeconds);
    }

    public CodeLocationWaitResult waitForSignatureScan(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames,
        int expectedNotificationCount, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds);
    }

}
