/**
 * hub-common
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
package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.log.IntLogger;

public class UploadRunner {
    private final IntLogger logger;
    private final HubService hubService;
    private final Optional<ExecutorService> optionalExecutorService;

    public UploadRunner(final IntLogger logger, final HubService hubService) {
        this.logger = logger;
        this.hubService = hubService;
        optionalExecutorService = Optional.empty();
    }

    public UploadRunner(final IntLogger logger, final HubService hubService, final ExecutorService executorService) {
        this.logger = logger;
        this.hubService = hubService;
        optionalExecutorService = Optional.of(executorService);
    }

    public UploadBatchOutput executeUploads(final UploadBatch uploadBatch) throws HubIntegrationException {
        logger.info("Starting the codelocation file uploads.");
        final UploadBatchOutput uploadBatchOutput = uploadTargets(uploadBatch);
        logger.info("Completed the codelocation file uploads.");

        return uploadBatchOutput;
    }

    private UploadBatchOutput uploadTargets(final UploadBatch uploadBatch) throws HubIntegrationException {
        final List<UploadOutput> uploadOutputs = new ArrayList<>();

        try {
            final List<UploadCallable> callables = createCallables(uploadBatch);
            if (optionalExecutorService.isPresent()) {
                final ExecutorService executorService = optionalExecutorService.get();
                final List<Future<UploadOutput>> submitted = new ArrayList<>();
                for (final UploadCallable callable : callables) {
                    submitted.add(executorService.submit(callable));
                }
                for (final Future<UploadOutput> future : submitted) {
                    final UploadOutput uploadOutput = future.get();
                    uploadOutputs.add(uploadOutput);
                }
            } else {
                for (final UploadCallable callable : callables) {
                    uploadOutputs.add(callable.call());
                }
            }
        } catch (final Exception e) {
            throw new HubIntegrationException(String.format("Encountered a problem uploading a file: %s", e.getMessage()), e);
        }

        return new UploadBatchOutput(uploadOutputs);
    }

    private List<UploadCallable> createCallables(final UploadBatch uploadBatch) {
        final List<UploadCallable> callables = uploadBatch.getUploadTargets()
                                                       .stream()
                                                       .map(uploadTarget -> new UploadCallable(hubService, uploadTarget))
                                                       .collect(Collectors.toList());

        return callables;
    }

}
