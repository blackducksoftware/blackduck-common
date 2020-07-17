/**
 * blackduck-common
 * <p>
 * Copyright (c) 2020 Synopsys, Inc.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.service.bucket;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlackDuckBucket {
    private final Map<String, BlackDuckBucketItem<BlackDuckResponse>> bucket = new ConcurrentHashMap<>();

    public boolean contains(String uri) {
        return bucket.containsKey(uri);
    }

    public Set<String> getAvailableUris() {
        return bucket.keySet();
    }

    public BlackDuckBucketItem<BlackDuckResponse> get(String uri) {
        return bucket.get(uri);
    }

    public <T extends BlackDuckResponse> T get(String uri, Class<T> responseClass) {
        LinkSingleResponse<T> linkSingleResponse = new LinkSingleResponse<>(uri, responseClass);
        return get(linkSingleResponse);
    }

    public <T extends BlackDuckResponse> T get(LinkSingleResponse<T> linkSingleResponse) {
        String uri = linkSingleResponse.getLink();
        if (contains(uri)) {
            BlackDuckBucketItem<BlackDuckResponse> bucketItem = get(uri);
            if (bucketItem.hasValidResponse()) {
                Optional<BlackDuckResponse> optionalBlackDuckResponse = bucketItem.getBlackDuckResponse();
                if (optionalBlackDuckResponse.isPresent()) {
                    BlackDuckResponse blackDuckResponse = optionalBlackDuckResponse.get();
                    if (blackDuckResponse.getClass().equals(linkSingleResponse.getResponseClass())) {
                        return getResponseFromBucket(bucketItem);
                    }
                }
            }
        }
        return null;
    }

    private <T extends BlackDuckResponse> T getResponseFromBucket(BlackDuckBucketItem<BlackDuckResponse> bucketItem) {
        // the mapping of uri -> response type are assumed to be correct, so returning T is possible
        return (T) bucketItem.getBlackDuckResponse().orElse(null);
    }

    public Optional<BlackDuckResponse> getResponse(String uri) {
        return bucket.get(uri).getBlackDuckResponse();
    }

    public Optional<Exception> getError(String uri) {
        return bucket.get(uri).getE();
    }

    public void addValid(String uri, BlackDuckResponse blackDuckResponse) {
        bucket.put(uri, new BlackDuckBucketItem<>(uri, blackDuckResponse));
    }

    public void addError(String uri, Exception e) {
        bucket.put(uri, new BlackDuckBucketItem<>(uri, e));
    }

    public boolean hasAnyErrors() {
        return bucket.values()
                .stream()
                .filter(BlackDuckBucketItem::hasException)
                .findFirst()
                .isPresent();
    }

    public BlackDuckBucketItem<BlackDuckResponse> remove(String uri) {
        return bucket.remove(uri);
    }

}
