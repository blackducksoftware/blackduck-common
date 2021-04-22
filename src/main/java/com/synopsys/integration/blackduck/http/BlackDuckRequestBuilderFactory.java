/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.StringBodyContent;

public class BlackDuckRequestBuilderFactory {
    private final Gson gson;
    private final HttpUrl blackDuckUrl;

    public BlackDuckRequestBuilderFactory(Gson gson, HttpUrl blackDuckUrl) {
        this.gson = gson;
        this.blackDuckUrl = blackDuckUrl;
    }

    public BlackDuckRequestBuilder createBlackDuckRequestBuilder() {
        return new BlackDuckRequestBuilder();
    }

    public BlackDuckRequestBuilder createBlackDuckRequestBuilder(BlackDuckPath blackDuckPath) throws IntegrationException {
        return new BlackDuckRequestBuilder(blackDuckPath.getFullBlackDuckUrl(blackDuckUrl));
    }

    public BlackDuckRequestBuilder createBlackDuckRequestBuilder(HttpUrl url) {
        return new BlackDuckRequestBuilder(url);
    }

    public void populateUrl(BlackDuckRequestBuilder blackDuckRequestBuilder, BlackDuckPath blackDuckPath) throws IntegrationException {
        blackDuckRequestBuilder.url(blackDuckPath.getFullBlackDuckUrl(blackDuckUrl));
    }

    public void populateBodyContent(BlackDuckRequestBuilder blackDuckRequestBuilder, Object object) throws IntegrationException {
        blackDuckRequestBuilder.bodyContent(new StringBodyContent(gson.toJson(object)));
    }

}
