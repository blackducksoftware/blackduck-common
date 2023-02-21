/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;

import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.RoleView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFilter;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class RoleService extends DataService {
    public static final String SERVER_SCOPE = "server";
    public static final String PROJECT_SCOPE = "project";

    private final UrlMultipleResponses<RoleView> rolesResponses = apiDiscovery.metaRolesLink();

    public RoleService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, logger);
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
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .addBlackDuckFilter(scope);
        BlackDuckMultipleRequest<RoleView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(rolesResponses);

        return blackDuckApiClient.getAllResponses(requestMultiple);
    }

    private BlackDuckRequestFilter createScopeFilter(String scope) {
        return BlackDuckRequestFilter.createFilterWithSingleValue("scope", scope);
    }

}
