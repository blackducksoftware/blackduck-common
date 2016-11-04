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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.restlet.Context;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.log.IntLogger;

public class CredentialsRestConnection extends RestConnection {

    public CredentialsRestConnection(final HubServerConfig hubServerConfig)
            throws IllegalArgumentException, URISyntaxException, BDRestException, EncryptionException {
        this(null, hubServerConfig);
    }
    
    public CredentialsRestConnection(final IntLogger logger, final HubServerConfig hubServerConfig)
            throws IllegalArgumentException, URISyntaxException, BDRestException, EncryptionException {
        super(logger);
        setBaseUrl(hubServerConfig.getHubUrl().toString());
        final HubProxyInfo proxyInfo = hubServerConfig.getProxyInfo();
        if (proxyInfo.shouldUseProxyForUrl(hubServerConfig.getHubUrl())) {
            setProxyProperties(proxyInfo);
        }
        setTimeout(hubServerConfig.getTimeout());
        final String username = hubServerConfig.getGlobalCredentials().getUsername();
        String password = hubServerConfig.getGlobalCredentials().getEncryptedPassword();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            password = hubServerConfig.getGlobalCredentials().getDecryptedPassword();
            setCookies(username, password);
        }
    }

    @Override
    public ClientResource createClientResource(final Context context, final String providedUrl)
            throws URISyntaxException {
        return new ClientResource(context, new URI(providedUrl));
    }
}
