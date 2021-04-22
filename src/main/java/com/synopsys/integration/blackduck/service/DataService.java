/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service;

import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.service.request.BlackDuckApiExchangeDescriptorFactory;
import com.synopsys.integration.log.IntLogger;

public class DataService {
    protected final BlackDuckApiClient blackDuckApiClient;
    protected final BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory;
    protected final BlackDuckApiExchangeDescriptorFactory blackDuckApiExchangeDescriptorFactory;
    protected final IntLogger logger;

    public DataService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory, BlackDuckApiExchangeDescriptorFactory blackDuckApiExchangeDescriptorFactory, IntLogger logger) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.blackDuckRequestBuilderFactory = blackDuckRequestBuilderFactory;
        this.blackDuckApiExchangeDescriptorFactory = blackDuckApiExchangeDescriptorFactory;
        this.logger = logger;
    }

}
