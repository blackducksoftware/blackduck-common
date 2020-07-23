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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NoThreadExecutorService;

public class BinaryScanBatchRunner {
    private final IntLogger logger;
    private final BlackDuckService blackDuckService;
    private final ExecutorService executorService;

    public BinaryScanBatchRunner(IntLogger logger, BlackDuckService blackDuckService, ExecutorService executorService) {
        this.logger = logger;
        this.blackDuckService = blackDuckService;
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
                        .map(binaryScan -> new BinaryScanCallable(blackDuckService, binaryScan))
                        .collect(Collectors.toList());

        return callables;
    }

}
