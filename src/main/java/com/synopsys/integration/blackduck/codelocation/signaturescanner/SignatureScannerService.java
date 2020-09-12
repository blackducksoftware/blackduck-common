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
package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import java.util.Set;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.CodeLocationWaitResult;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;

public class SignatureScannerService extends DataService {
    private final ScanBatchRunner scanBatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public SignatureScannerService(BlackDuckService blackDuckService, RequestFactory requestFactory, IntLogger logger,
        ScanBatchRunner scanBatchRunner, CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckService, requestFactory, logger);
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
