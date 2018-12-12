/**
 * blackduck-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class SignatureScannerService extends DataService {
    private final ScanBatchRunner scanBatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public SignatureScannerService(final BlackDuckService blackDuckService, final IntLogger logger,
            final ScanBatchRunner scanBatchRunner, final CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckService, logger);
        this.scanBatchRunner = scanBatchRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public SignatureScannerCodeLocationCreationRequest createScanRequest(final ScanBatch scanBatch) {
        return new SignatureScannerCodeLocationCreationRequest(scanBatchRunner, scanBatch);
    }

    public CodeLocationCreationData<ScanBatchOutput> performSignatureScan(final SignatureScannerCodeLocationCreationRequest scanRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(scanRequest);
    }

    public CodeLocationCreationData<ScanBatchOutput> performSignatureScan(final ScanBatch scanBatch) throws IntegrationException {
        final SignatureScannerCodeLocationCreationRequest scanRequest = new SignatureScannerCodeLocationCreationRequest(scanBatchRunner, scanBatch);

        return performSignatureScan(scanRequest);
    }

    public ScanBatchOutput performSignatureScanAndWait(final SignatureScannerCodeLocationCreationRequest scanRequest, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(scanRequest, timeoutInSeconds);
    }

    public ScanBatchOutput performSignatureScanAndWait(final ScanBatch scanBatch, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        final SignatureScannerCodeLocationCreationRequest scanRequest = new SignatureScannerCodeLocationCreationRequest(scanBatchRunner, scanBatch);

        return performSignatureScanAndWait(scanRequest, timeoutInSeconds);
    }

    public void waitForBdioUpload(final NotificationTaskRange notificationTaskRange, final Set<String> codeLocationNames, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, codeLocationNames, timeoutInSeconds);
    }

}
