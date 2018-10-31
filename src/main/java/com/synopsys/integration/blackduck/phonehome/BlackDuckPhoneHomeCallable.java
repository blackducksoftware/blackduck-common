/**
 * hub-common
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

import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.CurrentVersionView;
import com.synopsys.integration.blackduck.service.HubRegistrationService;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.phonehome.PhoneHomeCallable;
import com.synopsys.integration.phonehome.PhoneHomeClient;
import com.synopsys.integration.phonehome.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.enums.ProductIdEnum;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class BlackDuckPhoneHomeCallable extends PhoneHomeCallable {
    private final IntLogger logger;
    private final HubService hubService;
    private final HubRegistrationService hubRegistrationService;
    private final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder;

    public BlackDuckPhoneHomeCallable(final IntLogger logger, final PhoneHomeClient client, final URL productURL, final String artifactId, final String artifactVersion, final IntEnvironmentVariables intEnvironmentVariables,
            final HubService hubService, final HubRegistrationService hubRegistrationService) {
        super(logger, client, productURL, artifactId, artifactVersion, intEnvironmentVariables);
        this.logger = logger;
        this.hubService = hubService;
        this.hubRegistrationService = hubRegistrationService;
        phoneHomeRequestBodyBuilder = new PhoneHomeRequestBody.Builder();
    }

    public BlackDuckPhoneHomeCallable(final IntLogger logger, final PhoneHomeClient client, final URL productURL, final String artifactId, final String artifactVersion, final IntEnvironmentVariables intEnvironmentVariables,
            final HubService hubService, final HubRegistrationService hubRegistrationService, final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder) {
        super(logger, client, productURL, artifactId, artifactVersion, intEnvironmentVariables);
        this.logger = logger;
        this.hubService = hubService;
        this.hubRegistrationService = hubRegistrationService;
        this.phoneHomeRequestBodyBuilder = phoneHomeRequestBodyBuilder;
    }

    @Override
    public PhoneHomeRequestBody.Builder createPhoneHomeRequestBodyBuilder() {
        try {
            final CurrentVersionView currentVersion = hubService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
            String registrationId = null;
            try {
                // We need to wrap this because this will most likely fail unless they are running as an admin
                registrationId = hubRegistrationService.getRegistrationId();
            } catch (final IntegrationException e) {
            }
            // We must check if the reg id is blank because of an edge case in which the hub can authenticate (while the webserver is coming up) without registration
            if (StringUtils.isBlank(registrationId)) {
                registrationId = PhoneHomeRequestBody.Builder.UNKNOWN_ID;
            }
            phoneHomeRequestBodyBuilder.setCustomerId(registrationId);
            phoneHomeRequestBodyBuilder.setProductId(ProductIdEnum.HUB);
            phoneHomeRequestBodyBuilder.setProductVersion(currentVersion.version);
        } catch (final Exception e) {
            logger.debug("Couldn't detail phone home request builder: " + e.getMessage());
        }
        return phoneHomeRequestBodyBuilder;
    }

}
