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
package com.blackducksoftware.integration.hub.rest;

import java.io.File;
import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.core.HubComponent;
import com.google.gson.JsonObject;

public class UpdateRequestWrapper extends BaseRequestWrapper {
    private String bodyContent;
    private Map<String, String> bodyContentMap;
    private File bodyContentFile;
    private HubComponent hubComponent;
    private JsonObject jsonObject;
    private final HttpMethod method;

    public UpdateRequestWrapper(final HttpMethod method) throws IntegrationException {
        if (null == method) {
            throw new IntegrationException("The HttpMethod can not be null");
        }
        if (HttpMethod.GET == method) {
            throw new IntegrationException("The HttpMethod can not be GET");
        }
        this.method = method;
    }

    public UpdateRequestWrapper(final HttpMethod method, final String bodyContent) throws IntegrationException {
        this(method);
        this.bodyContent = bodyContent;
    }

    public UpdateRequestWrapper(final HttpMethod method, final HubComponent hubComponent) throws IntegrationException {
        this(method);
        this.hubComponent = hubComponent;
    }

    public UpdateRequestWrapper(final HttpMethod method, final JsonObject jsonObject) throws IntegrationException {
        this(method);
        this.jsonObject = jsonObject;
    }

    public UpdateRequestWrapper(final HttpMethod method, final Map<String, String> bodyContentMap) throws IntegrationException {
        this(method);
        this.bodyContentMap = bodyContentMap;
    }

    public UpdateRequestWrapper(final HttpMethod method, final File bodyContentFile) throws IntegrationException {
        this(method);
        this.bodyContentFile = bodyContentFile;
    }

    public HubComponent getHubComponent() {
        return hubComponent;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public Map<String, String> getBodyContentMap() {
        return bodyContentMap;
    }

    public File getBodyContentFile() {
        return bodyContentFile;
    }
}
