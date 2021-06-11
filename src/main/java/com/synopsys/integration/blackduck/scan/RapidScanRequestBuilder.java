package com.synopsys.integration.blackduck.scan;

import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.synopsys.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.synopsys.integration.rest.HttpUrl;

public class RapidScanRequestBuilder {
    private final BlackDuckRequestBuilder blackDuckRequestBuilder;

    public RapidScanRequestBuilder() {
        this.blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                           .commonGet()
                                           .acceptMimeType(DeveloperScanComponentResultView.CURRENT_MEDIA_TYPE);
    }

    public BlackDuckResponseRequest createResponseRequest(HttpUrl httpUrl) {
        return blackDuckRequestBuilder.buildBlackDuckResponseRequest(httpUrl);
    }

    public BlackDuckMultipleRequest<DeveloperScanComponentResultView> createRequest(HttpUrl httpUrl) {
        return blackDuckRequestBuilder.buildBlackDuckRequest(new UrlMultipleResponses<>(httpUrl, DeveloperScanComponentResultView.class));
    }

}
