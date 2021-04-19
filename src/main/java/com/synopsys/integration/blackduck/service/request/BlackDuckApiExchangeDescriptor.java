/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;

public class BlackDuckApiExchangeDescriptor<T extends BlackDuckResponse> {
    private final BlackDuckRequestBuilder blackDuckRequestBuilder;
    private final Class<T> responseClass;

    public BlackDuckApiExchangeDescriptor(BlackDuckRequestBuilder blackDuckRequestBuilder, Class<T> responseClass) {
        this.blackDuckRequestBuilder = blackDuckRequestBuilder;
        this.responseClass = responseClass;
    }

    public BlackDuckRequestBuilder getBlackDuckRequestBuilder() {
        return blackDuckRequestBuilder;
    }

    public Class<T> getResponseClass() {
        return responseClass;
    }

}
