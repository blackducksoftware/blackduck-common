/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.generated.discovery.BlackDuckMediaTypeDiscovery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.rest.HttpUrl;

/**
 * When requesting resources from Black Duck, a default ACCEPT header of
 * 'application/json' is needed, but often, a more appropriate header can be
 * determined.
 */
public class AcceptHeaderEditor implements BlackDuckRequestBuilderEditor {
    private final BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery;

    public AcceptHeaderEditor(BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery) {
        this.blackDuckMediaTypeDiscovery = blackDuckMediaTypeDiscovery;
    }

    @Override
    public void edit(BlackDuckRequestBuilder blackDuckRequestBuilder) {
        HttpUrl httpUrl = blackDuckRequestBuilder.getUrl();
        String mediaType = blackDuckRequestBuilder.getAcceptMimeType();

        String replacementMediaType = blackDuckMediaTypeDiscovery.determineMediaType(httpUrl, mediaType);
        blackDuckRequestBuilder.acceptMimeType(replacementMediaType);
    }

}
