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
package com.synopsys.integration.blackduck.service.bucket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.hub.api.core.HubResponse;
import com.synopsys.integration.log.IntLogger;

public class HubBucketService extends DataService {
    private final Optional<ExecutorService> executorService;

    public HubBucketService(final HubService hubService, final IntLogger logger) {
        super(hubService, logger);
        executorService = Optional.empty();
    }

    public HubBucketService(final HubService hubService, final IntLogger logger, final ExecutorService executorService) {
        super(hubService, logger);
        this.executorService = Optional.of(executorService);
    }

    public HubBucket startTheBucket(final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses) throws IntegrationException {
        final HubBucket hubBucket = new HubBucket();
        addToTheBucket(hubBucket, uriSingleResponses);
        return hubBucket;
    }

    public <T extends HubResponse> void addToTheBucket(final HubBucket hubBucket, final String uri, final Class<T> responseClass) throws IntegrationException {
        final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses = new ArrayList<>();
        uriSingleResponses.add(new UriSingleResponse<>(uri, responseClass));

        addToTheBucket(hubBucket, uriSingleResponses);
    }

    public void addToTheBucket(final HubBucket hubBucket, final Map<String, Class<? extends HubResponse>> uriToResponseClass) throws IntegrationException {
        final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses = new ArrayList<>();
        uriToResponseClass.forEach((key, value) -> {
            uriSingleResponses.add(new UriSingleResponse<>(key, value));
        });
    }

    public void addToTheBucket(final HubBucket hubBucket, final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses) throws IntegrationException {
        final List<HubBucketFillTask> taskList = uriSingleResponses
                .stream()
                .map(uriSingleResponse -> {
                    return new HubBucketFillTask(hubService, hubBucket, uriSingleResponse);
                })
                .collect(Collectors.toList());
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
