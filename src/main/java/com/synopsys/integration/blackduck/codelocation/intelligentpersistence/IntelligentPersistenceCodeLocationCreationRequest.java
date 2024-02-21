/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.intelligentpersistence;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;

public class IntelligentPersistenceCodeLocationCreationRequest extends CodeLocationCreationRequest<UploadBatchOutput> {
    private final IntelligentPersistenceBatchRunner uploadBatchRunner;
    private final UploadBatch uploadBatch;
    private final long timeout;
    private final long clientStartTime;

    public IntelligentPersistenceCodeLocationCreationRequest(final IntelligentPersistenceBatchRunner uploadBatchRunner, final UploadBatch uploadBatch, final long timeout, final long clientStartTime) {
        this.uploadBatchRunner = uploadBatchRunner;
        this.uploadBatch = uploadBatch;
        this.timeout = timeout;
        this.clientStartTime = clientStartTime;
    }

    @Override
    public UploadBatchOutput executeRequest() throws BlackDuckIntegrationException {
        return uploadBatchRunner.executeUploads(uploadBatch, timeout, clientStartTime);
    }
}
