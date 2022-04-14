package com.synopsys.integration.blackduck.bdio2;

import com.synopsys.integration.rest.HttpUrl;

public class Bdio2UploadResult {
    private HttpUrl uploadUrl;

    public Bdio2UploadResult(HttpUrl uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public HttpUrl getUploadUrl() {
        return uploadUrl;
    }


}
