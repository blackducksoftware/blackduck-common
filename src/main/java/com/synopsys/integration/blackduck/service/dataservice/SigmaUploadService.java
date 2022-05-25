/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import org.apache.http.entity.ContentType;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;

public class SigmaUploadService {
    public static final String SIGMA_UPLOAD_PATH_PATTERN = "/api/internal/scans/%s/iac-issues";
    public static final String CONTENT_TYPE = "application/vnd.blackducksoftware.scan-6+json";

    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;

    public SigmaUploadService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
    }

    public Response uploadSigmaResults(String resultsFileContent, String scanId) throws IntegrationException {
        HttpUrl url = apiDiscovery.metaSingleResponse(getUploadEndpoint(scanId)).getUrl();
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
            .postString(resultsFileContent, ContentType.create(CONTENT_TYPE))
            .buildBlackDuckResponseRequest(url);
        return blackDuckApiClient.execute(request);

    }

    private BlackDuckPath<BlackDuckResponse> getUploadEndpoint(String scanId) {
        return new BlackDuckPath<>(String.format(SIGMA_UPLOAD_PATH_PATTERN, scanId), BlackDuckResponse.class, false);
    }
}
