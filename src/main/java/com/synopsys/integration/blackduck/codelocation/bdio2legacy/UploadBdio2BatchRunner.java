/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdio2legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.bdio2.Bdio2StreamUploader;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.upload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.log.IntLogger;

public class UploadBdio2BatchRunner {
    private final IntLogger logger;
    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;
    private final ExecutorService executorService;

    public UploadBdio2BatchRunner(IntLogger logger, BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, ExecutorService executorService) {
        this.logger = logger;
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
        this.executorService = executorService;
    }

    public UploadBatchOutput executeUploads(UploadBatch uploadBatch) throws BlackDuckIntegrationException {
        logger.info("Starting the codelocation file uploads.");
        UploadBatchOutput uploadBatchOutput = uploadTargets(uploadBatch);
        logger.info("Completed the codelocation file uploads.");

        return uploadBatchOutput;
    }

    private UploadBatchOutput uploadTargets(UploadBatch uploadBatch) throws BlackDuckIntegrationException {
        List<UploadOutput> uploadOutputs = new ArrayList<>();

        try {
            List<UploadBdio2Callable> callables = createCallables(uploadBatch);
            List<Future<UploadOutput>> submitted = new ArrayList<>();
            for (UploadBdio2Callable callable : callables) {
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

    private List<UploadBdio2Callable> createCallables(UploadBatch uploadBatch) {
        return uploadBatch.getUploadTargets()
            .stream()
            .map(uploadTarget -> new UploadBdio2Callable(blackDuckApiClient, apiDiscovery, uploadTarget, createEditor(uploadTarget)))
            .collect(Collectors.toList());
    }

    private BlackDuckRequestBuilderEditor createEditor(UploadTarget uploadTarget) {
        return uploadTarget.getProjectAndVersion()
            .map(projectVersion -> (BlackDuckRequestBuilderEditor) builder -> {
                builder
                    .addHeader(Bdio2StreamUploader.PROJECT_NAME_HEADER, projectVersion.getName())
                    .addHeader(Bdio2StreamUploader.VERSION_NAME_HEADER, projectVersion.getVersion());
            })
            .orElse(noOp -> {});
    }
}
