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

import java.util.List;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.cli.SimpleScanService;
import com.blackducksoftware.integration.hub.dataservice.phonehome.PhoneHomeDataService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class CLIDataService extends HubRequestService {
    private final IntLogger logger;

    private final CIEnvironmentVariables ciEnvironmentVariables;

    private final HubVersionRequestService hubVersionRequestService;

    private final CLIDownloadService cliDownloadService;

    private final PhoneHomeDataService phoneHomeDataService;

    public CLIDataService(final IntLogger logger, final RestConnection restConnection, CIEnvironmentVariables ciEnvironmentVariables,
            final HubVersionRequestService hubVersionRequestService,
            CLIDownloadService cliDownloadService, PhoneHomeDataService phoneHomeDataService) {
        super(restConnection);
        this.logger = logger;
        this.ciEnvironmentVariables = ciEnvironmentVariables;
        this.hubVersionRequestService = hubVersionRequestService;
        this.cliDownloadService = cliDownloadService;
        this.phoneHomeDataService = phoneHomeDataService;
    }

    public List<ScanSummaryItem> installAndRunScan(final HubServerConfig hubServerConfig,
            HubScanConfig hubScanConfig)
            throws HubIntegrationException, EncryptionException {
        final String localHostName = HostnameHelper.getMyHostname();
        logger.info("Running on machine : " + localHostName);
        printConfiguration(hubScanConfig);
        final String hubVersion = hubVersionRequestService.getHubVersion();
        cliDownloadService.performInstallation(hubServerConfig.getProxyInfo(), hubScanConfig.getToolsDir(), ciEnvironmentVariables,
                hubServerConfig.getHubUrl().toString(),
                hubVersion, localHostName);

        phoneHomeDataService.phoneHome(hubServerConfig, hubScanConfig, hubVersion);

        final HubSupportHelper hubSupportHelper = new HubSupportHelper();
        hubSupportHelper.checkHubSupport(hubVersionRequestService, logger);
        final SimpleScanService simpleScanService = new SimpleScanService(logger, getRestConnection(), hubServerConfig, hubSupportHelper,
                ciEnvironmentVariables,
                hubScanConfig.getToolsDir(),
                hubScanConfig.getScanMemory(), hubScanConfig.isDryRun(), hubScanConfig.getProjectName(), hubScanConfig.getVersion(),
                hubScanConfig.getScanTargetPaths(), hubScanConfig.getWorkingDirectory());
        simpleScanService.setupAndExecuteScan();
        return simpleScanService.getScanSummaryItems();
    }

    public void printConfiguration(HubScanConfig hubScanConfig) {
        logger.alwaysLog("--> Log Level : " + logger.getLogLevel().name());
        hubScanConfig.print(logger);
    }

}
