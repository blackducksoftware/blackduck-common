/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.api.core.response.UrlSingleResponse;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.response.CurrentVersionView;
import com.blackduck.integration.blackduck.api.generated.view.RegistrationView;
import com.blackduck.integration.blackduck.exception.BlackDuckApiException;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.blackduck.service.model.BlackDuckServerData;
import com.blackduck.integration.blackduck.service.request.BlackDuckSingleRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.exception.IntegrationRestException;

public class BlackDuckRegistrationService extends DataService {
    private final UrlSingleResponse<RegistrationView> registrationResponse = apiDiscovery.metaRegistrationLink();
    private final UrlSingleResponse<CurrentVersionView> currentVersionResponse = apiDiscovery.metaCurrentVersionLink();
    private final HttpUrl blackDuckUrl;
    private static final ThreadLocal<Boolean> isRegistrationIdFetchAllowed = ThreadLocal.withInitial(() -> true);

    public BlackDuckRegistrationService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger, HttpUrl blackDuckUrl) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.blackDuckUrl = blackDuckUrl;
    }

    public String getRegistrationId() throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
            .commonGet()
            .acceptMimeType("application/vnd.blackducksoftware.status-4+json");

        BlackDuckSingleRequest<RegistrationView> requestSingle = blackDuckRequestBuilder.buildBlackDuckRequest(registrationResponse);
        RegistrationView registrationView = blackDuckApiClient.getResponse(requestSingle);

        return registrationView.getRegistrationId();
    }

    public BlackDuckServerData getBlackDuckServerData() throws IntegrationException {
        CurrentVersionView currentVersionView = blackDuckApiClient.getResponse(currentVersionResponse);
        String registrationId = null;
        try {
            if (isRegistrationIdFetchAllowed()) {
                // We need to wrap this because this will most likely fail unless they are running as an admin
                registrationId = getRegistrationId();
            }
        } catch (IntegrationException e) {
            handleForbiddenExceptionAndSetFlag(e);
        }
        return new BlackDuckServerData(blackDuckUrl, currentVersionView.getVersion(), registrationId);
    }

    private boolean isRegistrationIdFetchAllowed() {
        return isRegistrationIdFetchAllowed.get();
    }

    private void setRegistrationIdFetchAllowed(boolean allowed) {
        isRegistrationIdFetchAllowed.set(allowed);
    }

    private void handleForbiddenExceptionAndSetFlag(IntegrationException e) {
        if (e instanceof BlackDuckApiException) {
            BlackDuckApiException apiException = (BlackDuckApiException) e;
            if (apiException.getBlackDuckErrorCode().contains("core.rest.unauthorized")) {
                logger.warn("Failed to fetch registration id. The current user is not authorized.");
                setRegistrationIdFetchAllowed(false);
            }
        }
    }
}