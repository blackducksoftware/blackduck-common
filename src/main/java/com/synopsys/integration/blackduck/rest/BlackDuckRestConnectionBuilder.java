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

import com.synopsys.integration.rest.connection.RestConnectionBuilder;
import com.synopsys.integration.util.BuilderStatus;

public abstract class BlackDuckRestConnectionBuilder<C extends BlackDuckRestConnection> extends RestConnectionBuilder<C> {
    protected String baseUrl;

    @Override
    protected void validate(final BuilderStatus builderStatus) {
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

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Used by implementation classes to retrieve the url. Assumes the baseUrl field has been validated
     */
    protected URL getBaseUrl() {
        try {
            return new URL(baseUrl);
        } catch (final MalformedURLException ignored) {
            return null;
        }
    }
}
