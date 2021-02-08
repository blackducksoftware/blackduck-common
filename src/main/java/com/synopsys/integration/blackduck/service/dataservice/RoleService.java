/*
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
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.RoleView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFilter;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class RoleService extends DataService {
    public static final String SERVER_SCOPE = "server";
    public static final String PROJECT_SCOPE = "project";

    public RoleService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public List<RoleView> getServerRoles() throws IntegrationException {
        BlackDuckRequestFilter scope = createScopeFilter(SERVER_SCOPE);

        return getScopedRoles(scope);
    }

    public List<RoleView> getProjectRoles() throws IntegrationException {
        BlackDuckRequestFilter scope = createScopeFilter(PROJECT_SCOPE);

        return getScopedRoles(scope);
    }

    private List<RoleView> getScopedRoles(BlackDuckRequestFilter scope) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder();
        blackDuckRequestBuilder.addBlackDuckFilter(scope);

        return blackDuckApiClient.getAllResponses(ApiDiscovery.ROLES_LINK_RESPONSE, blackDuckRequestBuilder);
    }

    private BlackDuckRequestFilter createScopeFilter(String scope) {
        return BlackDuckRequestFilter.createFilterWithSingleValue("scope", scope);
    }

}
