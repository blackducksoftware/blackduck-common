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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.api.report.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.api.report.ReportInformationItem;
import com.blackducksoftware.integration.hub.api.scan.ScanStatus;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.test.TestLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HubEventPollingTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private HubServerConfig hubServerConfig;

    @Before
    public void initTest() throws MalformedURLException {
        final HubProxyInfo proxyInfo = Mockito.mock(HubProxyInfo.class);
        Mockito.when(proxyInfo.getUsername()).thenReturn("");
        Mockito.when(proxyInfo.getEncryptedPassword()).thenReturn("");
        Mockito.when(proxyInfo.getActualPasswordLength()).thenReturn(0);

        final HubCredentials credentials = Mockito.mock(HubCredentials.class);
        Mockito.when(credentials.getUsername()).thenReturn("");
        Mockito.when(credentials.getActualPasswordLength()).thenReturn(0);
        Mockito.when(credentials.getEncryptedPassword()).thenReturn("");
        hubServerConfig = Mockito.mock(HubServerConfig.class);

        Mockito.when(hubServerConfig.getHubUrl()).thenReturn(new URL("http://fakeURL"));
        Mockito.when(hubServerConfig.getTimeout()).thenReturn(120);
        Mockito.when(hubServerConfig.getGlobalCredentials()).thenReturn(credentials);
        Mockito.when(hubServerConfig.getProxyInfo()).thenReturn(proxyInfo);
    }

    private void writeScanStatusToFile(final ScanSummaryItem status, final File file) throws IOException {
        final Gson gson = new GsonBuilder().create();

        final String stringStatus = gson.toJson(status);

        final FileWriter writer = new FileWriter(file);
        writer.write(stringStatus);
        writer.close();
    }

    @Test
    public void testIsBomUpToDateStatusFiles() throws Exception {
        final MetaInformation meta = new MetaInformation(null, "link", null);
        final ScanSummaryItem status1 = new ScanSummaryItem(ScanStatus.REQUESTED_MATCH_JOB, null, null, null, meta);
        final ScanSummaryItem status2 = new ScanSummaryItem(ScanStatus.BUILDING_BOM, null, null, null, meta);
        final ScanSummaryItem status3 = new ScanSummaryItem(ScanStatus.SCANNING, null, null, null, meta);
        final File scanStatusDir = folder.newFolder();
        final File statusFile1 = new File(scanStatusDir, "status1.txt");
        statusFile1.createNewFile();
        final File statusFile2 = new File(scanStatusDir, "status2.txt");
        statusFile2.createNewFile();
        final File statusFile3 = new File(scanStatusDir, "status3.txt");
        statusFile3.createNewFile();
        writeScanStatusToFile(status1, statusFile1);
        writeScanStatusToFile(status2, statusFile2);
        writeScanStatusToFile(status3, statusFile3);

        final ScanSummaryRestService scanSummaryRestService = Mockito.mock(ScanSummaryRestService.class);
        final MetaInformation _meta = new MetaInformation(null, "link", null);
        Mockito.when(scanSummaryRestService.getItem(Mockito.anyString()))
                .thenReturn(new ScanSummaryItem(ScanStatus.COMPLETE, null, null, null, _meta));

        final RestConnection restConnection = new CredentialsRestConnection(hubServerConfig);
        final HubIntRestService restService = new HubIntRestService(restConnection);
        restService.setScanSummaryRestService(scanSummaryRestService);

        final HubEventPolling eventPoller = new HubEventPolling(restService);
        final TestLogger logger = new TestLogger();

        final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
        final List<String> scanTargets = new ArrayList<>();
        scanTargets.add("1");
        scanTargets.add("2");
        scanTargets.add("3");
        hubReportGenerationInfo.setScanTargets(scanTargets);
        hubReportGenerationInfo.setScanStatusDirectory(scanStatusDir.getCanonicalPath());
        hubReportGenerationInfo.setMaximumWaitTime(20000);

        eventPoller.assertBomUpToDate(hubReportGenerationInfo, logger);

        assertTrue(logger.getOutputString(), logger.getOutputString()
                .contains("Checking the directory : " + scanStatusDir.getCanonicalPath() + " for the scan status's."));
        assertTrue(statusFile1.exists());
        assertTrue(scanStatusDir.exists());
    }

    @Test
    public void testIsBomUpToDateStatusFilesNotUpToDate() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The pending scans have not completed within the specified wait time:");

        final MetaInformation meta = new MetaInformation(null, "link", null);
        final ScanSummaryItem status1 = new ScanSummaryItem(ScanStatus.REQUESTED_MATCH_JOB, null, null, null, meta);
        final ScanSummaryItem status2 = new ScanSummaryItem(ScanStatus.BUILDING_BOM, null, null, null, meta);
        final ScanSummaryItem status3 = new ScanSummaryItem(ScanStatus.SCANNING, null, null, null, meta);
        final File scanStatusDir = folder.newFolder();
        final File statusFile1 = new File(scanStatusDir, "status1.txt");
        statusFile1.createNewFile();
        final File statusFile2 = new File(scanStatusDir, "status2.txt");
        statusFile2.createNewFile();
        final File statusFile3 = new File(scanStatusDir, "status3.txt");
        statusFile3.createNewFile();
        writeScanStatusToFile(status1, statusFile1);
        writeScanStatusToFile(status2, statusFile2);
        writeScanStatusToFile(status3, statusFile3);

        final ScanSummaryRestService scanSummaryRestService = Mockito.mock(ScanSummaryRestService.class);
        final MetaInformation _meta = new MetaInformation(null, "link", null);
        Mockito.when(scanSummaryRestService.getItem(Mockito.anyString()))
                .thenReturn(new ScanSummaryItem(ScanStatus.BUILDING_BOM, null, null, null, _meta));

        final RestConnection restConnection = new CredentialsRestConnection(hubServerConfig);
        final HubIntRestService restService = new HubIntRestService(restConnection);
        restService.setScanSummaryRestService(scanSummaryRestService);

        final HubEventPolling eventPoller = new HubEventPolling(restService);
        final TestLogger logger = new TestLogger();

        final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
        final List<String> scanTargets = new ArrayList<>();
        scanTargets.add("1");
        scanTargets.add("2");
        scanTargets.add("3");
        hubReportGenerationInfo.setScanTargets(scanTargets);
        hubReportGenerationInfo.setScanStatusDirectory(scanStatusDir.getCanonicalPath());
        hubReportGenerationInfo.setMaximumWaitTime(1000);

        eventPoller.assertBomUpToDate(hubReportGenerationInfo, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesError() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("There was a problem with one of the scans. Error Status : ");

        final MetaInformation meta = new MetaInformation(null, "link", null);
        final ScanSummaryItem status1 = new ScanSummaryItem(ScanStatus.REQUESTED_MATCH_JOB, null, null, null, meta);
        final ScanSummaryItem status2 = new ScanSummaryItem(ScanStatus.BUILDING_BOM, null, null, null, meta);
        final ScanSummaryItem status3 = new ScanSummaryItem(ScanStatus.SCANNING, null, null, null, meta);
        final File scanStatusDir = folder.newFolder();
        final File statusFile1 = new File(scanStatusDir, "status1.txt");
        statusFile1.createNewFile();
        final File statusFile2 = new File(scanStatusDir, "status2.txt");
        statusFile2.createNewFile();
        final File statusFile3 = new File(scanStatusDir, "status3.txt");
        statusFile3.createNewFile();
        writeScanStatusToFile(status1, statusFile1);
        writeScanStatusToFile(status2, statusFile2);
        writeScanStatusToFile(status3, statusFile3);

        final ScanSummaryRestService scanSummaryRestService = Mockito.mock(ScanSummaryRestService.class);
        final MetaInformation _meta = new MetaInformation(null, "link", null);
        Mockito.when(scanSummaryRestService.getItem(Mockito.anyString()))
                .thenReturn(new ScanSummaryItem(ScanStatus.ERROR, null, null, null, _meta));

        final RestConnection restConnection = new CredentialsRestConnection(hubServerConfig);
        final HubIntRestService restService = new HubIntRestService(restConnection);
        restService.setScanSummaryRestService(scanSummaryRestService);

        final HubEventPolling eventPoller = new HubEventPolling(restService);
        final TestLogger logger = new TestLogger();

        final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
        final List<String> scanTargets = new ArrayList<>();
        scanTargets.add("1");
        scanTargets.add("2");
        scanTargets.add("3");
        hubReportGenerationInfo.setScanTargets(scanTargets);
        hubReportGenerationInfo.setScanStatusDirectory(scanStatusDir.getCanonicalPath());
        hubReportGenerationInfo.setMaximumWaitTime(20000);

        eventPoller.assertBomUpToDate(hubReportGenerationInfo, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesIncorrectFileContent() throws Exception {
        final MetaInformation meta = new MetaInformation(null, "link", null);
        final File scanStatusDir = folder.newFolder();
        final File statusFile1 = new File(scanStatusDir, "status1.txt");
        statusFile1.createNewFile();
        final Gson gson = new GsonBuilder().create();

        final String stringStatus = gson.toJson(meta);

        final FileWriter writer = new FileWriter(statusFile1);
        writer.write(stringStatus);
        writer.close();

        final HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        final HubEventPolling eventPoller = new HubEventPolling(restService);
        final TestLogger logger = new TestLogger();
        try {
            final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
            final List<String> scanTargets = new ArrayList<>();
            scanTargets.add("1");
            hubReportGenerationInfo.setScanTargets(scanTargets);
            hubReportGenerationInfo.setScanStatusDirectory(scanStatusDir.getCanonicalPath());
            hubReportGenerationInfo.setMaximumWaitTime(1000);

            eventPoller.assertBomUpToDate(hubReportGenerationInfo, logger);
        } catch (final Exception e) {
            assertTrue(e instanceof HubIntegrationException);
            assertTrue(e.getMessage(), e.getMessage().contains("The scan status file : "
                    + statusFile1.getCanonicalPath() + " does not contain valid scan status json."));
        }
        assertTrue(statusFile1.exists());
        assertTrue(scanStatusDir.exists());
    }

    @Test
    public void testIsBomUpToDateStatusFilesNumberMisMatch() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("There were " + 5 + " scans configured and we found " + 3 + " status files.");

        final MetaInformation meta = new MetaInformation(null, "link", null);
        final ScanSummaryItem status1 = new ScanSummaryItem(ScanStatus.REQUESTED_MATCH_JOB, null, null, null, meta);
        final ScanSummaryItem status2 = new ScanSummaryItem(ScanStatus.BUILDING_BOM, null, null, null, meta);
        final ScanSummaryItem status3 = new ScanSummaryItem(ScanStatus.SCANNING, null, null, null, meta);
        final File scanStatusDir = folder.newFolder();
        final File statusFile1 = new File(scanStatusDir, "status1.txt");
        statusFile1.createNewFile();
        final File statusFile2 = new File(scanStatusDir, "status2.txt");
        statusFile2.createNewFile();
        final File statusFile3 = new File(scanStatusDir, "status3.txt");
        statusFile3.createNewFile();
        writeScanStatusToFile(status1, statusFile1);
        writeScanStatusToFile(status2, statusFile2);
        writeScanStatusToFile(status3, statusFile3);

        final HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        final HubEventPolling eventPoller = new HubEventPolling(restService);
        final TestLogger logger = new TestLogger();

        final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
        final List<String> scanTargets = new ArrayList<>();
        scanTargets.add("1");
        scanTargets.add("2");
        scanTargets.add("3");
        scanTargets.add("4");
        scanTargets.add("5");
        hubReportGenerationInfo.setScanTargets(scanTargets);
        hubReportGenerationInfo.setScanStatusDirectory(scanStatusDir.getCanonicalPath());
        hubReportGenerationInfo.setMaximumWaitTime(20000);

        eventPoller.assertBomUpToDate(hubReportGenerationInfo, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesNoScanStatusFiles() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("Can not find the scan status files in the directory provided.");

        final File scanStatusDir = folder.newFolder();

        final HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        final HubEventPolling eventPoller = new HubEventPolling(restService);
        final TestLogger logger = new TestLogger();

        final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
        final List<String> scanTargets = new ArrayList<>();
        hubReportGenerationInfo.setScanTargets(scanTargets);
        hubReportGenerationInfo.setScanStatusDirectory(scanStatusDir.getCanonicalPath());
        hubReportGenerationInfo.setMaximumWaitTime(1000);

        eventPoller.assertBomUpToDate(hubReportGenerationInfo, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesNotADirectory() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The scan status directory provided is not a directory.");

        final File scanStatusDir = folder.newFile();

        final HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        final HubEventPolling eventPoller = new HubEventPolling(restService);
        final TestLogger logger = new TestLogger();

        final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
        final List<String> scanTargets = new ArrayList<>();
        hubReportGenerationInfo.setScanTargets(scanTargets);
        hubReportGenerationInfo.setScanStatusDirectory(scanStatusDir.getCanonicalPath());
        hubReportGenerationInfo.setMaximumWaitTime(1000);

        eventPoller.assertBomUpToDate(hubReportGenerationInfo, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesDirectoryDNE() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The scan status directory does not exist.");

        final File scanStatusDir = new File("/ASSERTFAKE/DOES NOT EXIST/ANYWHERE");

        final HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        final HubEventPolling eventPoller = new HubEventPolling(restService);
        final TestLogger logger = new TestLogger();

        final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
        final List<String> scanTargets = new ArrayList<>();
        hubReportGenerationInfo.setScanTargets(scanTargets);
        hubReportGenerationInfo.setScanStatusDirectory(scanStatusDir.getCanonicalPath());
        hubReportGenerationInfo.setMaximumWaitTime(1000);

        eventPoller.assertBomUpToDate(hubReportGenerationInfo, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesDirectoryNotProvided() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The scan status directory must be a non empty value.");

        final HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        final HubEventPolling eventPoller = new HubEventPolling(restService);
        final TestLogger logger = new TestLogger();

        final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
        final List<String> scanTargets = new ArrayList<>();
        hubReportGenerationInfo.setScanTargets(scanTargets);
        hubReportGenerationInfo.setScanStatusDirectory("");
        hubReportGenerationInfo.setMaximumWaitTime(1000);

        eventPoller.assertBomUpToDate(hubReportGenerationInfo, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesDirectoryNull() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The scan status directory must be a non empty value.");

        final HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        final HubEventPolling eventPoller = new HubEventPolling(restService);
        final TestLogger logger = new TestLogger();

        final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
        final List<String> scanTargets = new ArrayList<>();
        hubReportGenerationInfo.setScanTargets(scanTargets);
        hubReportGenerationInfo.setScanStatusDirectory(null);
        hubReportGenerationInfo.setMaximumWaitTime(1000);

        eventPoller.assertBomUpToDate(hubReportGenerationInfo, logger);
    }

    @Test
    public void testIsReportDoneGeneratingNotDone() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The Report has not finished generating in : ");

        // 5 seconds
        final long maximumWait = 1000 * 5;

        final HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        Mockito.when(restService.getReportInformation(Mockito.anyString())).then(new Answer<ReportInformationItem>() {
            @Override
            public ReportInformationItem answer(final InvocationOnMock invocation) throws Throwable {
                return new ReportInformationItem(null, null, null, 0, null, null, null, null, null);
            }
        });
        final HubEventPolling eventPoller = new HubEventPolling(restService);
        eventPoller.isReportFinishedGenerating("", maximumWait);
    }

    @Test
    public void testIsReportDoneGeneratingDone() throws Exception {
        // 5 seconds
        final long maximumWait = 1000 * 5;

        final HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        Mockito.when(restService.getReportInformation(Mockito.anyString())).then(new Answer<ReportInformationItem>() {
            @Override
            public ReportInformationItem answer(final InvocationOnMock invocation) throws Throwable {
                return new ReportInformationItem(null, null, null, 0, null, null, "test", null, null);
            }
        });
        final HubEventPolling eventPoller = new HubEventPolling(restService);
        eventPoller.isReportFinishedGenerating("", maximumWait);
    }

}
