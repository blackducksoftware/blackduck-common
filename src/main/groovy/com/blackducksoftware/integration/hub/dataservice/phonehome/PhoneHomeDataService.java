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

import com.blackducksoftware.integration.hub.api.nonpublic.HubRegistrationRequestService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phone.home.IntegrationInfo;
import com.blackducksoftware.integration.phone.home.PhoneHomeClient;
import com.blackducksoftware.integration.phone.home.PhoneHomeRequest;
import com.blackducksoftware.integration.phone.home.PhoneHomeRequestBuilder;
import com.blackducksoftware.integration.phone.home.enums.BlackDuckName;
import com.blackducksoftware.integration.phone.home.enums.PhoneHomeSource;
import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;

public class PhoneHomeDataService extends HubResponseService {
    private final IntLogger logger;

    private final HubRegistrationRequestService hubRegistrationRequestService;

    private final PhoneHomeClient et;

    public PhoneHomeDataService(final IntLogger logger, final RestConnection restConnection, final PhoneHomeClient phoneHomeClient,
            final HubRegistrationRequestService hubRegistrationRequestService) {
        super(restConnection);
        this.logger = logger;
        this.hubRegistrationRequestService = hubRegistrationRequestService;
        this.et = phoneHomeClient;
    }

    public void phoneHome(final HubServerConfig hubServerConfig, final String hubVersion, final IntegrationInfo integrationInfo) {
        if (IntegrationInfo.DO_NOT_PHONE_HOME == integrationInfo) {
            logger.debug("Skipping phone-home");
        } else {
            phoneHome(hubServerConfig, hubVersion, integrationInfo.getThirdPartyName(), integrationInfo.getThirdPartyVersion(),
                    integrationInfo.getPluginVersion());
        }
    }

    private void phoneHome(final HubServerConfig hubServerConfig, final String hubVersion, final ThirdPartyName thirdPartyName, final String thirdPartyVersion,
            final String pluginVersion) {
        try {
            String registrationId = null;
            try {
                registrationId = hubRegistrationRequestService.getRegistrationId();
            } catch (final Exception e) {
                logger.debug("Could not get the Hub registration Id.");
            }

            String hubHostName = null;
            try {
                final URL url = hubServerConfig.getHubUrl();
                hubHostName = url.getHost();
            } catch (final Exception e) {
                logger.debug("Could not get the Hub Host name.");
            }
            et.setTimeout(hubServerConfig.getTimeout());
            et.setProxyInfo(hubServerConfig.getProxyInfo().getHost(), hubServerConfig.getProxyInfo().getPort(),
                    hubServerConfig.getProxyInfo().getUsername(), hubServerConfig.getProxyInfo().getDecryptedPassword(),
                    hubServerConfig.getProxyInfo().getIgnoredProxyHosts());
            final PhoneHomeRequestBuilder requestBuilder = new PhoneHomeRequestBuilder();
            requestBuilder.setRegistrationId(registrationId);
            requestBuilder.setHostName(hubHostName);
            requestBuilder.setBlackDuckName(BlackDuckName.HUB);
            requestBuilder.setBlackDuckVersion(hubVersion);
            requestBuilder.setThirdPartyName(thirdPartyName);
            requestBuilder.setThirdPartyVersion(thirdPartyVersion);
            requestBuilder.setPluginVersion(pluginVersion);
            requestBuilder.setSource(PhoneHomeSource.INTEGRATIONS);
            final PhoneHomeRequest phoneHomeRequest = requestBuilder.build();
            et.phoneHome(phoneHomeRequest);
        } catch (final Exception e) {
            logger.debug("Problem with phone-home : " + e.getMessage(), e);
        }
    }

}
