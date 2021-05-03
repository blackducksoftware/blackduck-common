/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http;

import org.apache.commons.lang3.math.NumberUtils;

import com.synopsys.integration.rest.request.Request;

public class PagedRequest {
    private final BlackDuckRequestBuilder blackDuckRequestBuilder;
    private final BlackDuckPageDefinition blackDuckPageDefinition;

    public PagedRequest(BlackDuckRequestBuilder blackDuckRequestBuilder) {
        this.blackDuckRequestBuilder = blackDuckRequestBuilder;
        int offset = BlackDuckRequestBuilder.DEFAULT_OFFSET;
        int limit = BlackDuckRequestBuilder.DEFAULT_LIMIT;
        if (blackDuckRequestBuilder.getQueryParameters() != null) {
            if (blackDuckRequestBuilder.getQueryParameters().containsKey(BlackDuckRequestBuilder.OFFSET_PARAMETER)) {
                offset = NumberUtils.toInt(blackDuckRequestBuilder.getQueryParameters().get(BlackDuckRequestBuilder.OFFSET_PARAMETER).stream().findFirst().orElse(null), offset);
            }
            if (blackDuckRequestBuilder.getQueryParameters().containsKey(BlackDuckRequestBuilder.LIMIT_PARAMETER)) {
                limit = NumberUtils.toInt(blackDuckRequestBuilder.getQueryParameters().get(BlackDuckRequestBuilder.LIMIT_PARAMETER).stream().findFirst().orElse(null), limit);
            }
        }

        this.blackDuckPageDefinition = new BlackDuckPageDefinition(limit, offset);
    }

    public Request createRequest() {
        Request request = blackDuckRequestBuilder
                              .setBlackDuckPageDefinition(blackDuckPageDefinition)
                              .build();
        return request;
    }

    public BlackDuckRequestBuilder getBlackDuckRequestBuilder() {
        return blackDuckRequestBuilder;
    }

    public BlackDuckPageDefinition getBlackDuckPageDefinition() {
        return blackDuckPageDefinition;
    }

}
