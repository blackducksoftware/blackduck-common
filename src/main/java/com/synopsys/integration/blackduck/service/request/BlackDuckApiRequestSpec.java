/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;

public class BlackDuckApiRequestSpec<T extends BlackDuckResponse> {
    private final BlackDuckPath path;
    private final Class<T> responseClass;
    private final BlackDuckRequestBuilder requestBuilder;

    public BlackDuckApiRequestSpec(BlackDuckPath path, Class<T> responseClass, BlackDuckRequestBuilder requestBuilder) {
        this.path = path;
        this.responseClass = responseClass;
        this.requestBuilder = requestBuilder;
    }

    public BlackDuckPath getPath() {
        return path;
    }

    public Class<T> getResponseClass() {
        return responseClass;
    }

    public BlackDuckRequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

}
