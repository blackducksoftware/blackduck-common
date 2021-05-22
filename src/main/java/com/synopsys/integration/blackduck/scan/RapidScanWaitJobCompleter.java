/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import java.util.List;

import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.exception.IntegrationTimeoutException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.wait.WaitJobCompleter;

public class RapidScanWaitJobCompleter implements WaitJobCompleter<List<DeveloperScanComponentResultView>> {
    private final BlackDuckApiClient blackDuckApiClient;
    private final HttpUrl resultUrl;

    public RapidScanWaitJobCompleter(BlackDuckApiClient blackDuckApiClient, HttpUrl resultUrl) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.resultUrl = resultUrl;
    }

    @Override
    public List<DeveloperScanComponentResultView> complete() throws IntegrationException {
        UrlMultipleResponses<DeveloperScanComponentResultView> resultResponses = new UrlMultipleResponses<>(resultUrl, DeveloperScanComponentResultView.class);
        return blackDuckApiClient.getAllResponses(resultResponses);
    }

    @Override
    public List<DeveloperScanComponentResultView> handleTimeout() throws IntegrationTimeoutException {
        throw new IntegrationTimeoutException("Error getting developer scan result. Timeout may have occurred.");
    }

}
