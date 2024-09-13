/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.ScanReadinessView;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;

public class BlackDuckScanReadinessService extends DataService {
    public BlackDuckScanReadinessService(
        final BlackDuckApiClient blackDuckApiClient,
        final ApiDiscovery apiDiscovery,
        final IntLogger logger
    ) {
        super(blackDuckApiClient, apiDiscovery, logger);
    }

    public ScanReadinessView getScanReadiness() throws IntegrationException {
        return blackDuckApiClient.getResponse(apiDiscovery.metaScanReadinessLink());
    }
}
