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

import java.net.URL;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.rest.connection.RestConnectionBuilder;

public class ApiTokenRestConnectionBuilder extends RestConnectionBuilder<ApiTokenRestConnection> {
    private String apiToken;

    @Override
    protected ApiTokenRestConnection buildWithoutValidation() {
        final Optional<URL> url = getURL();
        return new ApiTokenRestConnection(getLogger(), url.orElse(null), apiToken, getTimeout(), getProxyInfo());
    }

    @Override
    public void populateIndividualErrorMessages() {
        super.populateIndividualErrorMessages();

        if (StringUtils.isBlank(apiToken)) {
            errorMessages.add("No API token was found.");
        }
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(final String apiToken) {
        this.apiToken = apiToken;
    }

}
