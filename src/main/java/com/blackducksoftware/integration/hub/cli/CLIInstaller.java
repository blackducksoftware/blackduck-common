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
package com.blackducksoftware.integration.hub.cli;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

/**
 * @deprecated
 *             Please use the CLIDownloadService as provided by the DataServicesFactory
 */
@Deprecated
public class CLIInstaller {
    private HubProxyInfo hubProxyInfo;

    private CLILocation cliLocation;

    private CIEnvironmentVariables ciEnvironmentVariables;

    public CLIInstaller(HubProxyInfo hubProxyInfo, final CLILocation cliLocation, final CIEnvironmentVariables ciEnvironmentVariables) {
        this.hubProxyInfo = hubProxyInfo;
        this.cliLocation = cliLocation;
        this.ciEnvironmentVariables = ciEnvironmentVariables;
    }

    public void performInstallation(final IntLogger logger, String hubUrl, String hubVersion, final String localHostName)
            throws IOException, InterruptedException, BDRestException, URISyntaxException, HubIntegrationException, IllegalArgumentException,
            EncryptionException {
        CLIDownloadService cliDownloadService = new CLIDownloadService(logger);
        cliDownloadService.performInstallation(hubProxyInfo, cliLocation, ciEnvironmentVariables, hubUrl, hubVersion, localHostName);
    }

    public void customInstall(final URL archive, String hubVersion, final String localHostName, final IntLogger logger)
            throws IOException, InterruptedException, HubIntegrationException, IllegalArgumentException, EncryptionException {
        CLIDownloadService cliDownloadService = new CLIDownloadService(logger);
        cliDownloadService.customInstall(hubProxyInfo, cliLocation, ciEnvironmentVariables, archive, hubVersion, localHostName);
    }

}
