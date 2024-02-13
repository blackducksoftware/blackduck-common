/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.log.IntLogger;

public class BinaryScanBatchRunner {
    private final IntLogger logger;
    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;
    private final ExecutorService executorService;

    public BinaryScanBatchRunner(IntLogger logger, BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, ExecutorService executorService) {
        this.logger = logger;
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
        this.executorService = executorService;
    }

    public BinaryScanBatchOutput executeUploads(BinaryScanBatch binaryScanBatch) throws BlackDuckIntegrationException {
        logger.info("Starting the binary scan file uploads.");
        BinaryScanBatchOutput binaryScanBatchOutput = uploadFiles(binaryScanBatch);
        logger.info("Completed the binary scan file uploads.");

        return binaryScanBatchOutput;
    }

    private BinaryScanBatchOutput uploadFiles(BinaryScanBatch binaryScanBatch) throws BlackDuckIntegrationException {
        List<BinaryScanOutput> uploadOutputs = new ArrayList<>();

        try {
            List<BinaryScanCallable> callables = createCallables(binaryScanBatch);
            List<Future<BinaryScanOutput>> submitted = new ArrayList<>();
            for (BinaryScanCallable callable : callables) {
                submitted.add(executorService.submit(callable));
            }
            for (Future<BinaryScanOutput> future : submitted) {
                BinaryScanOutput uploadOutput = future.get();
                uploadOutputs.add(uploadOutput);
            }
        } catch (Exception e) {
            throw new BlackDuckIntegrationException(String.format("Encountered a problem uploading a binary file: %s", e.getMessage()), e);
        }

        return new BinaryScanBatchOutput(uploadOutputs);
    }

    private List<BinaryScanCallable> createCallables(BinaryScanBatch uploadBatch) {
        List<BinaryScanCallable> callables =
            uploadBatch
                .getBinaryScans()
                .stream()
                .map(binaryScan -> new BinaryScanCallable(blackDuckApiClient, apiDiscovery, binaryScan))
                .collect(Collectors.toList());

        return callables;
    }

}
