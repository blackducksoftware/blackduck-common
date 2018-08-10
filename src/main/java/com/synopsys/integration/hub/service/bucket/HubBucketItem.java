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
package com.synopsys.integration.hub.service.bucket;

import java.util.Optional;

import com.synopsys.integration.hub.api.core.HubResponse;
import com.synopsys.integration.util.Stringable;

public class HubBucketItem<T extends HubResponse> extends Stringable {
    private final String uri;
    private final Optional<T> hubResponse;
    private final Optional<Exception> e;

    public HubBucketItem(final String uri, final T hubResponse) {
        this.uri = uri;
        this.hubResponse = Optional.of(hubResponse);
        this.e = Optional.empty();
    }

    public HubBucketItem(final String uri, final Exception e) {
        this.uri = uri;
        this.hubResponse = Optional.empty();
        this.e = Optional.of(e);
    }

    public boolean hasException() {
        return e.isPresent();
    }

    public boolean hasValidResponse() {
        return !e.isPresent();
    }

    public String getUri() {
        return uri;
    }

    public Optional<T> getHubResponse() {
        return hubResponse;
    }

    public Optional<Exception> getE() {
        return e;
    }

}
