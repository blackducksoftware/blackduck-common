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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.Charsets;
import org.apache.http.entity.ContentType;

import com.blackducksoftware.integration.util.Stringable;

public class GetRequestWrapper extends Stringable {
    private final Map<String, String> queryParameters = new HashMap<>();
    private final Map<String, String> additionalHeaders = new HashMap<>();
    private String q;
    private String mimeType = ContentType.APPLICATION_JSON.getMimeType();
    private Charset bodyEncoding = Charsets.UTF_8;
    private int limitPerRequest = 100;

    public String getQ() {
        return q;
    }

    public void setQ(final String q) {
        this.q = q;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public Charset getBodyEncoding() {
        return bodyEncoding;
    }

    public void setBodyEncoding(final Charset bodyEncoding) {
        this.bodyEncoding = bodyEncoding;
    }

    public int getLimitPerRequest() {
        return limitPerRequest;
    }

    public void setLimitPerRequest(final int limitPerRequest) {
        this.limitPerRequest = limitPerRequest;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void addQueryParameter(final String key, final String value) {
        queryParameters.put(key, value);
    }

    public void addQueryParameters(final Map<String, String> parameters) {
        queryParameters.putAll(parameters);
    }

    public Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }

    public void addAdditionalHeader(final String key, final String value) {
        additionalHeaders.put(key, value);
    }

    public void addAdditionalHeaders(final Map<String, String> parameters) {
        additionalHeaders.putAll(parameters);
    }

}
