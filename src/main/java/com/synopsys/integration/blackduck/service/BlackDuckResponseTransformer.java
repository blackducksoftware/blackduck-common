/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class BlackDuckResponseTransformer {
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;

    public BlackDuckResponseTransformer(BlackDuckHttpClient blackDuckHttpClient, BlackDuckJsonTransformer blackDuckJsonTransformer) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
    }

    public <T extends BlackDuckResponse> T getResponse(Request request, Class<T> clazz) throws IntegrationException {
        try (Response response = blackDuckHttpClient.execute(request)) {
            blackDuckHttpClient.throwExceptionForError(response);
            return blackDuckJsonTransformer.getResponse(response, clazz);
        } catch (IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    public <T extends BlackDuckResponse> T getResponseAs(String json, Class<T> clazz) throws BlackDuckIntegrationException {
        return blackDuckJsonTransformer.getResponseAs(json, clazz);
    }

    public <T extends BlackDuckResponse> T getResponseAs(JsonElement jsonElement, Class<T> clazz) throws BlackDuckIntegrationException {
        return blackDuckJsonTransformer.getResponseAs(jsonElement, clazz);
    }

}
