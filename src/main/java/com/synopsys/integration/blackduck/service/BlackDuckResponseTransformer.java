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
package com.synopsys.integration.blackduck.service;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class BlackDuckResponseTransformer {
    private final BlackDuckRestConnection restConnection;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;

    public BlackDuckResponseTransformer(final BlackDuckRestConnection restConnection, final BlackDuckJsonTransformer blackDuckJsonTransformer) {
        this.restConnection = restConnection;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
    }

    public <T extends BlackDuckResponse> T getResponse(final Request request, final Class<T> clazz) throws IntegrationException {
        try (final Response response = restConnection.executeRequest(request)) {
            return blackDuckJsonTransformer.getResponse(response, clazz);
        } catch (final IOException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        }
    }

    public <T extends BlackDuckResponse> T getResponseAs(final String json, final Class<T> clazz) throws HubIntegrationException {
        return blackDuckJsonTransformer.getResponseAs(json, clazz);
    }

    public <T extends BlackDuckResponse> T getResponseAs(final JsonElement jsonElement, final Class<T> clazz) throws HubIntegrationException {
        return blackDuckJsonTransformer.getResponseAs(jsonElement, clazz);
    }

}