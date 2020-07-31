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
package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;

import java.util.Set;

public class BinaryScanUploadService extends DataService {
    private final BinaryScanBatchRunner binaryScanBatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public BinaryScanUploadService(BlackDuckService blackDuckService, RequestFactory requestFactory, IntLogger logger, BinaryScanBatchRunner binaryScanBatchRunner, CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckService, requestFactory, logger);
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

    public void waitForBinaryScanUpload(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames, int expectedNotificationCount, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds);
    }

}
