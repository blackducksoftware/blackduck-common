/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.bdio2;

import com.blackduck.integration.rest.HttpUrl;

public class Bdio2UploadResult {
    private final HttpUrl uploadUrl;
    private final String scanId;

    public Bdio2UploadResult(HttpUrl uploadUrl, final String scanId) {
        this.uploadUrl = uploadUrl;
        this.scanId = scanId;
    }

    public HttpUrl getUploadUrl() {
        return uploadUrl;
    }

    public String getScanId() {
        return scanId;
    }
}
