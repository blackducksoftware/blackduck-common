/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.dataservice.phonehome;

import java.net.URL;

import com.blackducksoftware.integration.hub.api.nonpublic.HubRegistrationRequestService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phone.home.PhoneHomeClient;
import com.blackducksoftware.integration.phone.home.enums.BlackDuckName;
import com.blackducksoftware.integration.phone.home.exception.PhoneHomeArgumentException;
import com.blackducksoftware.integration.phone.home.exception.PhoneHomeConnectionException;

public class PhoneHomeDataService extends HubRequestService {
    private final IntLogger logger;

    private final HubRegistrationRequestService hubRegistrationRequestService;

    public PhoneHomeDataService(final IntLogger logger, RestConnection restConnection, HubRegistrationRequestService hubRegistrationRequestService) {
        super(restConnection);
        this.logger = logger;
        this.hubRegistrationRequestService = hubRegistrationRequestService;
    }

    public void phoneHome(HubServerConfig hubServerConfig, HubScanConfig hubScanConfig, String hubVersion) {
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
            final PhoneHomeClient phClient = new PhoneHomeClient(logger);
            phClient.setProxyProperties(hubServerConfig.getProxyInfo().getHost(), hubServerConfig.getProxyInfo().getPort(),
                    hubServerConfig.getProxyInfo().getUsername(), hubServerConfig.getProxyInfo().getDecryptedPassword(),
                    hubServerConfig.getProxyInfo().getIgnoredProxyHosts());
            phClient.callHomeIntegrations(registrationId, hubHostName, BlackDuckName.HUB, hubVersion, hubScanConfig.getThirdPartyName(),
                    hubScanConfig.getThirdPartyVersion(), hubScanConfig.getPluginVersion());
        } catch (final PhoneHomeArgumentException e) {
            logger.debug(e.getMessage(), e);
        } catch (final PhoneHomeConnectionException e) {
            logger.debug("Problem with phone-home connection : " + e.getMessage(), e);
        } catch (final Exception e) {
            logger.debug("Problem with phone-home : " + e.getMessage(), e);
        }
    }

}
