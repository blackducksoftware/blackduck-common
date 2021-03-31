/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;

public class BlackDuckApiRequestSpec<T extends BlackDuckPathResponse<?>> {
    private final T pathResponse;
    private final BlackDuckRequestBuilder requestBuilder;

    public BlackDuckApiRequestSpec(T pathResponse, BlackDuckRequestBuilder requestBuilder) {
        this.pathResponse = pathResponse;
        this.requestBuilder = requestBuilder;
    }

    public T getPathResponse() {
        return pathResponse;
    }

    public BlackDuckRequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

}
