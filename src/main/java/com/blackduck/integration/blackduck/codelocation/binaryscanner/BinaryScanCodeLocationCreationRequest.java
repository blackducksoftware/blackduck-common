/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.binaryscanner;

import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationRequest;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;

public class BinaryScanCodeLocationCreationRequest extends CodeLocationCreationRequest<BinaryScanBatchOutput> {
    private final BinaryScanBatchRunner binaryScanBatchRunner;
    private final BinaryScanBatch binaryScanBatch;

    public BinaryScanCodeLocationCreationRequest(BinaryScanBatchRunner binaryScanBatchRunner, BinaryScanBatch binaryScanBatch) {
        this.binaryScanBatchRunner = binaryScanBatchRunner;
        this.binaryScanBatch = binaryScanBatch;
    }

    @Override
    public BinaryScanBatchOutput executeRequest() throws BlackDuckIntegrationException {
        return binaryScanBatchRunner.executeUploads(binaryScanBatch);
    }

}
