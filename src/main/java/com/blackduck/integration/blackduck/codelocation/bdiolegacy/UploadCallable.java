/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.bdiolegacy;

import com.blackduck.integration.blackduck.api.core.response.UrlSingleResponse;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.manual.response.BlackDuckStringResponse;
import com.blackduck.integration.blackduck.codelocation.upload.UploadOutput;
import com.blackduck.integration.blackduck.codelocation.upload.UploadTarget;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.blackduck.integration.rest.response.Response;
import com.blackduck.integration.util.NameVersion;
import org.apache.commons.io.FileUtils;
import org.apache.http.entity.ContentType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

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
