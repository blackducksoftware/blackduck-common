/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;

public class BlackDuckApiSpecSingle<T extends BlackDuckResponse> extends BlackDuckApiSpec<T> {
    private final UrlSingleResponse<T> urlSingleResponse;

    public BlackDuckApiSpecSingle(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlSingleResponse<T> urlSingleResponse) {
        super(blackDuckRequestBuilder, urlSingleResponse);
        this.urlSingleResponse = urlSingleResponse;
    }

    public UrlSingleResponse<T> getUrlSingleResponse() {
        return urlSingleResponse;
    }

}
