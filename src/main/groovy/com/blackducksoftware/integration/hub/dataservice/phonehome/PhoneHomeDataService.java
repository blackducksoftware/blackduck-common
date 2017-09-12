/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.dataservice.phonehome;

import java.net.URL;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.nonpublic.HubRegistrationRequestService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phonehome.PhoneHomeClient;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBodyBuilder;
import com.blackducksoftware.integration.phonehome.enums.BlackDuckName;
import com.blackducksoftware.integration.phonehome.enums.PhoneHomeSource;
import com.blackducksoftware.integration.phonehome.enums.ThirdPartyName;

public class PhoneHomeDataService {
    private final IntLogger logger;
    private final HubRegistrationRequestService hubRegistrationRequestService;
    private final HubVersionRequestService hubVersionRequestService;
    private final PhoneHomeClient phoneHomeClient;

    public PhoneHomeDataService(final IntLogger logger, final PhoneHomeClient phoneHomeClient, final HubRegistrationRequestService hubRegistrationRequestService, final HubVersionRequestService hubVersionRequestService) {
        this.logger = logger;
        this.hubRegistrationRequestService = hubRegistrationRequestService;
        this.hubVersionRequestService = hubVersionRequestService;
        this.phoneHomeClient = phoneHomeClient;
    }

    public void phoneHome(final ThirdPartyName thirdPartyName, final String thirdPartyVersion, final String pluginVersion) {
        phoneHome(thirdPartyName.getName(), thirdPartyVersion, pluginVersion);
    }

    public void phoneHome(final String thirdPartyName, final String thirdPartyVersion, final String pluginVersion) {
        final PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = createInitialPhoneHomeRequestBodyBuilder(thirdPartyName, thirdPartyVersion, pluginVersion);
        phoneHome(phoneHomeRequestBodyBuilder);
    }

    public void phoneHome(final PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder) {
        try {
            final PhoneHomeRequestBody phoneHomeRequestBody = phoneHomeRequestBodyBuilder.build();
            phoneHome(phoneHomeRequestBody);
        } catch (final Exception e) {
            logger.debug("Could not build phone home body" + e.getMessage(), e);
        }
    }

    public void phoneHome(final PhoneHomeRequestBody phoneHomeRequestBody) {
        if (phoneHomeRequestBody == PhoneHomeRequestBody.DO_NOT_PHONE_HOME) {
            logger.debug("Skipping phone-home");
        } else {
            try {
                phoneHomeClient.postPhoneHomeRequest(phoneHomeRequestBody);
            } catch (final Exception e) {
                logger.debug("Problem with phone-home : " + e.getMessage(), e);
            }
        }
    }

    public PhoneHomeRequestBodyBuilder createInitialPhoneHomeRequestBodyBuilder(final ThirdPartyName thirdPartyName, final String thirdPartyVersion, final String pluginVersion) {
        return createInitialPhoneHomeRequestBodyBuilder(thirdPartyName.getName(), thirdPartyVersion, pluginVersion);
    }

    public PhoneHomeRequestBodyBuilder createInitialPhoneHomeRequestBodyBuilder(final String thirdPartyName, final String thirdPartyVersion, final String pluginVersion) {
        final PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = createInitialPhoneHomeRequestBodyBuilder();
        phoneHomeRequestBodyBuilder.setThirdPartyName(thirdPartyName);
        phoneHomeRequestBodyBuilder.setThirdPartyVersion(thirdPartyVersion);
        phoneHomeRequestBodyBuilder.setPluginVersion(pluginVersion);
        return phoneHomeRequestBodyBuilder;
    }

    public PhoneHomeRequestBodyBuilder createInitialPhoneHomeRequestBodyBuilder() {
        final PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = new PhoneHomeRequestBodyBuilder();
        try {
            final String hubVersion = hubVersionRequestService.getHubVersion();
            String registrationId = null;
            try {
                // We need to wrap this because this will most likely fail unless they are running as an admin
                registrationId = hubRegistrationRequestService.getRegistrationId();
            } catch (final IntegrationException e) {
            }
            final URL hubHostName = hubRegistrationRequestService.getHubBaseUrl();
            phoneHomeRequestBodyBuilder.setRegistrationId(registrationId);
            phoneHomeRequestBodyBuilder.setHostName(hubHostName.toString());
            phoneHomeRequestBodyBuilder.setBlackDuckName(BlackDuckName.HUB);
            phoneHomeRequestBodyBuilder.setBlackDuckVersion(hubVersion);
            phoneHomeRequestBodyBuilder.setSource(PhoneHomeSource.INTEGRATIONS);
        } catch (final Exception e) {
            logger.debug("Couldn't detail phone home request builder: " + e.getMessage());
        }
        return phoneHomeRequestBodyBuilder;
    }

}
