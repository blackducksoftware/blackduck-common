package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.rest.HttpUrl;

public class UrlResponse<T extends BlackDuckResponse> {
    private final HttpUrl url;
    private final Class<T> responseClass;

    public UrlResponse(HttpUrl url, Class<T> responseClass) {
        this.url = url;
        this.responseClass = responseClass;
    }

    public HttpUrl getUrl() {
        return url;
    }

    public Class<T> getResponseClass() {
        return responseClass;
    }

}
