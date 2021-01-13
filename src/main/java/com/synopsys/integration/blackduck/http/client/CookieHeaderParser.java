/**
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.http.client;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;

public class CookieHeaderParser {
    public static final String SET_COOKIE = "SET-COOKIE";
    public static final String AUTHORIZATION_BEARER_PREFIX = "AUTHORIZATION_BEARER=";
    public static final String HEADER_VALUE_SEPARATOR = ";";

    public Optional<String> parseBearerToken(Header[] allHeaders) {
        return Arrays
                   .stream(allHeaders)
                   // https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
                   // header names are case insensitive
                   .filter(header -> SET_COOKIE.equalsIgnoreCase(header.getName()))
                   .filter(header -> header.getValue().contains(AUTHORIZATION_BEARER_PREFIX))
                   .findFirst()
                   .map(header -> getToken(header.getValue()));
    }

    private String getToken(String headerValue) {
        if (headerValue.contains(HEADER_VALUE_SEPARATOR)) {
            return StringUtils.substringBetween(headerValue, AUTHORIZATION_BEARER_PREFIX, HEADER_VALUE_SEPARATOR);
        } else {
            return StringUtils.substringAfter(headerValue, AUTHORIZATION_BEARER_PREFIX);
        }
    }

}
