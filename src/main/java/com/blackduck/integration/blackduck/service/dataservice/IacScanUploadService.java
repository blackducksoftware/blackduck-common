/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import org.apache.http.entity.ContentType;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;

public class IacScanUploadService {
    public static final String IAC_SCAN_UPLOAD_PATH_PATTERN = "/api/internal/scans/%s/iac-issues";
    public static final String HEADER_CONTENT_TYPE = "Content-type";
    public static final String CONTENT_TYPE = "application/vnd.blackducksoftware.scan-6+json";

    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;

    public IacScanUploadService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
    }

    //TODO- do we need to implement waiting?
    public Response uploadIacScanResults(String resultsFileContent, String scanId) throws IntegrationException {
        HttpUrl url = apiDiscovery.metaSingleResponse(getUploadEndpoint(scanId)).getUrl();
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
            .postString(resultsFileContent, ContentType.create(CONTENT_TYPE))
            .addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE)
            .buildBlackDuckResponseRequest(url);
        return blackDuckApiClient.execute(request);

    }

    private BlackDuckPath<BlackDuckResponse> getUploadEndpoint(String scanId) {
        return new BlackDuckPath<>(String.format(IAC_SCAN_UPLOAD_PATH_PATTERN, scanId), BlackDuckResponse.class, false);
    }
}
