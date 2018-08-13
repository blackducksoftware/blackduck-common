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
package com.synopsys.integration.blackduck.service.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

import com.synopsys.integration.rest.request.Request;

public class PagedRequest {
    private final Request.Builder requestBuilder;
    private final int offset;
    private final int limit;

    public PagedRequest(final Request.Builder requestBuilder) {
        this.requestBuilder = requestBuilder;
        int offset = 0;
        int limit = 100;
        if (requestBuilder.getQueryParameters() != null) {
            // we know that limit and offset are only ever set as single values
            // so iterator().next() is a reasonable way to get them out of the
            // Set
            if (requestBuilder.getQueryParameters().containsKey("offset")) {
                offset = NumberUtils.toInt(requestBuilder.getQueryParameters().get("offset").iterator().next(), 0);
            }
            if (requestBuilder.getQueryParameters().containsKey("limit")) {
                limit = NumberUtils.toInt(requestBuilder.getQueryParameters().get("limit").iterator().next(), 100);
            }
        }

        this.offset = offset;
        this.limit = limit;
    }

    public PagedRequest(final Request.Builder requestBuilder, final int offset, final int limit) {
        this.requestBuilder = requestBuilder;
        this.offset = offset;
        this.limit = limit;
    }

    public Request createRequest() {
        final Request request = requestBuilder.build();
        final Set<String> limitValue = new HashSet<>();
        limitValue.add(String.valueOf(getLimit()));

        final Set<String> offsetValue = new HashSet<>();
        offsetValue.add(String.valueOf(getOffset()));

        request.getQueryParameters().put("limit", limitValue);
        request.getQueryParameters().put("offset", offsetValue);
        return request;
    }

    public Request.Builder getRequestBuilder() {
        return requestBuilder;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

}
