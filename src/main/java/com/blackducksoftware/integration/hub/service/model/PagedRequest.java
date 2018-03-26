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
package com.blackducksoftware.integration.hub.service.model;

import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.hub.request.Request;

public class PagedRequest {
    private final Request.Builder requestBuilder;
    private final int offset;
    private final int limit;

    public PagedRequest(final Request.Builder requestBuilder) {
        this.requestBuilder = requestBuilder;
        int offset = 0;
        int limit = 100;
        if (requestBuilder.getQueryParameters() != null) {
            if (requestBuilder.getQueryParameters().containsKey("offset")) {
                offset = NumberUtils.toInt(requestBuilder.getQueryParameters().get("offset"), 0);
            }
            if (requestBuilder.getQueryParameters().containsKey("limit")) {
                limit = NumberUtils.toInt(requestBuilder.getQueryParameters().get("limit"), 100);
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
        request.getQueryParameters().put("limit", String.valueOf(getLimit()));
        request.getQueryParameters().put("offset", String.valueOf(getOffset()));
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
