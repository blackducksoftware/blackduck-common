/**
 * blackduck-common
 * <p>
 * Copyright (c) 2020 Synopsys, Inc.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.http.client;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;

import java.net.URI;

public class BlackDuckRedirectStrategy implements RedirectStrategy {
    private LaxRedirectStrategy baseRedirectStrategy = new LaxRedirectStrategy();

    @Override
    public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws ProtocolException {
        int status = httpResponse.getStatusLine().getStatusCode();
        if (status == 308) {
            return true;
        } else {
            return baseRedirectStrategy.isRedirected(httpRequest, httpResponse, httpContext);
        }
    }

    @Override
    public HttpUriRequest getRedirect(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws ProtocolException {
        int status = httpResponse.getStatusLine().getStatusCode();
        if (status == 308) {
            URI uri = baseRedirectStrategy.getLocationURI(httpRequest, httpResponse, httpContext);
            return RequestBuilder.copy(httpRequest).setUri(uri).build();
        } else {
            return baseRedirectStrategy.getRedirect(httpRequest, httpResponse, httpContext);
        }
    }

}
