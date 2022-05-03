/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.rest.HttpUrl;

public class Bdio2UploadResult {
    private final HttpUrl uploadUrl;

    public Bdio2UploadResult(HttpUrl uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public HttpUrl getUploadUrl() {
        return uploadUrl;
    }


}
