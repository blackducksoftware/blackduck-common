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

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.body.FileBodyContent;
import com.synopsys.integration.rest.body.MapBodyContent;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.request.Request;

public class RequestFactory {
    public static final String LIMIT_PARAMETER = "limit";
    public static final String OFFSET_PARAMETER = "offset";
    public static final String Q_PARAMETER = "q";
    public static final String FILTER_PARAMETER = "filter";

    public static final int DEFAULT_LIMIT = 100;
    public static final int DEFAULT_OFFSET = 0;

    public static Request.Builder createCommonGetRequestBuilder() {
        return createCommonGetRequestBuilder(null, Optional.empty(), DEFAULT_LIMIT, DEFAULT_OFFSET);
    }

    public static Request.Builder createCommonGetRequestBuilder(final String uri) {
        return createCommonGetRequestBuilder(uri, Optional.empty(), DEFAULT_LIMIT, DEFAULT_OFFSET);
    }

    public static Request.Builder createCommonGetRequestBuilder(final Optional<HubQuery> hubQuery) {
        return createCommonGetRequestBuilder(null, hubQuery, DEFAULT_LIMIT, DEFAULT_OFFSET);
    }

    public static Request.Builder createCommonGetRequestBuilder(final int limit, final int offset) {
        return createCommonGetRequestBuilder(null, Optional.empty(), limit, offset);
    }

    public static Request.Builder createCommonGetRequestBuilder(final String uri, final Optional<HubQuery> hubQuery) {
        return createCommonGetRequestBuilder(uri, hubQuery, DEFAULT_LIMIT, DEFAULT_OFFSET);
    }

    public static Request.Builder createCommonGetRequestBuilder(final String uri, final int limit, final int offset) {
        return createCommonGetRequestBuilder(uri, Optional.empty(), limit, offset);
    }

    public static Request.Builder createCommonGetRequestBuilder(final Optional<HubQuery> hubQuery, final int limit, final int offset) {
        return createCommonGetRequestBuilder(null, hubQuery, limit, offset);
    }

    public static Request.Builder createCommonGetRequestBuilder(final String uri, final Optional<HubQuery> hubQuery, final int limit, final int offset) {
        return createCommonGetRequestBuilder(uri, hubQuery, null, limit, offset);
    }

    public static Request.Builder createCommonGetRequestBuilder(final String uri, final Optional<HubQuery> hubQuery, final HubFilter hubFilter, final int limit, final int offset) {
        final Request.Builder requestBuilder = new Request.Builder();
        if (StringUtils.isNotBlank(uri)) {
            requestBuilder.uri(uri);
        }
        addHubQuery(requestBuilder, hubQuery);
        addHubFilter(requestBuilder, hubFilter);
        addLimit(requestBuilder, limit);
        addOffset(requestBuilder, offset);
        return requestBuilder;
    }

    public static Request createCommonGetRequest(final String uri) {
        return createCommonGetRequestBuilder(uri).build();
    }

    public static Request.Builder addLimit(final Request.Builder requestBuilder, final int limit) {
        requestBuilder.addQueryParameter(LIMIT_PARAMETER, String.valueOf(limit));
        return requestBuilder;
    }

    public static Request.Builder addOffset(final Request.Builder requestBuilder, final int offset) {
        requestBuilder.addQueryParameter(OFFSET_PARAMETER, String.valueOf(offset));
        return requestBuilder;
    }

    public static Request.Builder addHubQuery(final Request.Builder requestBuilder, final Optional<HubQuery> hubQuery) {
        if (hubQuery.isPresent()) {
            requestBuilder.addQueryParameter(Q_PARAMETER, hubQuery.get().getParameter());
        }
        return requestBuilder;
    }

    public static Request.Builder addHubFilter(final Request.Builder requestBuilder, final HubFilter hubFilter) {
        if (hubFilter != null) {
            hubFilter.getFilterParameters().forEach(parameter -> {
                requestBuilder.addQueryParameter(FILTER_PARAMETER, parameter);
            });
        }
        return requestBuilder;
    }

    public static Request.Builder createCommonPostRequestBuilder(final File bodyContentFile) {
        return new Request.Builder().method(HttpMethod.POST).bodyContent(new FileBodyContent(bodyContentFile));
    }

    public static Request.Builder createCommonPostRequestBuilder(final Map<String, String> bodyContentMap) {
        return new Request.Builder().method(HttpMethod.POST).bodyContent(new MapBodyContent(bodyContentMap));
    }

    public static Request.Builder createCommonPostRequestBuilder(final String bodyContent) {
        return new Request.Builder().method(HttpMethod.POST).bodyContent(new StringBodyContent(bodyContent));
    }

    public static Request.Builder createCommonPutRequestBuilder(final String bodyContent) {
        return new Request.Builder().method(HttpMethod.PUT).bodyContent(new StringBodyContent(bodyContent));
    }

}
