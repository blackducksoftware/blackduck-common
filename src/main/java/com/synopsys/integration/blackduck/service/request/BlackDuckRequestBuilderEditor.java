/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;

/**
 * This will allow for encapsulating multiple related changes to the
 * BlackDuckRequestBuilder.
 */
public interface BlackDuckRequestBuilderEditor {
    void edit(BlackDuckRequestBuilder blackDuckRequestBuilder);

}
