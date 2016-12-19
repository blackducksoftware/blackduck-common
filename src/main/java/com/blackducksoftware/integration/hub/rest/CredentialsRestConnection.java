/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.rest;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.log.IntLogger;

import okhttp3.HttpUrl;
import okhttp3.JavaNetCookieJar;
import okhttp3.Request;
import okhttp3.Response;

public class CredentialsRestConnection extends RestConnection {
    private final HubServerConfig hubServerConfig;

    public CredentialsRestConnection(final HubServerConfig hubServerConfig) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        this(null, hubServerConfig);
    }

    public CredentialsRestConnection(final IntLogger logger, final HubServerConfig hubServerConfig)
            throws IllegalArgumentException, HubIntegrationException {
        super(logger, hubServerConfig.getHubUrl(), hubServerConfig.getProxyInfo());
        this.hubServerConfig = hubServerConfig;
        setTimeout(hubServerConfig.getTimeout());
    }

    @Override
    public void connect() throws HubIntegrationException {
        final String username = hubServerConfig.getGlobalCredentials().getUsername();
        String password = hubServerConfig.getGlobalCredentials().getEncryptedPassword();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            try {
                password = hubServerConfig.getGlobalCredentials().getDecryptedPassword();
                final CookieManager cookieManager = new CookieManager();
                cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                getBuilder().cookieJar(new JavaNetCookieJar(cookieManager));
                setupClient();
                setClient(getBuilder().build());
                setCookies(username, password);
            } catch (IllegalArgumentException | EncryptionException e) {
                throw new HubIntegrationException(e.getMessage(), e);
            }
        }
    }

    /**
     * Gets the cookie for the Authorized connection to the Hub server. Returns
     * the response code from the connection.
     *
     */
    public void setCookies(final String hubUserName, final String hubPassword)
            throws HubIntegrationException {
        try {
            final ArrayList<String> segments = new ArrayList<>();
            segments.add("j_spring_security_check");
            final HttpUrl httpUrl = createHttpUrl(segments, null);

            final Map<String, String> content = new HashMap<>();
            content.put("j_username", hubUserName);
            content.put("j_password", hubPassword);

            final Request request = createPostRequest(httpUrl, createEncodedRequestBody(content));
            final Response response = handleExecuteClientCall(request);
            if (!response.isSuccessful()) {
                throw new HubIntegrationException(response.message());
            }
        } catch (final IOException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        }
    }

    public HubServerConfig getHubServerConfig() {
        return hubServerConfig;
    }

}
