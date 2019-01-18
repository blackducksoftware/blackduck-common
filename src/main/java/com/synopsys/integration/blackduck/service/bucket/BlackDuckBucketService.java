/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.blackduck.service.bucket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class BlackDuckBucketService extends DataService {
    private final Optional<ExecutorService> executorService;

    public BlackDuckBucketService(final BlackDuckService blackDuckService, final IntLogger logger) {
        super(blackDuckService, logger);
        executorService = Optional.empty();
    }

    public BlackDuckBucketService(final BlackDuckService blackDuckService, final IntLogger logger, final ExecutorService executorService) {
        super(blackDuckService, logger);
        this.executorService = Optional.of(executorService);
    }

    public BlackDuckBucket startTheBucket(final List<UriSingleResponse<? extends BlackDuckResponse>> uriSingleResponses) throws IntegrationException {
        final BlackDuckBucket blackDuckBucket = new BlackDuckBucket();
        addToTheBucket(blackDuckBucket, uriSingleResponses);
        return blackDuckBucket;
    }

    public <T extends BlackDuckResponse> void addToTheBucket(final BlackDuckBucket blackDuckBucket, final String uri, final Class<T> responseClass) throws IntegrationException {
        final List<UriSingleResponse<? extends BlackDuckResponse>> uriSingleResponses = new ArrayList<>();
        uriSingleResponses.add(new UriSingleResponse<>(uri, responseClass));
        addToTheBucket(blackDuckBucket, uriSingleResponses);
    }

    public void addToTheBucket(final BlackDuckBucket blackDuckBucket, final Map<String, Class<? extends BlackDuckResponse>> uriToResponseClass) throws IntegrationException {
        final List<UriSingleResponse<? extends BlackDuckResponse>> uriSingleResponses = new ArrayList<>();
        uriToResponseClass.forEach((key, value) -> {
            uriSingleResponses.add(new UriSingleResponse<>(key, value));
        });
        addToTheBucket(blackDuckBucket, uriSingleResponses);
    }

    public void addToTheBucket(final BlackDuckBucket blackDuckBucket, final List<UriSingleResponse<? extends BlackDuckResponse>> uriSingleResponses) throws IntegrationException {
        final List<BlackDuckBucketFillTask> taskList = uriSingleResponses.stream().map(uriSingleResponse -> {
            return new BlackDuckBucketFillTask(blackDuckService, blackDuckBucket, uriSingleResponse);
        }).collect(Collectors.toList());
        if (executorService.isPresent()) {
            // NOTE: it is up to the user of the bucket service to shutdown the executor
            taskList.forEach(task -> {
                executorService.get().execute(task);
            });
        } else {
            taskList.forEach(task -> {
                task.run();
            });
        }
    }

}
