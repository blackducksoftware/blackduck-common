/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.CurrentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.RegistrationView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.BlackDuckServerData;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckRegistrationService extends DataService {
    private HttpUrl blackDuckUrl;

    public BlackDuckRegistrationService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory, IntLogger logger, HttpUrl blackDuckUrl) {
        super(blackDuckApiClient, apiDiscovery, blackDuckRequestBuilderFactory, logger);
        this.blackDuckUrl = blackDuckUrl;
    }

    public String getRegistrationId() throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestBuilderFactory.createCommonGetRequestBuilder();
        requestBuilder.acceptMimeType("application/vnd.blackducksoftware.status-4+json");

        UrlSingleResponse<RegistrationView> registrationResponse = apiDiscovery.metaSingleResponse(ApiDiscovery.REGISTRATION_PATH);
        RegistrationView registrationView = blackDuckApiClient.getResponse(registrationResponse, requestBuilder);
        return registrationView.getRegistrationId();
    }

    public BlackDuckServerData getBlackDuckServerData() throws IntegrationException {
        UrlSingleResponse<CurrentVersionView> currentVersionResponse = apiDiscovery.metaSingleResponse(ApiDiscovery.CURRENT_VERSION_PATH);
        CurrentVersionView currentVersionView = blackDuckApiClient.getResponse(currentVersionResponse);
        String registrationId = null;
        try {
            // We need to wrap this because this will most likely fail unless they are running as an admin
            registrationId = getRegistrationId();
        } catch (IntegrationException e) {
        }
        return new BlackDuckServerData(blackDuckUrl, currentVersionView.getVersion(), registrationId);
    }

}
