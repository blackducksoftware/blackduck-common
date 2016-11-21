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
package com.blackducksoftware.integration.hub.polling;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.api.report.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.api.report.ReportInformationItem;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.HubTimeoutExceededException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.log.IntLogger;

public class HubEventPolling {
    /**
     * Checks the status's in the scan files and polls their URL's, every 10
     * seconds, until they have all have status COMPLETE. We keep trying until
     * we hit the maximum wait time. If we find a scan history object that has
     * status cancelled or an error type then we throw an exception.
     *
     * @throws HubTimeoutExceededException
     * @throws UnexpectedHubResponseException
     * @throws ProjectDoesNotExistException
     */
    public void assertBomUpToDate(ScanStatusDataService scanStatusDataService, final HubReportGenerationInfo hubReportGenerationInfo, final IntLogger logger)
            throws InterruptedException, BDRestException, HubIntegrationException, URISyntaxException, IOException,
            ProjectDoesNotExistException, UnexpectedHubResponseException {
        if (StringUtils.isBlank(hubReportGenerationInfo.getScanStatusDirectory())) {
            throw new HubIntegrationException("The scan status directory must be a non empty value.");
        }
        final File statusDirectory = new File(hubReportGenerationInfo.getScanStatusDirectory());
        if (!statusDirectory.exists()) {
            throw new HubIntegrationException("The scan status directory does not exist.");
        }
        if (!statusDirectory.isDirectory()) {
            throw new HubIntegrationException("The scan status directory provided is not a directory.");
        }
        final File[] statusFiles = statusDirectory.listFiles();
        if (statusFiles == null || statusFiles.length == 0) {
            throw new HubIntegrationException("Can not find the scan status files in the directory provided.");
        }
        int expectedNumScans = 0;
        if (hubReportGenerationInfo.getScanTargets() != null && !hubReportGenerationInfo.getScanTargets().isEmpty()) {
            expectedNumScans = hubReportGenerationInfo.getScanTargets().size();
        }
        if (statusFiles.length != expectedNumScans) {
            throw new HubIntegrationException("There were " + expectedNumScans + " scans configured and we found "
                    + statusFiles.length + " status files.");
        }
        logger.info("Checking the directory : " + statusDirectory.getCanonicalPath() + " for the scan status's.");
        final List<ScanSummaryItem> scanSummaryItems = new ArrayList<>();
        for (final File currentStatusFile : statusFiles) {
            final String fileContent = FileUtils.readFileToString(currentStatusFile, "UTF8");
            final ScanSummaryItem scanSummaryItem = scanStatusDataService.getRestConnection().getGson().fromJson(fileContent, ScanSummaryItem.class);
            if (scanSummaryItem.getMeta() == null || scanSummaryItem.getStatus() == null) {
                throw new HubIntegrationException("The scan status file : " + currentStatusFile.getCanonicalPath()
                        + " does not contain valid scan status json.");
            }
            scanSummaryItems.add(scanSummaryItem);
        }

        final long timeoutInMilliseconds = hubReportGenerationInfo.getMaximumWaitTime();
        scanStatusDataService.assertBomImportScansFinished(scanSummaryItems, timeoutInMilliseconds);
    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished
     * time available, then we know it is done being generated. Throws
     * HubIntegrationException after 30 minutes if the report has not been
     * generated yet.
     *
     */
    public ReportInformationItem isReportFinishedGenerating(HubIntRestService service, final String reportUrl)
            throws IOException, BDRestException, URISyntaxException, InterruptedException, HubIntegrationException {
        // maximum wait time of 30 minutes
        final long maximumWait = 1000 * 60 * 30;
        return isReportFinishedGenerating(service, reportUrl, maximumWait);
    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished
     * time available, then we know it is done being generated. Throws
     * HubIntegrationException after the maximum wait if the report has not been
     * generated yet.
     *
     */
    public ReportInformationItem isReportFinishedGenerating(HubIntRestService service, final String reportUrl, final long maximumWait)
            throws IOException, BDRestException, URISyntaxException, InterruptedException, HubIntegrationException {
        final long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        String timeFinished = null;
        ReportInformationItem reportInfo = null;

        while (timeFinished == null) {
            reportInfo = service.getReportInformation(reportUrl);
            timeFinished = reportInfo.getFinishedAt();
            if (timeFinished != null) {
                break;
            }
            if (elapsedTime >= maximumWait) {
                final String formattedTime = String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(maximumWait));
                throw new HubIntegrationException("The Report has not finished generating in : " + formattedTime);
            }
            // Retry every 5 seconds
            Thread.sleep(5000);
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        return reportInfo;
    }

}
