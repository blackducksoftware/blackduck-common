/**
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.log.IntLogger;

public class UploadBatchRunner {
    private final IntLogger logger;
    private final BlackDuckApiClient blackDuckApiClient;
    private final BlackDuckRequestFactory blackDuckRequestFactory;
    private final ExecutorService executorService;

    public UploadBatchRunner(IntLogger logger, BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, ExecutorService executorService) {
        this.logger = logger;
        this.blackDuckApiClient = blackDuckApiClient;
        this.blackDuckRequestFactory = blackDuckRequestFactory;
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
            List<UploadCallable> callables = createCallables(uploadBatch);
            List<Future<UploadOutput>> submitted = new ArrayList<>();
            for (UploadCallable callable : callables) {
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

    private List<UploadCallable> createCallables(UploadBatch uploadBatch) {
        List<UploadCallable> callables = uploadBatch.getUploadTargets()
                                             .stream()
                                             .map(uploadTarget -> new UploadCallable(blackDuckApiClient, blackDuckRequestFactory, uploadTarget))
                                             .collect(Collectors.toList());

        return callables;
    }

}
