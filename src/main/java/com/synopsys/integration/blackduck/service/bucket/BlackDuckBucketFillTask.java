/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.util.Optional;
import java.util.concurrent.Callable;

import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.service.BlackDuckService;

public class BlackDuckBucketFillTask<T extends BlackDuckResponse> implements Callable<Optional<T>> {
    private final BlackDuckService blackDuckService;
    private final BlackDuckBucket blackDuckBucket;
    private final UriSingleResponse<T> uriSingleResponse;

    public BlackDuckBucketFillTask(final BlackDuckService blackDuckService, final BlackDuckBucket blackDuckBucket, final UriSingleResponse<T> uriSingleResponse) {
        this.blackDuckService = blackDuckService;
        this.blackDuckBucket = blackDuckBucket;
        this.uriSingleResponse = uriSingleResponse;
    }

    @Override
    public Optional<T> call() {
        if (!blackDuckBucket.contains(uriSingleResponse.getUri())) {
            try {
                final T blackDuckResponse = blackDuckService.getResponse(uriSingleResponse);
                blackDuckBucket.addValid(uriSingleResponse.getUri(), blackDuckResponse);
                return Optional.of(blackDuckResponse);
            } catch (final Exception e) {
                // it is up to the consumer of the bucket to log or handle any/all Exceptions
                blackDuckBucket.addError(uriSingleResponse.getUri(), e);
                return Optional.empty();
            }
        }
        return Optional.ofNullable(blackDuckBucket.get(uriSingleResponse.getUri(), uriSingleResponse.getResponseClass()));
    }

}
