package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckUrlFactory {
    private final HttpUrl blackDuckBaseUrl;

    public BlackDuckUrlFactory(HttpUrl blackDuckBaseUrl) {
        this.blackDuckBaseUrl = blackDuckBaseUrl;
    }

    public HttpUrl fromBlackDuckPath(BlackDuckPath blackDuckPath) throws IntegrationException {
        return blackDuckPath.getFullBlackDuckUrl(blackDuckBaseUrl);
    }

}
