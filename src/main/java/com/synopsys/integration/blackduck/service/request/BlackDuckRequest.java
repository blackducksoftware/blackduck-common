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
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;

/**
 * Intended to be an immutable, valid packaging of an HTTP request to Black Duck and how to consider the response.
 */
public class BlackDuckRequest<T extends BlackDuckResponse> {
    private final BlackDuckRequestBuilder blackDuckRequestBuilder;
    private final UrlResponse<T> urlResponse;

    public BlackDuckRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, HttpUrl url, Class<T> responseClass) {
        this.blackDuckRequestBuilder = blackDuckRequestBuilder
                                           .url(url);
        this.urlResponse = new UrlResponse<>(url, responseClass);
    }

    public BlackDuckRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, UrlResponse<T> urlResponse) {
        this.blackDuckRequestBuilder = blackDuckRequestBuilder
                                           .url(urlResponse.getUrl());
        this.urlResponse = urlResponse;
    }

    public Request getRequest() {
        return blackDuckRequestBuilder.build();
    }

    public UrlResponse<T> getUrlResponse() {
        return urlResponse;
    }

    public HttpUrl getUrl() {
        return urlResponse.getUrl();
    }

    public Class<T> getResponseClass() {
        return urlResponse.getResponseClass();
    }

}
