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

import com.blackducksoftware.integration.hub.cli.SimpleScanService.Result;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class CLIDataService extends HubRequestService {
    private final IntLogger logger;

    public CLIDataService(final IntLogger logger, RestConnection restConnection) {
        super(restConnection);
        this.logger = logger;
    }

    public Result installAndRunScan(CIEnvironmentVariables commonEnvVars, HubServerConfig hubConfig) {
        // TODO moving CI code here to run scan
        // try {
        // hubConfig.print(logger);
        //
        // if (jobConfig == null) {
        // // invalid job configuration fail the build.
        // logger.error("Task Configuration invalid. Please validate configuration settings.");
        // result = resultBuilder.failedWithError().build();
        // logTaskResult(logger, result);
        // return result;
        // }
        // restConnection.setCookies(hubConfig.getGlobalCredentials().getUsername(),
        // hubConfig.getGlobalCredentials().getDecryptedPassword());
        // final HubIntRestService hubIntRestService = new HubIntRestService(restConnection);
        // final HubServicesFactory services = new HubServicesFactory(restConnection);
        // final String localHostName = HostnameHelper.getMyHostname();
        // logger.info("Running on machine : " + localHostName);
        // final HubVersionRestService versionRestService = services.createHubVersionRestService();
        // String hubVersion = versionRestService.getHubVersion();
        // final File toolsDir = new File(HubBambooUtils.getInstance().getBambooHome(), CLI_FOLDER_NAME);
        //
        // final HubSupportHelper hubSupport = new HubSupportHelper();
        // hubSupport.checkHubSupport(versionRestService, logger);
        //
        // CLIDownloadService cliDownloadService = services.createCliDownloadService(logger);
        // cliDownloadService.performInstallation(proxyInfo, toolsDir, commonEnvVars, hubConfig.getHubUrl().toString(),
        // hubVersion, localHostName);
        //
        // // Phone-Home
        // try {
        // String regId = null;
        // String hubHostName = null;
        // try {
        // regId = hubIntRestService.getRegistrationId();
        // } catch (final Exception e) {
        // logger.debug("Could not get the Hub registration Id.");
        // }
        // try {
        // final URL url = hubConfig.getHubUrl();
        // hubHostName = url.getHost();
        // } catch (final Exception e) {
        // logger.debug("Could not get the Hub Host name.");
        // }
        // bdPhoneHome(logger, hubVersion, regId, hubHostName, pluginHelper);
        // } catch (final Exception e) {
        // logger.debug("Unable to phone-home", e);
        // }
        //
        // // run the scan
        // int scanMemory = jobConfig.getScanMemory();
        // String workingDirectory = jobConfig.getWorkingDirectory();
        // String projectName = jobConfig.getProjectName();
        // String versionName = jobConfig.getVersion();
        // boolean isDryRun = jobConfig.isDryRun();
        // final SimpleScanService simpleScanService = services.createSimpleScanService(logger, restConnection,
        // hubConfig, hubSupport, commonEnvVars, toolsDir,
        // scanMemory, true, isDryRun, projectName, versionName, jobConfig.getScanTargetPaths(), workingDirectory);
        //
        // Result scanResult = simpleScanService.setupAndExecuteScan();
        //

        return null;
    }
}
