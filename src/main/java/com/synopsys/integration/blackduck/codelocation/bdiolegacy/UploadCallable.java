/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdiolegacy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.http.entity.ContentType;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.manual.response.BlackDuckStringResponse;
import com.synopsys.integration.blackduck.codelocation.upload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.NameVersion;

public class UploadCallable implements Callable<UploadOutput> {
    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;
    private final UploadTarget uploadTarget;
    @Nullable
    private final NameVersion projectAndVersion;
    private final String codeLocationName;

    public UploadCallable(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, UploadTarget uploadTarget) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
        this.uploadTarget = uploadTarget;
        this.projectAndVersion = uploadTarget.getProjectAndVersion().orElse(null);
        this.codeLocationName = uploadTarget.getCodeLocationName();
    }

    @Override
    public UploadOutput call() {
        try {
            String jsonPayload;
            try {
                jsonPayload = FileUtils.readFileToString(uploadTarget.getUploadFile(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                String errorMessage = String.format("Failed to initially read file: %s because %s", uploadTarget.getUploadFile().getAbsolutePath(), e.getMessage());
                return UploadOutput.FAILURE(projectAndVersion, codeLocationName, errorMessage, e);
            }

            UrlSingleResponse<BlackDuckStringResponse> stringResponse = new UrlSingleResponse<>(apiDiscovery.metaBomImportLink().getUrl(), BlackDuckStringResponse.class);
            BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
                                                   .postString(jsonPayload, ContentType.create(uploadTarget.getMediaType(), StandardCharsets.UTF_8))
                                                   .buildBlackDuckResponseRequest(stringResponse.getUrl());

            try (Response response = blackDuckApiClient.execute(request)) {
                String responseString = response.getContentString();
                return UploadOutput.SUCCESS(projectAndVersion, codeLocationName, responseString);
            } catch (IOException e) {
                return UploadOutput.FAILURE(projectAndVersion, codeLocationName, e.getMessage(), e);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Failed to upload file: %s because %s", uploadTarget.getUploadFile().getAbsolutePath(), e.getMessage());
            return UploadOutput.FAILURE(projectAndVersion, codeLocationName, errorMessage, e);
        }
    }

}
