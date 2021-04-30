/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;

public class BlackDuckApiSpecMultiple<T extends BlackDuckResponse> extends BlackDuckApiSpec<T> {
    private final UrlMultipleResponses<T> urlMultipleResponses;

    public BlackDuckApiSpecMultiple(UrlMultipleResponses<T> urlMultipleResponses, BlackDuckRequestBuilder blackDuckRequestBuilder) {
        super(urlMultipleResponses, blackDuckRequestBuilder);
        this.urlMultipleResponses = urlMultipleResponses;
    }

    public UrlMultipleResponses<T> getUrlMultipleResponses() {
        return urlMultipleResponses;
    }

}
