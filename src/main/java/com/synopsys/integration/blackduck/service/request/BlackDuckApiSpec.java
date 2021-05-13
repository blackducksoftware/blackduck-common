/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;

public class BlackDuckApiSpec<T extends BlackDuckResponse> {
    private final BlackDuckRequest<T> blackDuckRequest;

    public BlackDuckApiSpec(BlackDuckRequest<T> blackDuckRequest) {
        this.blackDuckRequest = blackDuckRequest;
    }

    public BlackDuckRequest<T> getBlackDuckRequest() {
        return blackDuckRequest;
    }

}
