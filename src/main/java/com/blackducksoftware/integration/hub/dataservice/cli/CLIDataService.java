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
package com.blackducksoftware.integration.hub.dataservice.cli;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.json.JSONException;
import org.restlet.resource.ResourceException;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.api.nonpublic.HubRegistrationRequestService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phone.home.PhoneHomeClient;
import com.blackducksoftware.integration.phone.home.enums.BlackDuckName;
import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;
import com.blackducksoftware.integration.phone.home.exception.PhoneHomeException;
import com.blackducksoftware.integration.phone.home.exception.PropertiesLoaderException;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class CLIDataService extends HubRequestService {
    private final IntLogger logger;

    private final HubVersionRequestService hubVersionRequestService;

    private final CLIDownloadService cliDownloadService;

    private final HubRegistrationRequestService hubRegistrationRequestService;

    public CLIDataService(final IntLogger logger, final RestConnection restConnection, final HubVersionRequestService hubVersionRequestService,
            CLIDownloadService cliDownloadService, HubRegistrationRequestService hubRegistrationRequestService) {
        super(restConnection);
        this.logger = logger;
        this.hubVersionRequestService = hubVersionRequestService;
        this.cliDownloadService = cliDownloadService;
        this.hubRegistrationRequestService = hubRegistrationRequestService;
    }

    public void installCLI(final CIEnvironmentVariables commonEnvVars, final HubServerConfig hubConfig, final File toolsDir, String thirdPartyVersion,
            String pluginVersion) throws IOException, ResourceDoesNotExistException, URISyntaxException, BDRestException, IllegalArgumentException,
            InterruptedException, HubIntegrationException, EncryptionException {
        hubConfig.print(logger);
        final String localHostName = HostnameHelper.getMyHostname();
        logger.info("Running on machine : " + localHostName);
        String hubVersion = hubVersionRequestService.getHubVersion();

        cliDownloadService.performInstallation(hubConfig.getProxyInfo(), toolsDir, commonEnvVars, hubConfig.getHubUrl().toString(),
                hubVersion, localHostName);

        // Phone-Home
        try {
            String regId = null;
            String hubHostName = null;
            try {
                regId = hubRegistrationRequestService.getRegistrationId();
            } catch (final Exception e) {
                logger.debug("Could not get the Hub registration Id.");
            }
            try {
                final URL url = hubConfig.getHubUrl();
                hubHostName = url.getHost();
            } catch (final Exception e) {
                logger.debug("Could not get the Hub Host name.");
            }
            bdPhoneHome(logger, hubVersion, regId, hubHostName, thirdPartyVersion, pluginVersion);
        } catch (final Exception e) {
            logger.debug("Unable to phone-home", e);
        }
    }

    public void bdPhoneHome(final IntLogger logger, final String blackDuckVersion, final String regId,
            final String hubHostName, String thirdPartyVersion, String pluginVersion)
            throws IOException, PhoneHomeException, PropertiesLoaderException, ResourceException, JSONException {

        final PhoneHomeClient phClient = new PhoneHomeClient();
        phClient.callHomeIntegrations(regId, hubHostName, BlackDuckName.HUB, blackDuckVersion, ThirdPartyName.BAMBOO,
                thirdPartyVersion, pluginVersion);
    }
}
