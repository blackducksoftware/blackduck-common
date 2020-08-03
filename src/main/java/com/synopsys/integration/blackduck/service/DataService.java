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

import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.log.IntLogger;

public class DataService {
    protected final BlackDuckService blackDuckService;
    protected final RequestFactory requestFactory;
    protected final IntLogger logger;

    public DataService(BlackDuckService blackDuckService, RequestFactory requestFactory, IntLogger logger) {
        this.blackDuckService = blackDuckService;
        this.requestFactory = requestFactory;
        this.logger = logger;
    }

}
