/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
        int offset = BlackDuckRequestBuilder.DEFAULT_OFFSET;
        int limit = BlackDuckRequestBuilder.DEFAULT_LIMIT;
        if (requestBuilder.getQueryParameters() != null) {
            if (requestBuilder.getQueryParameters().containsKey(BlackDuckRequestBuilder.OFFSET_PARAMETER)) {
                offset = NumberUtils.toInt(requestBuilder.getQueryParameters().get(BlackDuckRequestBuilder.OFFSET_PARAMETER).stream().findFirst().orElse(null), offset);
            }
            if (requestBuilder.getQueryParameters().containsKey(BlackDuckRequestBuilder.LIMIT_PARAMETER)) {
                limit = NumberUtils.toInt(requestBuilder.getQueryParameters().get(BlackDuckRequestBuilder.LIMIT_PARAMETER).stream().findFirst().orElse(null), limit);
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

    public PagedRequest(BlackDuckRequestBuilder requestBuilder, BlackDuckPageDefinition blackDuckPageDefinition) {
        this.requestBuilder = requestBuilder;
        this.offset = blackDuckPageDefinition.getOffset();
        this.limit = blackDuckPageDefinition.getLimit();
    }

    public Request createRequest() {
        Request request = requestBuilder.getRequestBuilder().build();
        Set<String> limitValue = new HashSet<>();
        limitValue.add(String.valueOf(getLimit()));

        Set<String> offsetValue = new HashSet<>();
        offsetValue.add(String.valueOf(getOffset()));

        request.getQueryParameters().put(BlackDuckRequestBuilder.LIMIT_PARAMETER, limitValue);
        request.getQueryParameters().put(BlackDuckRequestBuilder.OFFSET_PARAMETER, offsetValue);
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
