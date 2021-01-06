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
package com.synopsys.integration.blackduck.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Some requests to Black Duck can be filtered. There can be one more more filter keys each with one or more values.
 *
 * If multiple filters values are combined for the same filter key, these values will be OR'ed together. If multiple filter values are combined for different filter keys, these values will be AND'ed together.
 */
public class BlackDuckRequestFilter {
    private final Map<String, Set<String>> filterKeysToValues = new HashMap<>();

    public static BlackDuckRequestFilter createFilterWithMultipleValues(String key, List<String> values) {
        BlackDuckRequestFilter blackDuckRequestFilter = new BlackDuckRequestFilter();
        blackDuckRequestFilter.addFilter(key, values);
        return blackDuckRequestFilter;
    }

    public static BlackDuckRequestFilter createFilterWithSingleValue(String key, String value) {
        BlackDuckRequestFilter blackDuckRequestFilter = new BlackDuckRequestFilter();
        blackDuckRequestFilter.addFilter(key, value);
        return blackDuckRequestFilter;
    }

    public void addFilter(String key, String value) {
        filterKeysToValues.computeIfAbsent(key, k -> new HashSet<>()).add(value);
    }

    public void addFilter(String key, List<String> values) {
        filterKeysToValues.computeIfAbsent(key, k -> new HashSet<>()).addAll(values);
    }

    /**
     * This will return the filter key/value pairs as Black Duck expects them: [key1:value1,key1:value2,key2:value3] etc
     */
    public List<String> getFilterParameters() {
        List<String> parameters = new ArrayList<>();
        filterKeysToValues.forEach((filterKey, filterValues) -> {
            filterValues.forEach(filterValue -> {
                String parameterString = String.format("%s:%s", filterKey, filterValue);
                parameters.add(parameterString);
            });
        });

        return parameters;
    }

}
