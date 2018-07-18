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
package com.blackducksoftware.integration.hub.cli.parallel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.cli.CLILocation;
import com.blackducksoftware.integration.hub.cli.SignatureScanConfig;
import com.blackducksoftware.integration.hub.cli.SimpleScanUtility;
import com.blackducksoftware.integration.hub.cli.summary.ScanTargetOutput;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.IntEnvironmentVariables;
import com.google.gson.Gson;

public class ScanPathCallable implements Callable<ScanTargetOutput> {
    private final IntLogger logger;
    private final HubServerConfig hubServerConfig;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final SignatureScanConfig signatureScanConfig;
    private final String projectName;
    private final String projectVersionName;
    private final CLILocation cliLocation;
    private final Gson gson;

    public ScanPathCallable(final IntLogger logger, final IntEnvironmentVariables intEnvironmentVariables, final SignatureScanConfig signatureScanConfig, final String projectName,
            final String projectVersionName, final CLILocation cliLocation, final Gson gson) {
        this(logger, null, intEnvironmentVariables, signatureScanConfig, projectName, projectVersionName, cliLocation, gson);
    }

    public ScanPathCallable(final IntLogger logger, final HubServerConfig hubServerConfig, final IntEnvironmentVariables intEnvironmentVariables, final SignatureScanConfig signatureScanConfig, final String projectName,
            final String projectVersionName, final CLILocation cliLocation, final Gson gson) {
        this.logger = logger;
        this.hubServerConfig = hubServerConfig;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.signatureScanConfig = signatureScanConfig;
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.cliLocation = cliLocation;
        this.gson = gson;
    }

    @Override
    public ScanTargetOutput call() throws InterruptedException {
        ScanTargetOutput scanTargetOutput = null;

        final SimpleScanUtility simpleScanUtility;
        if (null != hubServerConfig) {
            simpleScanUtility = new SimpleScanUtility(logger, hubServerConfig, intEnvironmentVariables, signatureScanConfig, projectName, projectVersionName);
        } else {
            simpleScanUtility = new SimpleScanUtility(logger, intEnvironmentVariables, signatureScanConfig, projectName, projectVersionName);
        }

        logger.info(String.format("Starting the signature scan of %s", simpleScanUtility.getSignatureScanConfig().getScanTarget()));
        ScanSummaryView scanSummaryView = null;
        File dryRunFile = null;
        File logDirectory = null;
        final String scanTarget = simpleScanUtility.getSignatureScanConfig().getScanTarget();
        try {
            simpleScanUtility.setupAndExecuteScan(cliLocation);

            scanSummaryView = getScanSummaryFromFile(simpleScanUtility.getScanSummaryFile());
            dryRunFile = simpleScanUtility.getDryRunFile();
            logDirectory = simpleScanUtility.getLogDirectory();
        } catch (IllegalArgumentException | IntegrationException e) {
            final String errorMessage = String.format("There was a problem scanning target '%s' : %s", scanTarget, e.getMessage());
            scanTargetOutput = ScanTargetOutput.FAILURE(scanTarget, logDirectory, dryRunFile, scanSummaryView, errorMessage, e);
            return scanTargetOutput;
        }
        scanTargetOutput = ScanTargetOutput.SUCCESS(scanTarget, logDirectory, dryRunFile, scanSummaryView);

        logger.info(String.format("Completed the signature scan of %s", simpleScanUtility.getSignatureScanConfig().getScanTarget()));
        return scanTargetOutput;
    }

    private ScanSummaryView getScanSummaryFromFile(final File scanSummaryFile) {
        ScanSummaryView scanSummaryView = null;
        try {
            if (null != scanSummaryFile) {
                final String scanSummaryJson = FileUtils.readFileToString(scanSummaryFile, Charset.forName("UTF8"));
                scanSummaryView = gson.fromJson(scanSummaryJson, ScanSummaryView.class);
                scanSummaryView.json = scanSummaryJson;
            }
        } catch (final IOException ex) {
            logger.trace("Error reading scan summary file", ex);
        }
        return scanSummaryView;
    }

}
