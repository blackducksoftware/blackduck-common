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

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.service.HubService;

public class HubBucketFillTask implements Runnable {
    private final HubService hubService;
    private final HubBucket hubBucket;
    private final UriSingleResponse<? extends HubResponse> uriSingleResponse;

    public HubBucketFillTask(final HubService hubService, final HubBucket hubBucket, final UriSingleResponse<? extends HubResponse> uriSingleResponse) {
        this.hubService = hubService;
        this.hubBucket = hubBucket;
        this.uriSingleResponse = uriSingleResponse;
    }

    @Override
    public void run() {
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
