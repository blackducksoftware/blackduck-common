/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import java.io.IOException;

import org.apache.http.HttpStatus;

import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.wait.WaitJobTask;

public class RapidScanWaitJobTask implements WaitJobTask {
    private HttpUrl resultUrl;
    private BlackDuckApiClient blackDuckApiClient;

    public RapidScanWaitJobTask(BlackDuckApiClient blackDuckApiClient, HttpUrl resultUrl) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.resultUrl = resultUrl;
    }

    @Override
    public boolean isComplete() throws IntegrationException {
        Request.Builder requestBuilder = new Request.Builder(resultUrl);
        requestBuilder.acceptMimeType(DeveloperScanComponentResultView.CURRENT_MEDIA_TYPE);
        Request request = requestBuilder.build();
        try (Response response = blackDuckApiClient.execute(request)) {
            return response.isStatusCodeSuccess();
        } catch (IntegrationRestException ex) {
            if (HttpStatus.SC_NOT_FOUND == ex.getHttpStatusCode()) {
                return false;
            } else {
                throw ex;
            }
        } catch (IOException ex) {
            throw new BlackDuckIntegrationException(ex.getMessage(), ex);
        }
    }

}
