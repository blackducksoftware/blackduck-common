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
package com.synopsys.integration.blackduck.useragent;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.util.NameVersion;

public class UserAgentItem {
    public static final String UNKNOWN = "unknown";

    private final NameVersion product;
    private final String comments;

    public UserAgentItem(final NameVersion product, final String comments) {
        this.product = product;
        this.comments = comments;
    }

    public UserAgentItem(final NameVersion product) {
        this(product, null);
    }

    public NameVersion getProduct() {
        return product;
    }

    public Optional<String> getComments() {
        return Optional.ofNullable(comments);
    }

    public String createUserAgentString() {
        StringBuilder userAgent = new StringBuilder();
        userAgent.append(strip(clean(product.getName())));
        userAgent.append("/");
        userAgent.append(strip(clean(product.getVersion())));

        String cleaned = clean(comments);
        if (!UNKNOWN.equals(cleaned)) {
            userAgent.append(" (");
            userAgent.append(cleaned);
            userAgent.append(")");
        }

        return userAgent.toString();
    }

    private String strip(String original) {
        return original.replace(" ", "");
    }

    private String clean(String original) {
        String value = StringUtils.trimToEmpty(original);
        if (!value.isEmpty()) {
            return value;
        } else {
            return UNKNOWN;
        }
    }

}
