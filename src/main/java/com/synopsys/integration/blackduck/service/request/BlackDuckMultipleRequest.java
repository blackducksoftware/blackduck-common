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
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;

public class BlackDuckMultipleRequest<T extends BlackDuckResponse> extends BlackDuckRequest<T, UrlMultipleResponses<T>> {
    public BlackDuckMultipleRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlMultipleResponses<T> urlResponse) {
        super(blackDuckRequestBuilder, urlResponse);
    }

    public BlackDuckMultipleRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlMultipleResponses<T> urlResponse, PagingDefaultsEditor pagingDefaultsEditor,
        AcceptHeaderEditor acceptHeaderEditor) {
        super(blackDuckRequestBuilder, urlResponse, pagingDefaultsEditor, acceptHeaderEditor);
    }

}
