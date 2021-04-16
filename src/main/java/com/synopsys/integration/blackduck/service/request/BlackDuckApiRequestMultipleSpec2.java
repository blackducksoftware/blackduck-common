/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckApiRequestMultipleSpec2<T extends BlackDuckResponse> extends BlackDuckApiRequestSpec2<T> {
    public BlackDuckApiRequestMultipleSpec2(Request request, Class<T> responseClass) {
        super(request, responseClass);
    }

}
