/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ScanReadinessView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

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
