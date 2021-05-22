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

public class BlackDuckSingleRequest<T extends BlackDuckResponse> extends BlackDuckRequest<T, UrlSingleResponse<T>> {
    public BlackDuckSingleRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlSingleResponse<T> urlResponse) {
        super(blackDuckRequestBuilder, urlResponse);
    }

}
