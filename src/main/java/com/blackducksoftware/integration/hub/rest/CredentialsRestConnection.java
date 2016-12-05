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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.log.IntLogger;

public class CredentialsRestConnection extends RestConnection {

    private final HubServerConfig hubServerConfig;

    private Series<Cookie> cookies;

    public CredentialsRestConnection(final HubServerConfig hubServerConfig) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        this(null, hubServerConfig);
    }

    public CredentialsRestConnection(final IntLogger logger, final HubServerConfig hubServerConfig)
            throws IllegalArgumentException, HubIntegrationException {
        super(logger);
        this.hubServerConfig = hubServerConfig;

        setBaseUrl(hubServerConfig.getHubUrl().toString());
        final HubProxyInfo proxyInfo = hubServerConfig.getProxyInfo();
        if (proxyInfo.shouldUseProxyForUrl(hubServerConfig.getHubUrl())) {
            setProxyProperties(proxyInfo);
        }
        setTimeout(hubServerConfig.getTimeout());
    }

    @Override
    public void connect() throws HubIntegrationException {
        final String username = hubServerConfig.getGlobalCredentials().getUsername();
        String password = hubServerConfig.getGlobalCredentials().getEncryptedPassword();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            try {
                password = hubServerConfig.getGlobalCredentials().getDecryptedPassword();
                setCookies(username, password);
            } catch (IllegalArgumentException | EncryptionException e) {
                throw new HubIntegrationException(e.getMessage(), e);
            }
        }
    }

    @Override
    public ClientResource createClientResource(final Context context, final String providedUrl) throws HubIntegrationException {
        try {
            ClientResource resource = new ClientResource(context, new URI(providedUrl));
            resource.getRequest().setCookies(getCookies());
            return resource;
        } catch (final URISyntaxException e) {
            throw new HubIntegrationException(String.format("The providedUrl (%s) was invalid.", providedUrl), e);
        }
    }

    /**
     * Gets the cookie for the Authorized connection to the Hub server. Returns
     * the response code from the connection.
     *
     */
    public int setCookies(final String hubUserName, final String hubPassword)
            throws HubIntegrationException {
        final ClientResource resource = createClientResource();
        try {
            resource.addSegment("j_spring_security_check");
            resource.setMethod(Method.POST);

            String encodedHubUser = null;
            String encodedHubPassword = null;
            try {
                encodedHubUser = URLEncoder.encode(hubUserName, "UTF-8");
                encodedHubPassword = URLEncoder.encode(hubPassword, "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                throw new HubIntegrationException("Could not encode the HubUsername and Password", e);
            }

            final StringRepresentation stringRep = new StringRepresentation(
                    "j_username=" + encodedHubUser + "&j_password=" + encodedHubPassword);
            stringRep.setCharacterSet(CharacterSet.UTF_8);
            stringRep.setMediaType(MediaType.APPLICATION_WWW_FORM);
            resource.getRequest().setEntity(stringRep);

            handleRequest(resource);

            final int statusCode = resource.getResponse().getStatus().getCode();
            if (isSuccess(statusCode)) {
                final Series<CookieSetting> cookieSettings = resource.getResponse().getCookieSettings();
                final Series<Cookie> requestCookies = resource.getRequest().getCookies();
                if (cookieSettings != null && !cookieSettings.isEmpty()) {
                    for (final CookieSetting ck : cookieSettings) {
                        if (ck == null) {
                            continue;
                        }
                        final Cookie cookie = new Cookie();
                        cookie.setName(ck.getName());
                        cookie.setDomain(ck.getDomain());
                        cookie.setPath(ck.getPath());
                        cookie.setValue(ck.getValue());
                        cookie.setVersion(ck.getVersion());
                        requestCookies.add(cookie);
                    }
                }

                if (requestCookies == null || requestCookies.size() == 0) {
                    throw new HubIntegrationException(
                            "Could not establish connection to '" + getBaseUrl() + "' . Failed to retrieve cookies");
                }
                cookies = requestCookies;
            } else {
                getLogger().trace("Response entity : " + resource.getResponse().getEntityAsText());
                final Status status = resource.getResponse().getStatus();
                getLogger().trace("Status : " + status.toString(), status.getThrowable());
                throw new HubIntegrationException(resource.getResponse().getStatus().toString(), status.getThrowable());
            }
            return statusCode;
        } finally {
            releaseResource(resource);
        }
    }

    public Series<Cookie> getCookies() {
        return cookies;
    }
}
