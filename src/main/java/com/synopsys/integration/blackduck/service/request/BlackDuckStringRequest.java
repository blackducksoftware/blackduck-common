/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.manual.response.BlackDuckStringResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;

public class BlackDuckStringRequest extends BlackDuckRequest<BlackDuckStringResponse, UrlSingleResponse<BlackDuckStringResponse>> {
    public BlackDuckStringRequest(BlackDuckRequestBuilder blackDuckRequestBuilder,
        UrlSingleResponse<BlackDuckStringResponse> urlResponse) {
        super(blackDuckRequestBuilder, urlResponse);
    }

    public BlackDuckStringRequest(BlackDuckRequestBuilder blackDuckRequestBuilder,
        UrlSingleResponse<BlackDuckStringResponse> urlResponse, PagingDefaultsEditor pagingDefaultsEditor, AcceptHeaderEditor acceptHeaderEditor) {
        super(blackDuckRequestBuilder, urlResponse, pagingDefaultsEditor, acceptHeaderEditor);
    }

}
