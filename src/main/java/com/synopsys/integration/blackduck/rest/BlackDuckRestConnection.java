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
package com.synopsys.integration.blackduck.rest;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.connection.RestConnectionDecorator;
import com.synopsys.integration.util.BuilderStatus;

/**
 * A BlackDuckRestConnection will always decorate the provided RestConnection with a ReconnectingRestConnection
 */
public abstract class BlackDuckRestConnection extends RestConnectionDecorator {
    private final String baseUrl;

    public BlackDuckRestConnection(final RestConnection restConnection, final String baseUrl) {
        super(restConnection);
        this.baseUrl = baseUrl;
    }

    @Override
    public void validate(final BuilderStatus builderStatus) {
        super.validate(builderStatus);

        if (StringUtils.isBlank(baseUrl)) {
            builderStatus.addErrorMessage("No base url was provided.");
        } else {
            try {
                final URL url = new URL(baseUrl);
                url.toURI();
            } catch (final MalformedURLException e) {
                builderStatus.addErrorMessage("The provided base url is not a valid java.net.URL.");
            } catch (final URISyntaxException e) {
                builderStatus.addErrorMessage("The provided base url is not a valid java.net.URI.");
            }
        }
    }

    public abstract void authenticateWithBlackDuck() throws IntegrationException;

    @Override
    public void completeConnection() throws IntegrationException {
        super.completeConnection();
        authenticateWithBlackDuck();
    }

    public URL getBaseUrl() {
        try {
            return new URL(baseUrl);
        } catch (final MalformedURLException e) {
            return null;
        }
    }
}
