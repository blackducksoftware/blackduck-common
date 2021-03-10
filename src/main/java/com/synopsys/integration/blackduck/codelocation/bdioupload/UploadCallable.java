/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.NameVersion;

public class UploadCallable implements Callable<UploadOutput> {
    private final BlackDuckApiClient blackDuckApiClient;
    private final BlackDuckRequestFactory blackDuckRequestFactory;
    private final UploadTarget uploadTarget;
    private final NameVersion projectAndVersion;
    private final String codeLocationName;

    public UploadCallable(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, UploadTarget uploadTarget) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.blackDuckRequestFactory = blackDuckRequestFactory;
        this.uploadTarget = uploadTarget;
        this.projectAndVersion = uploadTarget.getProjectAndVersion();
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

            HttpUrl url = blackDuckApiClient.getUrl(BlackDuckApiClient.BOMIMPORT_PATH);
            Request request = blackDuckRequestFactory
                                  .createCommonPostRequestBuilder(url, jsonPayload)
                                  .acceptMimeType(uploadTarget.getMediaType())
                                  .build();
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
