/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.bdio2legacy;

import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationRequest;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatch;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;

public class Bdio2UploadCodeLocationCreationRequest extends CodeLocationCreationRequest<UploadBatchOutput> {
    private final UploadBdio2BatchRunner uploadBdio2BatchRunner;
    private final UploadBatch uploadBatch;

    public Bdio2UploadCodeLocationCreationRequest(UploadBdio2BatchRunner uploadBdio2BatchRunner, UploadBatch uploadBatch) {
        this.uploadBdio2BatchRunner = uploadBdio2BatchRunner;
        this.uploadBatch = uploadBatch;
    }

    @Override
    public UploadBatchOutput executeRequest() throws BlackDuckIntegrationException {
        return uploadBdio2BatchRunner.executeUploads(uploadBatch);
    }

}
