/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.bdio2legacy;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.codelocation.upload.UploadOutput;
import com.blackduck.integration.blackduck.codelocation.upload.UploadTarget;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.blackduck.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.response.Response;
import com.blackduck.integration.util.NameVersion;
import org.apache.http.entity.ContentType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class UploadBdio2Callable implements Callable<UploadOutput> {
    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;
    private final UploadTarget uploadTarget;
    @Nullable
    private final NameVersion projectAndVersion;
    private final String codeLocationName;
    private final BlackDuckRequestBuilderEditor editor;

    public UploadBdio2Callable(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, UploadTarget uploadTarget, BlackDuckRequestBuilderEditor editor) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
        this.uploadTarget = uploadTarget;
        this.projectAndVersion = uploadTarget.getProjectAndVersion().orElse(null);
        this.codeLocationName = uploadTarget.getCodeLocationName();
        this.editor = editor;
    }

    @Override
    public UploadOutput call() {
        try {
            HttpUrl url = apiDiscovery.metaSingleResponse(BlackDuckApiClient.SCAN_DATA_PATH).getUrl();
            BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
                                                   .postFile(uploadTarget.getUploadFile(), ContentType.create(uploadTarget.getMediaType(), StandardCharsets.UTF_8))
                                                   .apply(editor)
                                                   .buildBlackDuckResponseRequest(url);
            return executeRequest(request);
        } catch (Exception e) {
            String errorMessage = String.format("Failed to upload file: %s because %s", uploadTarget.getUploadFile().getAbsolutePath(), e.getMessage());
            return UploadOutput.FAILURE(projectAndVersion, codeLocationName, errorMessage, e);
        }
    }

    private UploadOutput executeRequest(BlackDuckResponseRequest request) throws IOException {
        try (Response response = blackDuckApiClient.execute(request)) {
            String responseString = response.getContentString();
            return UploadOutput.SUCCESS(projectAndVersion, codeLocationName, responseString);
        } catch (IntegrationException e) {
            return UploadOutput.FAILURE(projectAndVersion, codeLocationName, e.getMessage(), e);
        }
    }

}
