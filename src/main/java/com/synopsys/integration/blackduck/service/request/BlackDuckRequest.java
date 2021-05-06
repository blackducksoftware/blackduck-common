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

public class BlackDuckRequest<T extends BlackDuckResponse> {
    private final BlackDuckRequestBuilder blackDuckRequestBuilder;
    private final Class<T> responseClass;

    public BlackDuckRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, Class<T> responseClass) {
        this.blackDuckRequestBuilder = blackDuckRequestBuilder;
        this.responseClass = responseClass;
    }

    public BlackDuckRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlResponse<T> urlResponse) {
        this.blackDuckRequestBuilder = blackDuckRequestBuilder
                                           .url(urlResponse.getUrl());
        this.responseClass = urlResponse.getResponseClass();
    }

    public Request getRequest() {
        return blackDuckRequestBuilder.build();
    }

    public Request getRequest(BlackDuckRequestBuilderEditor blackDuckRequestBuilderEditor) {
        blackDuckRequestBuilderEditor.edit(blackDuckRequestBuilder);
        return getRequest();
    }

    public Class<T> getResponseClass() {
        return responseClass;
    }

}
