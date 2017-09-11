/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.validator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.cli.CLILocation;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class HubServerVerifier {

    private final URL hubURL;

    private final HubProxyInfo hubProxyInfo;

    private final int timeoutSeconds;

    private final boolean alwaysTrustServerCertificate;

    public HubServerVerifier(final URL hubURL, final HubProxyInfo hubProxyInfo, final boolean alwaysTrustServerCertificate, final int timeoutSeconds) {
        this.hubURL = hubURL;
        this.hubProxyInfo = hubProxyInfo;
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
        this.timeoutSeconds = timeoutSeconds;
    }

    public void verifyIsHubServer() throws IntegrationException {
        final UnauthenticatedRestConnection restConnection = new UnauthenticatedRestConnection(new PrintStreamIntLogger(System.out, LogLevel.INFO), hubURL, timeoutSeconds);
        restConnection.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
        if (hubProxyInfo != null) {
            restConnection.proxyHost = hubProxyInfo.getHost();
            restConnection.proxyPort = hubProxyInfo.getPort();
            restConnection.proxyNoHosts = hubProxyInfo.getIgnoredProxyHosts();
            restConnection.proxyUsername = hubProxyInfo.getUsername();
            restConnection.proxyPassword = hubProxyInfo.getDecryptedPassword();
        }

        HttpUrl httpUrl = restConnection.createHttpUrl();
        Request request = restConnection.createGetRequest(httpUrl);
        Response response = null;
        try {
            response = restConnection.handleExecuteClientCall(request);
        } catch (final IntegrationRestException e) {
            if (e.getHttpStatusCode() == 401 && e.getHttpStatusCode() == 403) {
                // This could be a Hub server
            } else {
                throw e;
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
        final List<String> urlSegments = new ArrayList<>();
        urlSegments.add("download");
        urlSegments.add(CLILocation.DEFAULT_CLI_DOWNLOAD);
        httpUrl = restConnection.createHttpUrl(urlSegments);
        request = restConnection.createGetRequest(httpUrl);
        try {
            response = restConnection.handleExecuteClientCall(request);
        } catch (final IntegrationRestException e) {
            throw new HubIntegrationException("The Url does not appear to be a Hub server :" + httpUrl.uri().toString() + ", because: " + e.getHttpStatusCode() + " : " + e.getHttpStatusMessage(), e);
        } catch (final IntegrationException e) {
            throw new HubIntegrationException("The Url does not appear to be a Hub server :" + httpUrl.uri().toString() + ", because: " + e.getMessage(), e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

}
