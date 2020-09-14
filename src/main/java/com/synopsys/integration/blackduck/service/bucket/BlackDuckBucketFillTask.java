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
package com.synopsys.integration.blackduck.service.bucket;

import java.util.Optional;
import java.util.concurrent.Callable;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.service.BlackDuckService;

public class BlackDuckBucketFillTask<T extends BlackDuckResponse> implements Callable<Optional<T>> {
    private final BlackDuckService blackDuckService;
    private final BlackDuckBucket blackDuckBucket;
    private final LinkSingleResponse<T> linkSingleResponse;

    public BlackDuckBucketFillTask(BlackDuckService blackDuckService, BlackDuckBucket blackDuckBucket, LinkSingleResponse<T> linkSingleResponse) {
        this.blackDuckService = blackDuckService;
        this.blackDuckBucket = blackDuckBucket;
        this.linkSingleResponse = linkSingleResponse;
    }

    @Override
    public Optional<T> call() {
        if (!blackDuckBucket.contains(linkSingleResponse.getLink())) {
            try {
                T blackDuckResponse = blackDuckService.getResponse(linkSingleResponse);
                blackDuckBucket.addValid(linkSingleResponse.getLink(), blackDuckResponse);
                return Optional.of(blackDuckResponse);
            } catch (Exception e) {
                // it is up to the consumer of the bucket to log or handle any/all Exceptions
                blackDuckBucket.addError(linkSingleResponse.getLink(), e);
                return Optional.empty();
            }
        }
        return Optional.ofNullable(blackDuckBucket.get(linkSingleResponse.getLink(), linkSingleResponse.getResponseClass()));
    }

}
