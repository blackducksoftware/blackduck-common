/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.request;

import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;

/**
 * This will allow for encapsulating multiple related changes to the
 * BlackDuckRequestBuilder.
 */
public interface BlackDuckRequestBuilderEditor {
    void edit(BlackDuckRequestBuilder blackDuckRequestBuilder);

}
