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
package com.synopsys.integration.blackduck.http;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

import com.synopsys.integration.rest.request.Request;

public class PagedRequest {
    private final BlackDuckRequestBuilder requestBuilder;
    private final int offset;
    private final int limit;

    public PagedRequest(BlackDuckRequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
        int offset = RequestFactory.DEFAULT_OFFSET;
        int limit = RequestFactory.DEFAULT_LIMIT;
        if (requestBuilder.getQueryParameters() != null) {
            if (requestBuilder.getQueryParameters().containsKey(RequestFactory.OFFSET_PARAMETER)) {
                offset = NumberUtils.toInt(requestBuilder.getQueryParameters().get(RequestFactory.OFFSET_PARAMETER).stream().findFirst().orElse(null), offset);
            }
            if (requestBuilder.getQueryParameters().containsKey(RequestFactory.LIMIT_PARAMETER)) {
                limit = NumberUtils.toInt(requestBuilder.getQueryParameters().get(RequestFactory.LIMIT_PARAMETER).stream().findFirst().orElse(null), limit);
            }
        }

        this.offset = offset;
        this.limit = limit;
    }

    public PagedRequest(BlackDuckRequestBuilder requestBuilder, int offset, int limit) {
        this.requestBuilder = requestBuilder;
        this.offset = offset;
        this.limit = limit;
    }

    public Request createRequest() {
        Request request = requestBuilder.getRequestBuilder().build();
        Set<String> limitValue = new HashSet<>();
        limitValue.add(String.valueOf(getLimit()));

        Set<String> offsetValue = new HashSet<>();
        offsetValue.add(String.valueOf(getOffset()));

        request.getQueryParameters().put(RequestFactory.LIMIT_PARAMETER, limitValue);
        request.getQueryParameters().put(RequestFactory.OFFSET_PARAMETER, offsetValue);
        return request;
    }

    public BlackDuckRequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

}
