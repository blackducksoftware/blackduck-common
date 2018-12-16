/**
 * blackduck-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.blackduck.exception.BlackDuckApiException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.connection.ReconnectingRestConnection;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;

/**
 * A BlackDuckRestConnection will always decorate the provided RestConnection with a ReconnectingRestConnection
 */
public abstract class BlackDuckRestConnection extends ReconnectingRestConnection {
    private final String baseUrl;

    public BlackDuckRestConnection(final IntLogger logger, final int timeout, final boolean alwaysTrustServerCertificate, final ProxyInfo proxyInfo, final String baseUrl) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo);
        this.baseUrl = baseUrl;

        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException("No base url was provided.");
        } else {
            try {
                final URL url = new URL(baseUrl);
                url.toURI();
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException("The provided base url is not a valid java.net.URL.", e);
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException("The provided base url is not a valid java.net.URI.", e);
            }
        }
    }

    public URL getBaseUrl() {
        try {
            return new URL(baseUrl);
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    public abstract Response attemptAuthentication() throws IntegrationException, IOException;

    public void throwExceptionForError(final Response response) throws IntegrationRestException, BlackDuckApiException {
        try {
            response.throwExceptionForError();
        } catch (final IntegrationRestException e) {
            final String httpResponseContent = e.getHttpResponseContent();
            if (StringUtils.isNotBlank(httpResponseContent)) {
                final String errorMessage = JsonPath.read(httpResponseContent, "$.errorMessage");
                final String errorCode = JsonPath.read(httpResponseContent, "$.errorCode");
                if (!StringUtils.isAllBlank(errorMessage, errorCode)) {
                    throw new BlackDuckApiException(e, errorMessage, errorCode);
                }
            }
            throw e;
        }
    }

}
