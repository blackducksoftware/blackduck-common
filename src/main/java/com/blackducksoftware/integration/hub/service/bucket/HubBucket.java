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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.blackducksoftware.integration.hub.api.core.HubResponse;

public class HubBucket {
    private final Map<String, HubBucketItem<HubResponse>> bucket = new HashMap<>();

    public boolean contains(final String uri) {
        return bucket.containsKey(uri);
    }

    public Set<String> getAvailableUris() {
        return bucket.keySet();
    }

    public HubBucketItem<HubResponse> get(final String uri) {
        return bucket.get(uri);
    }

    public Optional<HubResponse> getResponse(final String uri) {
        return bucket.get(uri).getHubResponse();
    }

    public Optional<Exception> getError(final String uri) {
        return bucket.get(uri).getE();
    }

    public void addValid(final String uri, final HubResponse hubResponse) {
        bucket.put(uri, new HubBucketItem<>(uri, hubResponse));
    }

    public void addError(final String uri, final Exception e) {
        bucket.put(uri, new HubBucketItem<>(uri, e));
    }

    public HubBucketItem<HubResponse> remove(final String uri) {
        return bucket.remove(uri);
    }

}
