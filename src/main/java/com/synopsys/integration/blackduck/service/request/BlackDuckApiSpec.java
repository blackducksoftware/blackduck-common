/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckApiSpec<T extends BlackDuckResponse> {
    private final UrlResponse<T> urlResponse;
    private final BlackDuckRequestBuilder blackDuckRequestBuilder;

    public BlackDuckApiSpec(UrlResponse<T> urlResponse, BlackDuckRequestBuilder blackDuckRequestBuilder) {
        this.urlResponse = urlResponse;
        this.blackDuckRequestBuilder = blackDuckRequestBuilder;
    }

    public UrlResponse<T> getUrlResponse() {
        return urlResponse;
    }

    public BlackDuckRequestBuilder getBlackDuckRequestBuilder() {
        return blackDuckRequestBuilder;
    }

}
