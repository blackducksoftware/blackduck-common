package com.blackducksoftware.integration.hub.api;

import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.core.LinkResponse;

public class UriSingleResponse<T extends HubResponse> extends LinkResponse {
    public String uri;
    public Class<T> responseClass;

    public UriSingleResponse(final String uri, final Class<T> responseClass) {
        this.uri = uri;
        this.responseClass = responseClass;
    }

    @Override
    public String toString() {
        return uri;
    }

}
