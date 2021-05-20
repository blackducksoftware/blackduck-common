/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import static com.synopsys.integration.wait.WaitJobInitializer.NO_INITIALIZATION;

import java.util.List;

import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.exception.IntegrationTimeoutException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobCompleter;
import com.synopsys.integration.wait.WaitJobConfig;
import com.synopsys.integration.wait.WaitJobTask;
import com.synopsys.integration.wait.WaitJobTimeoutHandler;

public class RapidScanWaiter {
    private IntLogger logger;
    private BlackDuckApiClient blackDuckApiClient;

    public RapidScanWaiter(final IntLogger logger, final BlackDuckApiClient blackDuckApiClient) {
        this.logger = logger;
        this.blackDuckApiClient = blackDuckApiClient;
    }

    public List<DeveloperScanComponentResultView> checkScanResult(HttpUrl url, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        RapidScanWaitJobChecker waitJobCheck = new RapidScanWaitJobChecker(blackDuckApiClient, url);

        WaitJobCompleter<List<DeveloperScanComponentResultView>> taskCompleter = () -> {
            UrlMultipleResponses<DeveloperScanComponentResultView> resultResponses = new UrlMultipleResponses<>(url, DeveloperScanComponentResultView.class);
            return blackDuckApiClient.getAllResponses(resultResponses);
        };

        WaitJobTimeoutHandler<List<DeveloperScanComponentResultView>> taskTimeoutHandler = () -> {
            throw new IntegrationTimeoutException("Error getting developer scan result. Timeout may have occurred.");
        };

        WaitJobConfig waitJobConfig = new WaitJobConfig(logger, timeoutInSeconds, System.currentTimeMillis(), waitIntervalInSeconds);
        WaitJobTask<List<DeveloperScanComponentResultView>> waitJobTask = new WaitJobTask<>("", NO_INITIALIZATION, waitJobCheck, taskCompleter, taskTimeoutHandler);
        WaitJob<List<DeveloperScanComponentResultView>> waitJob = new WaitJob<>(waitJobConfig, waitJobTask);

        try {
            return waitJob.waitFor();
        } catch (IntegrationTimeoutException e) {
            throw new BlackDuckIntegrationException(e.getMessage());
        }
    }

}
