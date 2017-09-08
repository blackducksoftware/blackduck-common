/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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
        try {
            restConnection.handleExecuteClientCall(request);
        } catch (final IntegrationRestException e) {
            if (e.getHttpStatusCode() == 401 && e.getHttpStatusCode() == 403) {
                // This could be a Hub server
            } else {
                throw e;
            }
        }
        final List<String> urlSegments = new ArrayList<>();
        urlSegments.add("download");
        urlSegments.add(CLILocation.DEFAULT_CLI_DOWNLOAD);
        httpUrl = restConnection.createHttpUrl(urlSegments);
        request = restConnection.createGetRequest(httpUrl);
        try {
            restConnection.handleExecuteClientCall(request);
        } catch (final IntegrationRestException e) {
            throw new HubIntegrationException("The Url does not appear to be a Hub server :" + httpUrl.uri().toString() + ", because: " + e.getHttpStatusCode() + " : " + e.getHttpStatusMessage(), e);
        } catch (final IntegrationException e) {
            throw new HubIntegrationException("The Url does not appear to be a Hub server :" + httpUrl.uri().toString() + ", because: " + e.getMessage(), e);
        }
    }

}
