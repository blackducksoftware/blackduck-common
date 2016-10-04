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
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.api.report.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.api.scan.ScanHistoryItem;
import com.blackducksoftware.integration.hub.api.scan.ScanLocationItem;
import com.blackducksoftware.integration.hub.api.scan.ScanStatus;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.test.TestLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HubEventPollingTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

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

		final RestConnection restConnection = new RestConnection("FakeHubUrl");
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
		assertTrue(logger.getOutputString(), logger.getOutputString()
				.contains("Cleaning up the scan status files at : " + scanStatusDir.getCanonicalPath()));
		assertTrue(!statusFile1.exists());
		assertTrue(!statusFile2.exists());
		assertTrue(!statusFile3.exists());
		assertTrue(!scanStatusDir.exists());
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

		final RestConnection restConnection = new RestConnection("FakeHubUrl");
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

		final RestConnection restConnection = new RestConnection("FakeHubUrl");
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
	public void testIsBomUpToDate() throws Exception {
		final DateTime beforeScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime startScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime inScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime endScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime afterScanTime = new DateTime();

		final String fakeHost = "TestHost";
		final String serverPath1 = "/Test/Fake/Path";
		final String serverPath2 = "/Test/Fake/Path/Child/";
		final String serverPath3 = "/Test/Fake/File";

		final HubIntRestService restService = Mockito.mock(HubIntRestService.class);

		Mockito.when(restService.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class)))
				.then(new Answer<List<ScanLocationItem>>() {
					@Override
					public List<ScanLocationItem> answer(final InvocationOnMock invocation) throws Throwable {
						final ScanHistoryItem historyBeforeScanTime = new ScanHistoryItem();
						historyBeforeScanTime.setCreatedOn(beforeScanTime.toString());
						historyBeforeScanTime.setStatus(ScanStatus.ERROR);

						final ScanHistoryItem historyInScanTime = new ScanHistoryItem();
						historyInScanTime.setCreatedOn(inScanTime.toString());
						historyInScanTime.setStatus(ScanStatus.COMPLETE);

						final ScanHistoryItem historyAfterScanTime = new ScanHistoryItem();
						historyAfterScanTime.setCreatedOn(afterScanTime.toString());
						historyAfterScanTime.setStatus(ScanStatus.MATCHING);

						final List<ScanHistoryItem> historyList = new ArrayList<>();
						historyList.add(historyBeforeScanTime);
						historyList.add(historyInScanTime);
						historyList.add(historyAfterScanTime);

						final ScanLocationItem sl1 = new ScanLocationItem();
						sl1.setHost(fakeHost);
						sl1.setPath(serverPath1);
						sl1.setScanList(historyList);
						final ScanLocationItem sl2 = new ScanLocationItem();
						sl2.setHost(fakeHost);
						sl2.setPath(serverPath2);
						sl2.setScanList(historyList);
						final ScanLocationItem sl3 = new ScanLocationItem();
						sl3.setHost(fakeHost);
						sl3.setPath(serverPath3);
						sl3.setScanList(historyList);

						final List<ScanLocationItem> items = new ArrayList<>();
						items.add(sl1);
						items.add(sl2);
						items.add(sl3);

						return items;
					}
				});

		final List<String> scanTargets = new ArrayList<>();
		scanTargets.add("Test/Fake/Path/Child");
		scanTargets.add("Test\\Fake\\File");
		final HubEventPolling eventPoller = new HubEventPolling(restService);

		final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
		hubReportGenerationInfo.setBeforeScanTime(startScanTime);
		hubReportGenerationInfo.setAfterScanTime(endScanTime);
		hubReportGenerationInfo.setHostname(fakeHost);
		hubReportGenerationInfo.setScanTargets(scanTargets);
		hubReportGenerationInfo.setMaximumWaitTime(5000);

		eventPoller.assertBomUpToDate(hubReportGenerationInfo);
	}

	@Test
	public void testIsBomUpToDateNotYetUpToDate() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("The Bom has not finished updating from the scan within the specified wait time :");

		final DateTime beforeScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime startScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime inScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime endScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime afterScanTime = new DateTime();

		final String fakeHost = "TestHost";
		final String serverPath1 = "/Test/Fake/Path";
		final String serverPath2 = "/Test/Fake/Path/Child/";
		final String serverPath3 = "/Test/Fake/File";

		final HubIntRestService restService = Mockito.mock(HubIntRestService.class);

		Mockito.when(restService.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class)))
				.then(new Answer<List<ScanLocationItem>>() {
					@Override
					public List<ScanLocationItem> answer(final InvocationOnMock invocation) throws Throwable {

						final ScanHistoryItem historyBeforeScanTime = new ScanHistoryItem();
						historyBeforeScanTime.setCreatedOn(beforeScanTime.toString());
						historyBeforeScanTime.setStatus(ScanStatus.ERROR);

						final ScanHistoryItem historyInScanTime = new ScanHistoryItem();
						historyInScanTime.setCreatedOn(inScanTime.toString());
						historyInScanTime.setStatus(ScanStatus.BUILDING_BOM);

						final ScanHistoryItem historyAfterScanTime = new ScanHistoryItem();
						historyAfterScanTime.setCreatedOn(afterScanTime.toString());
						historyAfterScanTime.setStatus(ScanStatus.MATCHING);

						final List<ScanHistoryItem> historyList = new ArrayList<>();
						historyList.add(historyBeforeScanTime);
						historyList.add(historyInScanTime);
						historyList.add(historyAfterScanTime);

						final ScanLocationItem sl1 = new ScanLocationItem();
						sl1.setHost(fakeHost);
						sl1.setPath(serverPath1);
						sl1.setScanList(historyList);
						final ScanLocationItem sl2 = new ScanLocationItem();
						sl2.setHost(fakeHost);
						sl2.setPath(serverPath2);
						sl2.setScanList(historyList);
						final ScanLocationItem sl3 = new ScanLocationItem();
						sl3.setHost(fakeHost);
						sl3.setPath(serverPath3);
						sl3.setScanList(historyList);

						final List<ScanLocationItem> items = new ArrayList<>();
						items.add(sl1);
						items.add(sl2);
						items.add(sl3);

						return items;
					}
				});

		final List<String> scanTargets = new ArrayList<>();
		scanTargets.add("Test/Fake/Path/Child");
		scanTargets.add("Test\\Fake\\File");
		final HubEventPolling eventPoller = new HubEventPolling(restService);

		final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
		hubReportGenerationInfo.setBeforeScanTime(startScanTime);
		hubReportGenerationInfo.setAfterScanTime(endScanTime);
		hubReportGenerationInfo.setHostname(fakeHost);
		hubReportGenerationInfo.setScanTargets(scanTargets);
		hubReportGenerationInfo.setMaximumWaitTime(1000);

		eventPoller.assertBomUpToDate(hubReportGenerationInfo);
	}

	@Test
	public void testIsBomUpToDateError() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("There was a problem with one of the code locations. Error Status :");

		final DateTime beforeScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime startScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime inScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime endScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime afterScanTime = new DateTime();

		final String fakeHost = "TestHost";
		final String serverPath1 = "/Test/Fake/Path";
		final String serverPath2 = "/Test/Fake/Path/Child/";
		final String serverPath3 = "/Test/Fake/File";

		final HubIntRestService restService = Mockito.mock(HubIntRestService.class);

		Mockito.when(restService.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class)))
				.then(new Answer<List<ScanLocationItem>>() {
					@Override
					public List<ScanLocationItem> answer(final InvocationOnMock invocation) throws Throwable {

						final ScanHistoryItem historyBeforeScanTime = new ScanHistoryItem();
						historyBeforeScanTime.setCreatedOn(beforeScanTime.toString());
						historyBeforeScanTime.setStatus(ScanStatus.ERROR);

						final ScanHistoryItem historyInScanTime = new ScanHistoryItem();
						historyInScanTime.setCreatedOn(inScanTime.toString());
						historyInScanTime.setStatus(ScanStatus.ERROR);

						final ScanHistoryItem historyAfterScanTime = new ScanHistoryItem();
						historyAfterScanTime.setCreatedOn(afterScanTime.toString());
						historyAfterScanTime.setStatus(ScanStatus.MATCHING);

						final List<ScanHistoryItem> historyList = new ArrayList<>();
						historyList.add(historyBeforeScanTime);
						historyList.add(historyInScanTime);
						historyList.add(historyAfterScanTime);

						final ScanLocationItem sl1 = new ScanLocationItem();
						sl1.setHost(fakeHost);
						sl1.setPath(serverPath1);
						sl1.setScanList(historyList);
						final ScanLocationItem sl2 = new ScanLocationItem();
						sl2.setHost(fakeHost);
						sl2.setPath(serverPath2);
						sl2.setScanList(historyList);
						final ScanLocationItem sl3 = new ScanLocationItem();
						sl3.setHost(fakeHost);
						sl3.setPath(serverPath3);
						sl3.setScanList(historyList);

						final List<ScanLocationItem> items = new ArrayList<>();
						items.add(sl1);
						items.add(sl2);
						items.add(sl3);

						return items;
					}
				});

		final List<String> scanTargets = new ArrayList<>();
		scanTargets.add("Test/Fake/Path/Child");
		scanTargets.add("Test\\Fake\\File");
		final HubEventPolling eventPoller = new HubEventPolling(restService);

		final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
		hubReportGenerationInfo.setBeforeScanTime(startScanTime);
		hubReportGenerationInfo.setAfterScanTime(endScanTime);
		hubReportGenerationInfo.setHostname(fakeHost);
		hubReportGenerationInfo.setScanTargets(scanTargets);
		hubReportGenerationInfo.setMaximumWaitTime(5000);

		eventPoller.assertBomUpToDate(hubReportGenerationInfo);
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
