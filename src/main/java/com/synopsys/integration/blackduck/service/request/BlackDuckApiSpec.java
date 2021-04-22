package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckApiSpec<T extends BlackDuckResponse> {
    private final Request request;
    private final Class<T> responseClass;

    public BlackDuckApiSpec(UrlResponse<T> urlResponse, BlackDuckRequestBuilder blackDuckRequestBuilder) {
        request = blackDuckRequestBuilder.build(urlResponse.getUrl());
        responseClass = urlResponse.getResponseClass();
    }

    public Request getRequest() {
        return request;
    }

    public Class<T> getResponseClass() {
        return responseClass;
    }

}
