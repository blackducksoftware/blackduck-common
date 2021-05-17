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

/**
 * The total picture of a Black Duck interaction. The single/multiple subclasses specify how the response should be handled.
 */
public class BlackDuckApiSpec<T extends BlackDuckResponse> {
    private final BlackDuckRequest<T> blackDuckRequest;

    public BlackDuckApiSpec(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlResponse<T> urlResponse) {
        this(new BlackDuckRequest<T>(blackDuckRequestBuilder, urlResponse));
    }

    public BlackDuckApiSpec(BlackDuckRequest<T> blackDuckRequest) {
        this.blackDuckRequest = blackDuckRequest;
    }

    public BlackDuckRequest<T> getBlackDuckRequest() {
        return blackDuckRequest;
    }

}
