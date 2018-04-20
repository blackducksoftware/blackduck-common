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
package com.blackducksoftware.integration.hub.service.bucket;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.service.DataService;
import com.blackducksoftware.integration.hub.service.HubService;

public class HubBucketService extends DataService {
    public HubBucketService(final HubService hubService) {
        super(hubService);
    }

    public HubBucket startTheBucket(final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses) throws IntegrationException {
        final HubBucket hubBucket = new HubBucket();
        addToTheBucket(hubBucket, uriSingleResponses);
        return hubBucket;
    }

    public void addToTheBucket(final HubBucket hubBucket, final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses) throws IntegrationException {
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
        final List<Callable<Void>> taskList = uriSingleResponses.stream().map(uriSingleResponse -> {
            return new HubBucketFillTask(hubBucket, uriSingleResponse);
        }).collect(Collectors.toList());
        try {
            executorService.invokeAll(taskList);
        } catch (final InterruptedException ex) {
            throw new IntegrationException("Exception adding to the HubBucket", ex);
        } finally {
            executorService.shutdown();
        }
    }

    public void addToTheBucketSynchronous(final HubBucket hubBucket, final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses) {
        for (final UriSingleResponse<? extends HubResponse> uriSingleResponse : uriSingleResponses) {
            if (!hubBucket.contains(uriSingleResponse.uri)) {
                try {
                    final HubResponse hubResponse = hubService.getResponse(uriSingleResponse);
                    hubBucket.addValid(uriSingleResponse.uri, hubResponse);
                } catch (final Exception e) {
                    // it is up to the consumer of the bucket to log or handle any/all Exceptions
                    hubBucket.addError(uriSingleResponse.uri, e);
                }
            }
        }
    }

    private class HubBucketFillTask implements Callable<Void> {
        private final HubBucket hubBucket;
        private final UriSingleResponse<? extends HubResponse> uriSingleResponse;

        public HubBucketFillTask(final HubBucket hubBucket, final UriSingleResponse<? extends HubResponse> uriSingleResponse) {
            this.hubBucket = hubBucket;
            this.uriSingleResponse = uriSingleResponse;
        }

        @Override
        public Void call() {
            if (!hubBucket.contains(uriSingleResponse.uri)) {
                try {
                    final HubResponse hubResponse = hubService.getResponse(uriSingleResponse);
                    hubBucket.addValid(uriSingleResponse.uri, hubResponse);
                } catch (final Exception e) {
                    // it is up to the consumer of the bucket to log or handle any/all Exceptions
                    hubBucket.addError(uriSingleResponse.uri, e);
                }
            }
            return null;
        }

    }
}
