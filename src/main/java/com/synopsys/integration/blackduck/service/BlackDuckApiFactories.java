package com.synopsys.integration.blackduck.service;

import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.request.BlackDuckApiExchangeDescriptorFactory;
import com.synopsys.integration.blackduck.service.request.BlackDuckUrlFactory;

public class BlackDuckApiFactories {
    public final BlackDuckUrlFactory blackDuckUrlFactory;
    public final BlackDuckRequestFactory blackDuckRequestFactory;
    public final BlackDuckApiExchangeDescriptorFactory blackDuckApiExchangeDescriptorFactory;

    public BlackDuckApiFactories(BlackDuckUrlFactory blackDuckUrlFactory, BlackDuckRequestFactory blackDuckRequestFactory,
        BlackDuckApiExchangeDescriptorFactory blackDuckApiExchangeDescriptorFactory) {
        this.blackDuckUrlFactory = blackDuckUrlFactory;
        this.blackDuckRequestFactory = blackDuckRequestFactory;
        this.blackDuckApiExchangeDescriptorFactory = blackDuckApiExchangeDescriptorFactory;
    }

}
