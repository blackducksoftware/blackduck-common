/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.request;

import com.blackduck.integration.blackduck.api.core.BlackDuckResponse;
import com.blackduck.integration.blackduck.api.core.response.UrlSingleResponse;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;

public class BlackDuckSingleRequest<T extends BlackDuckResponse> extends BlackDuckRequest<T, UrlSingleResponse<T>> {
    public BlackDuckSingleRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlSingleResponse<T> urlResponse) {
        super(blackDuckRequestBuilder, urlResponse);
    }

    public BlackDuckSingleRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlSingleResponse<T> urlResponse, PagingDefaultsEditor pagingDefaultsEditor,
        AcceptHeaderEditor acceptHeaderEditor) {
        super(blackDuckRequestBuilder, urlResponse, pagingDefaultsEditor, acceptHeaderEditor);
    }

}
