/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http;

import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckRequestBuilderFactory {
    private final Gson gson;

    public BlackDuckRequestBuilderFactory(Gson gson) {
        this.gson = gson;
    }

    public BlackDuckRequestBuilder createBlackDuckRequestBuilder() {
        return new BlackDuckRequestBuilder(gson, new Request.Builder());
    }

    public BlackDuckRequestBuilder createBlackDuckRequestBuilder(Request request) {
        return new BlackDuckRequestBuilder(gson, new Request.Builder(request));
    }

    public BlackDuckRequestBuilder createCommonGet() {
        return new BlackDuckRequestBuilder(gson, new Request.Builder())
                   .commonGet();
    }

    public BlackDuckRequestBuilder createCommonGet(Optional<BlackDuckQuery> blackDuckQuery) {
        return new BlackDuckRequestBuilder(gson, new Request.Builder())
                   .commonGet()
                   .addBlackDuckQuery(blackDuckQuery);
    }

    public BlackDuckRequestBuilder createCommonGet(BlackDuckRequestFilter blackDuckRequestFilter) {
        return new BlackDuckRequestBuilder(gson, new Request.Builder())
                   .commonGet()
                   .addBlackDuckFilter(blackDuckRequestFilter);
    }

}
