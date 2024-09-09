/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.request;

import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.manual.response.BlackDuckResponseResponse;

public class BlackDuckResponseRequest extends BlackDuckRequest<BlackDuckResponseResponse, UrlSingleResponse<BlackDuckResponseResponse>> {
    public BlackDuckResponseRequest(BlackDuckRequestBuilder blackDuckRequestBuilder,
                                    UrlSingleResponse<BlackDuckResponseResponse> urlResponse) {
        super(blackDuckRequestBuilder, urlResponse);
    }

    public BlackDuckResponseRequest(BlackDuckRequestBuilder blackDuckRequestBuilder,
        UrlSingleResponse<BlackDuckResponseResponse> urlResponse, PagingDefaultsEditor pagingDefaultsEditor, AcceptHeaderEditor acceptHeaderEditor) {
        super(blackDuckRequestBuilder, urlResponse, pagingDefaultsEditor, acceptHeaderEditor);
    }

}
