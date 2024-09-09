/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.request;

import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;

public class BlackDuckSingleRequest<T extends BlackDuckResponse> extends BlackDuckRequest<T, UrlSingleResponse<T>> {
    public BlackDuckSingleRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlSingleResponse<T> urlResponse) {
        super(blackDuckRequestBuilder, urlResponse);
    }

    public BlackDuckSingleRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlSingleResponse<T> urlResponse, PagingDefaultsEditor pagingDefaultsEditor,
        AcceptHeaderEditor acceptHeaderEditor) {
        super(blackDuckRequestBuilder, urlResponse, pagingDefaultsEditor, acceptHeaderEditor);
    }

}
