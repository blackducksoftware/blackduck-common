/**
 * blackduck-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.blackduck.phonehome;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.CurrentVersionView;
import com.synopsys.integration.blackduck.service.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.phonehome.PhoneHomeClient;
import com.synopsys.integration.phonehome.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.PhoneHomeResponse;
import com.synopsys.integration.phonehome.PhoneHomeService;
import com.synopsys.integration.phonehome.enums.ProductIdEnum;
import com.synopsys.integration.phonehome.google.analytics.GoogleAnalyticsConstants;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class BlackDuckPhoneHomeHelper {
    private final IntLogger logger;
    private final BlackDuckService blackDuckService;
    private final PhoneHomeService phoneHomeService;
    private final BlackDuckRegistrationService blackDuckRegistrationService;
    private final IntEnvironmentVariables intEnvironmentVariables;

    public static BlackDuckPhoneHomeHelper createPhoneHomeHelper(final BlackDuckServicesFactory blackDuckServicesFactory) {
        return createAsynchronousPhoneHomeHelper(blackDuckServicesFactory, null);
    }

    public static BlackDuckPhoneHomeHelper createAsynchronousPhoneHomeHelper(final BlackDuckServicesFactory blackDuckServicesFactory, final ExecutorService executorService) {
        final IntLogger intLogger = blackDuckServicesFactory.getLogger();
        final PhoneHomeService intPhoneHomeService;
        if (executorService != null) {
            intPhoneHomeService = PhoneHomeService.createAsynchronousPhoneHomeService(intLogger, createPhoneHomeClient(intLogger, blackDuckServicesFactory.getRestConnection(), blackDuckServicesFactory.getGson()), executorService);
        } else {
            intPhoneHomeService = PhoneHomeService.createPhoneHomeService(intLogger, createPhoneHomeClient(intLogger, blackDuckServicesFactory.getRestConnection(), blackDuckServicesFactory.getGson()));
        }
        return new BlackDuckPhoneHomeHelper(intLogger, blackDuckServicesFactory.createBlackDuckService(), intPhoneHomeService, blackDuckServicesFactory.createBlackDuckRegistrationService(), blackDuckServicesFactory.getEnvironmentVariables());
    }

    public static PhoneHomeClient createPhoneHomeClient(final IntLogger intLogger, final RestConnection restConnection, final Gson gson) {
        final String googleAnalyticsTrackingId = GoogleAnalyticsConstants.PRODUCTION_INTEGRATIONS_TRACKING_ID;
        final HttpClientBuilder httpClientBuilder = restConnection.getClientBuilder();
        return new PhoneHomeClient(googleAnalyticsTrackingId, intLogger, httpClientBuilder, gson);
    }

    public BlackDuckPhoneHomeHelper(final IntLogger logger, final BlackDuckService blackDuckService, final PhoneHomeService phoneHomeService, final BlackDuckRegistrationService blackDuckRegistrationService,
            final IntEnvironmentVariables intEnvironmentVariables) {
        this.logger = logger;
        this.blackDuckService = blackDuckService;
        this.phoneHomeService = phoneHomeService;
        this.blackDuckRegistrationService = blackDuckRegistrationService;
        this.intEnvironmentVariables = intEnvironmentVariables;
    }

    public PhoneHomeResponse handlePhoneHome(final String integrationRepoName, final String integrationVersion) {
        return handlePhoneHome(integrationRepoName, integrationVersion, Collections.emptyMap());
    }

    public PhoneHomeResponse handlePhoneHome(final String integrationRepoName, final String integrationVersion, final Map<String, String> metaData) {
        try {
            final PhoneHomeRequestBody phoneHomeRequestBody = createPhoneHomeRequestBody(integrationRepoName, integrationVersion, metaData);
            return phoneHomeService.phoneHome(phoneHomeRequestBody, getEnvironmentVariables());
        } catch (final Exception e) {
            logger.debug("Problem phoning home: " + e.getMessage(), e);
        }
        return PhoneHomeResponse.createResponse(Boolean.FALSE);
    }

    private PhoneHomeRequestBody createPhoneHomeRequestBody(final String integrationRepoName, final String integrationVersion, final Map<String, String> metaData) {
        final BlackDuckPhoneHomeRequestBuilder blackDuckBuilder = new BlackDuckPhoneHomeRequestBuilder();
        blackDuckBuilder.setIntegrationRepoName(integrationRepoName);
        blackDuckBuilder.setIntegrationVersion(integrationVersion);

        blackDuckBuilder.setProduct(ProductIdEnum.BLACK_DUCK);
        blackDuckBuilder.setProductVersion(getProductVersion());

        blackDuckBuilder.setRegistrationKey(getRegistrationKey());
        blackDuckBuilder.setCustomerDomainName(getHostName());

        final PhoneHomeRequestBody.Builder actualBuilder = blackDuckBuilder.getBuilder();
        final boolean metaDataSuccess = actualBuilder.addAllToMetaData(metaData);
        if (!metaDataSuccess) {
            logger.debug("The metadata provided to phone-home exceeded its size limit. At least some metadata will be missing.");
        }

        return actualBuilder.build();
    }

    private Map<String, String> getEnvironmentVariables() {
        if (intEnvironmentVariables != null) {
            return intEnvironmentVariables.getVariables();
        }
        return Collections.emptyMap();
    }

    private String getProductVersion() {
        final CurrentVersionView currentVersion;
        try {
            currentVersion = blackDuckService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
            return currentVersion.getVersion();
        } catch (final IntegrationException e) {
        }
        return PhoneHomeRequestBody.Builder.UNKNOWN_ID;
    }

    private String getHostName() {
        return blackDuckService.getHubBaseUrl().toString();
    }

    private String getRegistrationKey() {
        String registrationId = null;
        try {
            // We need to wrap this because this will most likely fail unless they are running as an admin
            registrationId = blackDuckRegistrationService.getRegistrationId();
        } catch (final IntegrationException e) {
        }
        // We must check if the reg id is blank because of an edge case in which the hub can authenticate (while the webserver is coming up) without registration
        if (StringUtils.isBlank(registrationId)) {
            registrationId = PhoneHomeRequestBody.Builder.UNKNOWN_ID;
        }
        return registrationId;
    }
}
