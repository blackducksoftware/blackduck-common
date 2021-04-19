/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service;

import com.synopsys.integration.log.IntLogger;

public class DataService {
    protected final BlackDuckApiClient blackDuckApiClient;
    protected final BlackDuckApiFactories blackDuckApiFactories;
    protected final IntLogger logger;

    public DataService(BlackDuckApiClient blackDuckApiClient, BlackDuckApiFactories blackDuckApiFactories, IntLogger logger) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.blackDuckApiFactories = blackDuckApiFactories;
        this.logger = logger;
    }

}
