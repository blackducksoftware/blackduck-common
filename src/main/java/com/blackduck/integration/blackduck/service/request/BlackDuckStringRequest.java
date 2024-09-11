/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.request;

import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.manual.response.BlackDuckStringResponse;

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
