/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http.transform;

import java.io.IOException;

import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.service.request.BlackDuckRequest;
import com.google.gson.JsonElement;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.response.Response;

public class BlackDuckResponseTransformer {
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;

    public BlackDuckResponseTransformer(BlackDuckHttpClient blackDuckHttpClient, BlackDuckJsonTransformer blackDuckJsonTransformer) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckRequest<T, UrlSingleResponse<T>> requestSingle) throws IntegrationException {
        try (Response response = blackDuckHttpClient.execute(requestSingle)) {
            blackDuckHttpClient.throwExceptionForError(response);
            return blackDuckJsonTransformer.getResponse(response, requestSingle.getResponseClass());
        } catch (IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    public <T extends BlackDuckResponse> T getResponseAs(String json, Class<T> clazz) throws BlackDuckIntegrationException {
        return blackDuckJsonTransformer.getResponseAs(json, clazz);
    }

    public <T extends BlackDuckResponse> T getResponseAs(JsonElement jsonElement, Class<T> clazz) throws BlackDuckIntegrationException {
        return blackDuckJsonTransformer.getResponseAs(jsonElement, clazz);
    }

}
