/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.request;

import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;

public class BlackDuckMultipleRequest<T extends BlackDuckResponse> extends BlackDuckRequest<T, UrlMultipleResponses<T>> {
    public BlackDuckMultipleRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlMultipleResponses<T> urlResponse) {
        super(blackDuckRequestBuilder, urlResponse);
    }

    public BlackDuckMultipleRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlMultipleResponses<T> urlResponse, PagingDefaultsEditor pagingDefaultsEditor,
        AcceptHeaderEditor acceptHeaderEditor) {
        super(blackDuckRequestBuilder, urlResponse, pagingDefaultsEditor, acceptHeaderEditor);
    }

}
