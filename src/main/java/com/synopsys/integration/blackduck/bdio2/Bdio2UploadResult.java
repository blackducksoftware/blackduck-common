/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2;

import com.synopsys.integration.rest.HttpUrl;

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
