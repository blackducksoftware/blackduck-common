/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.intelligentpersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.blackduck.integration.blackduck.bdio2.Bdio2FileUploadService;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatch;
import com.blackduck.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.blackduck.integration.blackduck.codelocation.upload.UploadOutput;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.log.IntLogger;

public class IntelligentPersistenceBatchRunner {
    private final IntLogger logger;
    private final ExecutorService executorService;
    private final Bdio2FileUploadService bdio2FileUploadService;

    public IntelligentPersistenceBatchRunner(final IntLogger logger, final ExecutorService executorService, final Bdio2FileUploadService bdio2FileUploadService) {
        this.logger = logger;
        this.executorService = executorService;
        this.bdio2FileUploadService = bdio2FileUploadService;
    }

    public UploadBatchOutput executeUploads(UploadBatch uploadBatch, long timeout, long clientStartTime) throws BlackDuckIntegrationException {
        logger.info("Starting the codelocation file uploads.");
        UploadBatchOutput uploadBatchOutput = uploadTargets(uploadBatch, timeout, clientStartTime);
        logger.info("Completed the codelocation file uploads.");

        return uploadBatchOutput;
    }

    private UploadBatchOutput uploadTargets(UploadBatch uploadBatch, long timeout, long clientStartTime) throws BlackDuckIntegrationException {
        List<UploadOutput> uploadOutputs = new ArrayList<>();

        try {
            List<IntelligentPersistenceCallable> callables = createCallables(uploadBatch, timeout, clientStartTime);
            List<Future<UploadOutput>> submitted = new ArrayList<>();
            for (IntelligentPersistenceCallable callable : callables) {
                submitted.add(executorService.submit(callable));
            }
            for (Future<UploadOutput> future : submitted) {
                UploadOutput uploadOutput = future.get();
                uploadOutputs.add(uploadOutput);
            }
        } catch (Exception e) {
            throw new BlackDuckIntegrationException(String.format("Encountered a problem uploading a file: %s", e.getMessage()), e);
        }

        return new UploadBatchOutput(uploadOutputs);
    }

    private List<IntelligentPersistenceCallable> createCallables(UploadBatch uploadBatch, long timeout, long clientStartTime) {
        List<IntelligentPersistenceCallable> callables = uploadBatch.getUploadTargets()
                                                             .stream()
                                                             .map(uploadTarget -> new IntelligentPersistenceCallable(bdio2FileUploadService, uploadTarget, timeout, clientStartTime))
                                                             .collect(Collectors.toList());

        return callables;
    }

}
