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
import com.synopsys.integration.blackduck.http.BlackDuckMediaTypes;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.wait.WaitJob;

public class RapidScanWaiter {
    private IntLogger logger;
    private BlackDuckApiClient blackDuckApiClient;

    public RapidScanWaiter(final IntLogger logger, final BlackDuckApiClient blackDuckApiClient) {
        this.logger = logger;
        this.blackDuckApiClient = blackDuckApiClient;
    }

    public List<DeveloperScanComponentResultView> checkScanResult(HttpUrl url, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        RapidScanWaitJobTask waitTask = new RapidScanWaitJobTask(blackDuckApiClient, url);
        // if a timeout of 0 is provided and the timeout check is done too quickly, w/o a do/while, no check will be performed
        // regardless of the timeout provided, we always want to check at least once
        boolean allCompleted = waitTask.isComplete();

        // waitInterval needs to be less than the timeout
        if (waitIntervalInSeconds > timeoutInSeconds) {
            waitIntervalInSeconds = (int) timeoutInSeconds;
        }

        if (!allCompleted) {
            WaitJob waitJob = WaitJob.create(logger, timeoutInSeconds, System.currentTimeMillis(), waitIntervalInSeconds, waitTask);
            allCompleted = waitJob.waitFor();
        }

        if (!allCompleted) {
            throw new BlackDuckIntegrationException("Error getting developer scan result. Timeout may have occurred.");
        }
        Request.Builder requestBuilder = new Request.Builder(url);
        requestBuilder.acceptMimeType(BlackDuckMediaTypes.APPLICATION_SCAN_V4);
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder(requestBuilder);
        return blackDuckApiClient.getAllResponses(blackDuckRequestBuilder, DeveloperScanComponentResultView.class);
    }
}
