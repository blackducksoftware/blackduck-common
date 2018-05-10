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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;

public class HubBucket {
    private final Map<String, HubBucketItem<HubResponse>> bucket = new ConcurrentHashMap<>();

    public boolean contains(final String uri) {
        return bucket.containsKey(uri);
    }

    public Set<String> getAvailableUris() {
        return bucket.keySet();
    }

    public HubBucketItem<HubResponse> get(final String uri) {
        return bucket.get(uri);
    }

    public <T extends HubResponse> T get(final String uri, final Class<T> responseClass) {
        final UriSingleResponse<T> uriSingleResponse = new UriSingleResponse<>(uri, responseClass);
        return get(uriSingleResponse);
    }

    public <T extends HubResponse> T get(final UriSingleResponse<T> uriSingleResponse) {
        final String uri = uriSingleResponse.uri;
        if (contains(uri)) {
            final HubBucketItem<HubResponse> bucketItem = get(uri);
            if (bucketItem.hasValidResponse() && bucketItem.getHubResponse().isPresent() && bucketItem.getHubResponse().get().getClass().equals(uriSingleResponse.responseClass)) {
                return getResponseFromBucket(bucketItem);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends HubResponse> T getResponseFromBucket(final HubBucketItem<HubResponse> bucketItem) {
        // the mapping of uri -> response type are assumed to be correct, so returning T is possible
        return (T) bucketItem.getHubResponse().orElse(null);
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
