/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import com.synopsys.integration.rest.body.MultipartBodyContent;
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
        return RequestFactory.createCommonGetRequestBuilder(null, Optional.empty(), RequestFactory.DEFAULT_LIMIT, RequestFactory.DEFAULT_OFFSET);
    }

    public static Request.Builder createCommonGetRequestBuilder(String uri) {
        return RequestFactory.createCommonGetRequestBuilder(uri, Optional.empty(), RequestFactory.DEFAULT_LIMIT, RequestFactory.DEFAULT_OFFSET);
    }

    public static Request.Builder createCommonGetRequestBuilder(Optional<BlackDuckQuery> blackDuckQuery) {
        return RequestFactory.createCommonGetRequestBuilder(null, blackDuckQuery, RequestFactory.DEFAULT_LIMIT, RequestFactory.DEFAULT_OFFSET);
    }

    public static Request.Builder createCommonGetRequestBuilder(int limit, int offset) {
        return RequestFactory.createCommonGetRequestBuilder(null, Optional.empty(), limit, offset);
    }

    public static Request.Builder createCommonGetRequestBuilder(String uri, Optional<BlackDuckQuery> blackDuckQuery) {
        return RequestFactory.createCommonGetRequestBuilder(uri, blackDuckQuery, RequestFactory.DEFAULT_LIMIT, RequestFactory.DEFAULT_OFFSET);
    }

    public static Request.Builder createCommonGetRequestBuilder(String uri, int limit, int offset) {
        return RequestFactory.createCommonGetRequestBuilder(uri, Optional.empty(), limit, offset);
    }

    public static Request.Builder createCommonGetRequestBuilder(Optional<BlackDuckQuery> blackDuckQuery, int limit, int offset) {
        return RequestFactory.createCommonGetRequestBuilder(null, blackDuckQuery, limit, offset);
    }

    public static Request.Builder createCommonGetRequestBuilder(String uri, Optional<BlackDuckQuery> blackDuckQuery, int limit, int offset) {
        return RequestFactory.createCommonGetRequestBuilder(uri, blackDuckQuery, null, limit, offset);
    }

    public static Request.Builder createCommonGetRequestBuilder(String uri, Optional<BlackDuckQuery> blackDuckQuery, BlackDuckRequestFilter blackDuckRequestFilter, int limit, int offset) {
        Request.Builder requestBuilder = new Request.Builder();
        if (StringUtils.isNotBlank(uri)) {
            requestBuilder.uri(uri);
        }
        RequestFactory.addBlackDuckQuery(requestBuilder, blackDuckQuery);
        RequestFactory.addBlackDuckFilter(requestBuilder, blackDuckRequestFilter);
        RequestFactory.addLimit(requestBuilder, limit);
        RequestFactory.addOffset(requestBuilder, offset);
        return requestBuilder;
    }

    public static Request createCommonGetRequest(String uri) {
        return RequestFactory.createCommonGetRequestBuilder(uri).build();
    }

    public static Request.Builder addLimit(Request.Builder requestBuilder, int limit) {
        requestBuilder.addQueryParameter(RequestFactory.LIMIT_PARAMETER, String.valueOf(limit));
        return requestBuilder;
    }

    public static Request.Builder addOffset(Request.Builder requestBuilder, int offset) {
        requestBuilder.addQueryParameter(RequestFactory.OFFSET_PARAMETER, String.valueOf(offset));
        return requestBuilder;
    }

    public static Request.Builder addBlackDuckQuery(Request.Builder requestBuilder, Optional<BlackDuckQuery> blackDuckQuery) {
        if (blackDuckQuery.isPresent()) {
            requestBuilder.addQueryParameter(RequestFactory.Q_PARAMETER, blackDuckQuery.get().getParameter());
        }
        return requestBuilder;
    }

    public static Request.Builder addBlackDuckFilter(Request.Builder requestBuilder, BlackDuckRequestFilter blackDuckRequestFilter) {
        if (blackDuckRequestFilter != null) {
            blackDuckRequestFilter.getFilterParameters().forEach(parameter -> {
                requestBuilder.addQueryParameter(RequestFactory.FILTER_PARAMETER, parameter);
            });
        }
        return requestBuilder;
    }

    public static Request.Builder createCommonPostRequestBuilder(File bodyContentFile) {
        return new Request.Builder().method(HttpMethod.POST).bodyContent(new FileBodyContent(bodyContentFile));
    }

    public static Request.Builder createCommonPostRequestBuilder(Map<String, String> bodyContentMap) {
        return new Request.Builder().method(HttpMethod.POST).bodyContent(new MapBodyContent(bodyContentMap));
    }

    public static Request.Builder createCommonPostRequestBuilder(String bodyContent) {
        return new Request.Builder().method(HttpMethod.POST).bodyContent(new StringBodyContent(bodyContent));
    }

    public static Request.Builder createCommonPutRequestBuilder(String bodyContent) {
        return new Request.Builder().method(HttpMethod.PUT).bodyContent(new StringBodyContent(bodyContent));
    }

    public static Request.Builder createCommonPostRequestBuilder(Map<String, File> bodyContentFileMap, Map<String, String> bodyContentStringMap) {
        return new Request.Builder().method(HttpMethod.POST).bodyContent(new MultipartBodyContent(bodyContentFileMap, bodyContentStringMap));
    }

}
