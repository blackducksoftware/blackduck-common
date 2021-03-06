/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import java.util.List;

import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.exception.IntegrationTimeoutException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobConfig;

public class RapidScanWaiter {
    private final IntLogger logger;
    private final BlackDuckApiClient blackDuckApiClient;

    public RapidScanWaiter(IntLogger logger, BlackDuckApiClient blackDuckApiClient) {
        this.logger = logger;
        this.blackDuckApiClient = blackDuckApiClient;
    }

    public List<DeveloperScanComponentResultView> checkScanResult(HttpUrl url, String codeLocationName, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        String taskName = "rapid scan for " + codeLocationName;
        WaitJobConfig waitJobConfig = new WaitJobConfig(logger, taskName, timeoutInSeconds, System.currentTimeMillis(), waitIntervalInSeconds);
        RapidScanWaitJobCondition waitJobCondition = new RapidScanWaitJobCondition(blackDuckApiClient, url);
        RapidScanWaitJobCompleter waitJobCompleter = new RapidScanWaitJobCompleter(blackDuckApiClient, url);

        WaitJob<List<DeveloperScanComponentResultView>> waitJob = new WaitJob<>(waitJobConfig, waitJobCondition, waitJobCompleter);

        try {
            return waitJob.waitFor();
        } catch (IntegrationTimeoutException e) {
            throw new BlackDuckIntegrationException(e.getMessage());
        }
    }

}
