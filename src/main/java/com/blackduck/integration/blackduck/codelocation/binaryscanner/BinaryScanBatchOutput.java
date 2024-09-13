/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.binaryscanner;

import com.blackduck.integration.blackduck.codelocation.CodeLocationBatchOutput;
import com.blackduck.integration.blackduck.codelocation.Result;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.log.IntLogger;

import java.util.List;

public class BinaryScanBatchOutput extends CodeLocationBatchOutput<BinaryScanOutput> {
    public BinaryScanBatchOutput(List<BinaryScanOutput> outputs) {
        super(outputs);
    }

    public void throwExceptionForError(IntLogger logger) throws BlackDuckIntegrationException {
        for (BinaryScanOutput binaryScanOutput : this) {
            if (binaryScanOutput.getResult() == Result.FAILURE) {
                String uploadErrorMessage = "Error when uploading binary scan: %s" + binaryScanOutput.getErrorMessage().orElse(binaryScanOutput.getStatusMessage());
                logger.error(uploadErrorMessage);
                throw new BlackDuckIntegrationException(uploadErrorMessage);
            }
        }
    }

}
