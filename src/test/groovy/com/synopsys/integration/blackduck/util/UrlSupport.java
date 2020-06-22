/**
 * integration-rest
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
package com.synopsys.integration.blackduck.util;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlSupport {
    public HttpUrl appendRelativeUrl(String baseUrl, String relativeUrl) throws IntegrationException {
        try {
            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }
            URL baseURL = new URL(baseUrl);
            if (relativeUrl.startsWith("/")) {
                relativeUrl = relativeUrl.substring(1);
            }

            return new HttpUrl(new URL(baseURL, relativeUrl).toString());
        } catch (MalformedURLException e) {
            throw new IntegrationException(String.format("Error appending the relative url (%s) to base url (%s): %s", relativeUrl, baseUrl, e.getMessage()), e);
        }
    }

    public HttpUrl appendRelativeUrl(HttpUrl baseUrl, String relativeUrl) throws IntegrationException {
        return appendRelativeUrl(baseUrl.string(), relativeUrl);
    }

}
