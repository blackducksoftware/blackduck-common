/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
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
