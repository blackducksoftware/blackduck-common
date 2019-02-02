/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.rest;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;

import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.blackduck.api.component.ErrorResponse;
import com.synopsys.integration.blackduck.exception.BlackDuckApiException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.AuthenticatingIntHttpClient;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;

/**
 * A BlackDuckRestConnection will always decorate the provided RestConnection with a ReconnectingRestConnection
 */
public abstract class BlackDuckHttpClient extends AuthenticatingIntHttpClient {
    private final String baseUrl;

    public BlackDuckHttpClient(IntLogger logger, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, String baseUrl) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo);
        this.baseUrl = baseUrl;

        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException("No base url was provided.");
        } else {
            try {
                URL url = new URL(baseUrl);
                url.toURI();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("The provided base url is not a valid java.net.URL.", e);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("The provided base url is not a valid java.net.URI.", e);
            }
        }
    }

    @Override
    public Response execute(HttpUriRequest request) throws IntegrationException {
        try {
            return super.execute(request);
        } catch (IntegrationRestException e) {
            throw transformException(e);
        }
    }

    public void throwExceptionForError(Response response) throws IntegrationException {
        try {
            response.throwExceptionForError();
        } catch (IntegrationRestException e) {
            throw transformException(e);
        }
    }

    public Optional<ErrorResponse> extractErrorResponse(String responseContent) {
        if (StringUtils.isNotBlank(responseContent)) {
            try {
                String errorMessage = JsonPath.read(responseContent, "$.errorMessage");
                String errorCode = JsonPath.read(responseContent, "$.errorCode");
                if (!StringUtils.isAllBlank(errorMessage, errorCode)) {
                    return Optional.of(new ErrorResponse(errorMessage, errorCode));
                }
            } catch (Exception ignored) {
                //ignored
            }
        }
        return Optional.empty();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private IntegrationException transformException(IntegrationRestException e) {
        String httpResponseContent = e.getHttpResponseContent();
        Optional<ErrorResponse> optionalErrorResponse = extractErrorResponse(httpResponseContent);
        // Not all IntegrationRestExceptions are from Black Duck - if we were able to
        // transform the IntegrationRestException, we want to return the resulting
        // BlackDuckApiException, otherwise, we want to ignore any potential
        // transformation and just return the original IntegrationRestException
        if (optionalErrorResponse.isPresent()) {
            ErrorResponse errorResponse = optionalErrorResponse.get();
            String apiExceptionErrorMessage = String.format("[Black Duck Error Message]: %s [Integration Error Message]: %s", errorResponse.getErrorMessage(), e.getMessage());
            return new BlackDuckApiException(e, apiExceptionErrorMessage, errorResponse.getErrorCode());
        } else {
            return e;
        }
    }

}
