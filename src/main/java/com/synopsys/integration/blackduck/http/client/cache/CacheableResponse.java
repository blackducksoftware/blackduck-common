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
package com.synopsys.integration.blackduck.http.client.cache;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class CacheableResponse implements Response {
    private final Request request;
    private final Response response;
    private final LRUMap<Request, Response> cache;

    private String stringResponse;

    public CacheableResponse(Request request, Response response, LRUMap<Request, Response> cache) {
        this.request = request;
        this.response = response;
        this.cache = cache;
    }

    @Override
    public HttpUriRequest getRequest() {
        return response.getRequest();
    }

    @Override
    public int getStatusCode() {
        return response.getStatusCode();
    }

    @Override
    public boolean isStatusCodeSuccess() {
        return response.isStatusCodeSuccess();
    }

    @Override
    public boolean isStatusCodeError() {
        return response.isStatusCodeError();
    }

    @Override
    public String getStatusMessage() {
        return response.getStatusMessage();
    }

    @Override
    public InputStream getContent() throws IntegrationException {
        return response.getContent();
    }

    @Override
    public String getContentString() throws IntegrationException {
        if (null != stringResponse) {
            return stringResponse;
        }

        stringResponse = response.getContentString();
        cache.put(request, this);
        return stringResponse;
    }

    @Override
    public String getContentString(Charset encoding) throws IntegrationException {
        if (null != stringResponse) {
            return stringResponse;
        }

        stringResponse = response.getContentString(encoding);
        cache.put(request, this);
        return stringResponse;
    }

    @Override
    public Long getContentLength() {
        return response.getContentLength();
    }

    @Override
    public String getContentEncoding() {
        return response.getContentEncoding();
    }

    @Override
    public String getContentType() {
        return response.getContentType();
    }

    @Override
    public Map<String, String> getHeaders() {
        return response.getHeaders();
    }

    @Override
    public String getHeaderValue(String name) {
        return response.getHeaderValue(name);
    }

    @Override
    public CloseableHttpResponse getActualResponse() {
        return response.getActualResponse();
    }

    @Override
    public void close() throws IOException {
        response.close();
    }

    @Override
    public long getLastModified() throws IntegrationException {
        return response.getLastModified();
    }

    @Override
    public void throwExceptionForError() throws IntegrationRestException {
        response.throwExceptionForError();
    }
}
