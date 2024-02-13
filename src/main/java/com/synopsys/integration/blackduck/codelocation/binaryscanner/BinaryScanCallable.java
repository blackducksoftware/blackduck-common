/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.manual.response.BlackDuckStringResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.NameVersion;

public class BinaryScanCallable implements Callable<BinaryScanOutput> {
    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;
    private final BinaryScan binaryScan;
    private final NameVersion projectAndVersion;
    private final String codeLocationName;

    public BinaryScanCallable(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, BinaryScan binaryScan) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
        this.binaryScan = binaryScan;
        this.projectAndVersion = new NameVersion(binaryScan.getProjectName(), binaryScan.getProjectVersion());
        this.codeLocationName = binaryScan.getCodeLocationName();
    }

    @Override
    public BinaryScanOutput call() {
        try {
            Map<String, String> textParts = new HashMap<>();
            textParts.put("projectName", binaryScan.getProjectName());
            textParts.put("version", binaryScan.getProjectVersion());
            textParts.put("codeLocationName", binaryScan.getCodeLocationName());

            Map<String, File> binaryParts = new HashMap<>();
            binaryParts.put("fileupload", binaryScan.getBinaryFile());

            UrlSingleResponse<BlackDuckStringResponse> stringResponse = apiDiscovery.metaUploadsLink();
            BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
                                                   .postMultipart(binaryParts, textParts)
                                                   .buildBlackDuckResponseRequest(stringResponse.getUrl());
            try (Response response = blackDuckApiClient.execute(request)) {
                return BinaryScanOutput.FROM_RESPONSE(projectAndVersion, codeLocationName, response);
            }
        } catch (IntegrationRestException e) {
            return BinaryScanOutput.FROM_INTEGRATION_REST_EXCEPTION(projectAndVersion, codeLocationName, e);
        } catch (IntegrationException | IOException e) {
            String errorMessage = String.format("Failed to upload binary file: %s because %s", binaryScan.getBinaryFile().getAbsolutePath(), e.getMessage());
            return BinaryScanOutput.FAILURE(projectAndVersion, codeLocationName, errorMessage, e);
        }
    }

}
