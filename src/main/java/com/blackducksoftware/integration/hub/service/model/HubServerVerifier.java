/**
 * hub-common
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
package com.blackducksoftware.integration.hub.service.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.cli.CLILocation;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;
import com.blackducksoftware.integration.rest.RestConstants;
import com.blackducksoftware.integration.rest.connection.UnauthenticatedRestConnection;
import com.blackducksoftware.integration.rest.connection.UnauthenticatedRestConnectionBuilder;
import com.blackducksoftware.integration.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;
import com.blackducksoftware.integration.rest.request.Request;
import com.blackducksoftware.integration.rest.request.Response;

public class HubServerVerifier {

    public void verifyIsHubServer(final URL hubURL, final ProxyInfo hubProxyInfo,
            final boolean alwaysTrustServerCertificate, final int timeoutSeconds) throws IntegrationException {
        final UnauthenticatedRestConnectionBuilder connectionBuilder = new UnauthenticatedRestConnectionBuilder();
        connectionBuilder.setLogger(new PrintStreamIntLogger(System.out, LogLevel.INFO));
        connectionBuilder.setBaseUrl(hubURL.toString());
        connectionBuilder.setTimeout(timeoutSeconds);
        connectionBuilder.setAlwaysTrustServerCertificate(alwaysTrustServerCertificate);
        if (hubProxyInfo != null) {
            connectionBuilder.applyProxyInfo(hubProxyInfo);
        }
        final UnauthenticatedRestConnection restConnection = connectionBuilder.build();

        try {
            Request request = new Request.Builder(hubURL.toURI().toString()).build();
            try (Response response = restConnection.executeRequest(request)) {
            } catch (final IntegrationRestException e) {
                if (e.getHttpStatusCode() == RestConstants.UNAUTHORIZED_401 || e.getHttpStatusCode() == RestConstants.FORBIDDEN_403) {
                    // This could be a Hub server
                } else {
                    throw e;
                }
            } catch (final IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
            URL downloadURL;
            try {
                downloadURL = new URL(hubURL, "download/" + CLILocation.DEFAULT_CLI_DOWNLOAD);
            } catch (MalformedURLException e) {
                throw new HubIntegrationException("Error constructing the download URL : " + e.getMessage(), e);
            }
            String downloadUri = downloadURL.toString();
            request = RequestFactory.createCommonGetRequest(downloadUri);
            try (Response response = restConnection.executeRequest(request)) {
            } catch (final IntegrationRestException e) {
                throw new HubIntegrationException("The Url does not appear to be a Hub server :" + downloadUri + ", because: " + e.getHttpStatusCode() + " : " + e.getHttpStatusMessage(), e);
            } catch (final IntegrationException e) {
                throw new HubIntegrationException("The Url does not appear to be a Hub server :" + downloadUri + ", because: " + e.getMessage(), e);
            } catch (final IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        } catch (final URISyntaxException e) {
            throw new IntegrationException("The Url does not appear to be a Hub server :" + hubURL.toString() + ", because: " + e.getMessage(), e);
        }
    }

}
