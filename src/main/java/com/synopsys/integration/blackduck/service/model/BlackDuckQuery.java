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
package com.synopsys.integration.blackduck.service.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class BlackDuckQuery {
    private final String q;

    public static Optional<BlackDuckQuery> createQuery(final String parameter) {
        if (StringUtils.isNotBlank(parameter)) {
            return Optional.of(new BlackDuckQuery(parameter));
        }

        return Optional.empty();
    }

    public static Optional<BlackDuckQuery> createQuery(final String prefix, final String parameter) {
        if (StringUtils.isNotBlank(parameter)) {
            return Optional.of(new BlackDuckQuery(prefix + ":" + parameter));
        }

        return Optional.empty();
    }

    private BlackDuckQuery(final String parameter) {
        q = parameter;
    }

    public String getParameter() {
        return q;
    }

}
