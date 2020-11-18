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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.log.IntLogger;

public class BlackDuckBucketService extends DataService {
    private final ExecutorService executorService;

    public BlackDuckBucketService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger, ExecutorService executorService) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.executorService = executorService;
    }

    public <T extends BlackDuckResponse> Future<Optional<T>> addToTheBucket(BlackDuckBucket blackDuckBucket, String uri, Class<T> responseClass) {
        LinkSingleResponse<? extends BlackDuckResponse> linkSingleResponse = new LinkSingleResponse<>(uri, responseClass);
        BlackDuckBucketFillTask blackDuckBucketFillTask = new BlackDuckBucketFillTask(blackDuckApiClient, blackDuckBucket, linkSingleResponse);
        return executorService.submit(blackDuckBucketFillTask);
    }

}
