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
package com.blackducksoftware.integration.hub.report.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.report.AggregateBomViewEntry;
import com.blackducksoftware.integration.hub.api.report.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.api.report.HubRiskReportData;
import com.blackducksoftware.integration.hub.api.report.ReportCategoriesEnum;
import com.blackducksoftware.integration.hub.api.report.ReportFormatEnum;
import com.blackducksoftware.integration.hub.api.report.RiskReportGenerator;
import com.blackducksoftware.integration.hub.api.report.VersionReport;
import com.blackducksoftware.integration.hub.api.scan.ScanHistoryItem;
import com.blackducksoftware.integration.hub.api.scan.ScanLocationItem;
import com.blackducksoftware.integration.hub.api.scan.ScanStatus;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.test.TestLogger;

public class RiskReportGeneratorTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Rule
	public ExpectedException exception = ExpectedException.none();

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

	@Test
	public void generateReportWithScanStatusFiles() throws Exception {
		final ScanSummaryRestService scanSummaryRestService = Mockito.mock(ScanSummaryRestService.class);

		final MetaInformation _meta = new MetaInformation(null, "link", null);
		final ScanSummaryItem statusBuilding = new ScanSummaryItem(ScanStatus.BUILDING_BOM, null, null, null, _meta);
		Mockito.when(scanSummaryRestService.getItem(Mockito.anyString()))
				.thenReturn(new ScanSummaryItem(ScanStatus.COMPLETE, null, null, null, _meta));

		final RestConnection restConnection = new CredentialsRestConnection(hubServerConfig);
		HubIntRestService service = new HubIntRestService(restConnection);
		service.setScanSummaryRestService(scanSummaryRestService);
		service = Mockito.spy(service);

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(20000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		final List<String> scanTargets = new ArrayList<>();
		scanTargets.add("test");
		generatorInfo.setScanTargets(scanTargets);

		final File scanStatusDirectory = folder.newFolder();

		generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
		final File file = new File(scanStatusDirectory, "scanStatus.txt");

		final FileWriter writer = new FileWriter(file);
		writer.write(service.getGson().toJson(statusBuilding));
		writer.close();

		final ScanSummaryItem statusComplete = new ScanSummaryItem(ScanStatus.COMPLETE, null, null, null, _meta);
		Mockito.doReturn(statusComplete).when(service).checkScanStatus(Mockito.anyString());
		Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.any(ReleaseItem.class),
				Mockito.any(ReportFormatEnum.class), Mockito.any(ReportCategoriesEnum[].class));

		final List<MetaLink> links = new ArrayList<>();
		final MetaLink linkItem = new MetaLink("content", "FakeContentLink");
		links.add(linkItem);
		final MetaInformation reportMeta = new MetaInformation(null, null, links);
		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0,
				null, null, "Finished", null, reportMeta);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		final List<AggregateBomViewEntry> bomEntries = new ArrayList<>();
		final VersionReport report = new VersionReport(null, bomEntries);

		Mockito.doReturn(report).when(service).getReportContent(Mockito.anyString());
		Mockito.doReturn(204).when(service).deleteHubReport(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("3.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		final HubRiskReportData hubRiskReportData = generator.generateHubReport(logger, ReportCategoriesEnum.values());
		assertEquals(report, hubRiskReportData.getReport());
		final String output = logger.getOutputString();
		assertTrue(output, output.contains("Waiting for the bom to be updated with the scan results."));
		assertTrue(output, output.contains("The bom has been updated, generating the report."));
		assertTrue(output, output.contains("Finished retrieving the report."));
	}

	@Test
	public void generateReportWithScanStatusFilesNoContentLink() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("Could not find content link for the report at : ");

		final ScanSummaryRestService scanSummaryRestService = Mockito.mock(ScanSummaryRestService.class);

		final MetaInformation _meta = new MetaInformation(null, "link", null);
		final ScanSummaryItem statusComplete = new ScanSummaryItem(ScanStatus.COMPLETE, null, null, null, _meta);
		Mockito.when(scanSummaryRestService.getItem(Mockito.anyString())).thenReturn(statusComplete);

		final RestConnection restConnection = new CredentialsRestConnection(hubServerConfig);
		;
		HubIntRestService service = new HubIntRestService(restConnection);
		service.setScanSummaryRestService(scanSummaryRestService);
		service = Mockito.spy(service);

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(20000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		final List<String> scanTargets = new ArrayList<>();
		scanTargets.add("test");
		generatorInfo.setScanTargets(scanTargets);

		final File scanStatusDirectory = folder.newFolder();

		generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
		final File file = new File(scanStatusDirectory, "scanStatus.txt");

		final ScanSummaryItem statusBuilding = new ScanSummaryItem(ScanStatus.BUILDING_BOM, null, null, null, _meta);

		final FileWriter writer = new FileWriter(file);
		writer.write(service.getGson().toJson(statusBuilding));
		writer.close();

		Mockito.doReturn(statusComplete).when(service).checkScanStatus(Mockito.anyString());
		Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.any(ReleaseItem.class),
				Mockito.any(ReportFormatEnum.class), Mockito.any(ReportCategoriesEnum[].class));

		final List<MetaLink> links = new ArrayList<>();
		final MetaInformation reportMeta = new MetaInformation(null, null, links);
		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0,
				null, null, "Finished", null, reportMeta);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("3.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		generator.generateHubReport(logger, ReportCategoriesEnum.values()).getReport();
	}

	@Test
	public void generateReportWithScanStatusFilesReportNotFinishedGenerating() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("The Report has not finished generating in : ");

		final ScanSummaryRestService scanSummaryRestService = Mockito.mock(ScanSummaryRestService.class);

		final MetaInformation _meta = new MetaInformation(null, "link", null);
		final ScanSummaryItem statusBuilding = new ScanSummaryItem(ScanStatus.BUILDING_BOM, null, null, null, _meta);
		Mockito.when(scanSummaryRestService.getItem(Mockito.anyString()))
				.thenReturn(new ScanSummaryItem(ScanStatus.COMPLETE, null, null, null, _meta));

		final RestConnection restConnection = new CredentialsRestConnection(hubServerConfig);
		HubIntRestService service = new HubIntRestService(restConnection);
		service.setScanSummaryRestService(scanSummaryRestService);
		service = Mockito.spy(service);

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(5000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		final List<String> scanTargets = new ArrayList<>();
		scanTargets.add("test");
		generatorInfo.setScanTargets(scanTargets);

		final File scanStatusDirectory = folder.newFolder();

		generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
		final File file = new File(scanStatusDirectory, "scanStatus.txt");

		final FileWriter writer = new FileWriter(file);
		writer.write(service.getGson().toJson(statusBuilding));
		writer.close();

		final ScanSummaryItem statusComplete = new ScanSummaryItem(ScanStatus.COMPLETE, null, null, null, _meta);
		Mockito.doReturn(statusComplete).when(service).checkScanStatus(Mockito.anyString());
		Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.any(ReleaseItem.class),
				Mockito.any(ReportFormatEnum.class), Mockito.any(ReportCategoriesEnum[].class));

		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0,
				null, null, null, null, null);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("3.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		generator.generateHubReport(logger, ReportCategoriesEnum.values()).getReport();
	}

	@Test
	public void generateReportWithScanStatusFilesRiskNotUpToDate() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("The pending scans have not completed within the specified wait time:");

		final ScanSummaryRestService scanSummaryRestService = Mockito.mock(ScanSummaryRestService.class);

		final MetaInformation _meta = new MetaInformation(null, "link", null);
		final ScanSummaryItem statusBuilding = new ScanSummaryItem(ScanStatus.BUILDING_BOM, null, null, null, _meta);
		Mockito.when(scanSummaryRestService.getItem(Mockito.anyString())).thenReturn(statusBuilding);

		final RestConnection restConnection = new CredentialsRestConnection(hubServerConfig);
		HubIntRestService service = new HubIntRestService(restConnection);
		service.setScanSummaryRestService(scanSummaryRestService);
		service = Mockito.spy(service);

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(5000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		final List<String> scanTargets = new ArrayList<>();
		scanTargets.add("test");
		generatorInfo.setScanTargets(scanTargets);

		final File scanStatusDirectory = folder.newFolder();

		generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
		final File file = new File(scanStatusDirectory, "scanStatus.txt");

		final FileWriter writer = new FileWriter(file);
		writer.write(service.getGson().toJson(statusBuilding));
		writer.close();

		Mockito.doReturn(statusBuilding).when(service).checkScanStatus(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("3.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		generator.generateHubReport(logger, ReportCategoriesEnum.values()).getReport();
	}

	@Test
	public void generateReportWithCodeLocations() throws Exception {
		final HubIntRestService service = Mockito.mock(HubIntRestService.class);

		final String hostName = "FakeHostName";

		final DateTime beforeScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime startScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime inScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime endScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime afterScanTime = new DateTime();

		final String serverPath1 = "/Test/Fake/Path";
		final String serverPath2 = "/Test/Fake/Path/Child/";
		final String serverPath3 = "/Test/Fake/File";

		Mockito.when(service.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class)))
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
						sl1.setHost(hostName);
						sl1.setPath(serverPath1);
						sl1.setScanList(historyList);
						final ScanLocationItem sl2 = new ScanLocationItem();
						sl2.setHost(hostName);
						sl2.setPath(serverPath2);
						sl2.setScanList(historyList);
						final ScanLocationItem sl3 = new ScanLocationItem();
						sl3.setHost(hostName);
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

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(20000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		generatorInfo.setScanTargets(scanTargets);
		generatorInfo.setHostname(hostName);
		generatorInfo.setBeforeScanTime(startScanTime);
		generatorInfo.setAfterScanTime(endScanTime);

		Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.any(ReleaseItem.class),
				Mockito.any(ReportFormatEnum.class), Mockito.any(ReportCategoriesEnum[].class));

		final List<MetaLink> links = new ArrayList<>();
		final MetaLink linkItem = new MetaLink("content", "FakeContentLink");
		links.add(linkItem);
		final MetaInformation reportMeta = new MetaInformation(null, null, links);
		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0,
				null, null, "Finished", null, reportMeta);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		final List<AggregateBomViewEntry> RiskEntries = new ArrayList<>();
		final VersionReport report = new VersionReport(null, RiskEntries);

		Mockito.doReturn(report).when(service).getReportContent(Mockito.anyString());
		Mockito.doReturn(204).when(service).deleteHubReport(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("2.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		assertEquals(report, generator.generateHubReport(logger, null).getReport());
		final String output = logger.getOutputString();
		assertTrue(output, output.contains("Waiting for the bom to be updated with the scan results."));
		assertTrue(output, output.contains("The bom has been updated, generating the report."));
		assertTrue(output, output.contains("Finished retrieving the report."));
	}

	@Test
	public void generateReportWithCodeLocationsNoContentLink() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("Could not find content link for the report at : ");

		final HubIntRestService service = Mockito.mock(HubIntRestService.class);

		final String hostName = "FakeHostName";

		final DateTime beforeScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime startScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime inScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime endScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime afterScanTime = new DateTime();

		final String serverPath1 = "/Test/Fake/Path";
		final String serverPath2 = "/Test/Fake/Path/Child/";
		final String serverPath3 = "/Test/Fake/File";

		Mockito.when(service.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class)))
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
						sl1.setHost(hostName);
						sl1.setPath(serverPath1);
						sl1.setScanList(historyList);
						final ScanLocationItem sl2 = new ScanLocationItem();
						sl2.setHost(hostName);
						sl2.setPath(serverPath2);
						sl2.setScanList(historyList);
						final ScanLocationItem sl3 = new ScanLocationItem();
						sl3.setHost(hostName);
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

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(20000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		generatorInfo.setScanTargets(scanTargets);
		generatorInfo.setHostname(hostName);
		generatorInfo.setBeforeScanTime(startScanTime);
		generatorInfo.setAfterScanTime(endScanTime);

		Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.any(ReleaseItem.class),
				Mockito.any(ReportFormatEnum.class), Mockito.any(ReportCategoriesEnum[].class));

		final List<MetaLink> links = new ArrayList<>();
		final MetaInformation reportMeta = new MetaInformation(null, null, links);
		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0,
				null, null, "Finished", null, reportMeta);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		Mockito.doReturn(204).when(service).deleteHubReport(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("2.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		generator.generateHubReport(logger, ReportCategoriesEnum.values()).getReport();
	}

	@Test
	public void generateReportWithCodeLocationsNotFinishedGeneratingReport() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("The Report has not finished generating in : ");

		final HubIntRestService service = Mockito.mock(HubIntRestService.class);

		final String hostName = "FakeHostName";

		final DateTime beforeScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime startScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime inScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime endScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime afterScanTime = new DateTime();

		final String serverPath1 = "/Test/Fake/Path";
		final String serverPath2 = "/Test/Fake/Path/Child/";
		final String serverPath3 = "/Test/Fake/File";

		Mockito.when(service.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class)))
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
						sl1.setHost(hostName);
						sl1.setPath(serverPath1);
						sl1.setScanList(historyList);
						final ScanLocationItem sl2 = new ScanLocationItem();
						sl2.setHost(hostName);
						sl2.setPath(serverPath2);
						sl2.setScanList(historyList);
						final ScanLocationItem sl3 = new ScanLocationItem();
						sl3.setHost(hostName);
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

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(5000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		generatorInfo.setScanTargets(scanTargets);
		generatorInfo.setHostname(hostName);
		generatorInfo.setBeforeScanTime(startScanTime);
		generatorInfo.setAfterScanTime(endScanTime);

		Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.any(ReleaseItem.class),
				Mockito.any(ReportFormatEnum.class), Mockito.any(ReportCategoriesEnum[].class));

		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0,
				null, null, null, null, null);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("2.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		generator.generateHubReport(logger, null).getReport();
	}

	@Test
	public void generateReportWithCodeLocationsRiskNotUpToDate() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("The Bom has not finished updating from the scan within the specified wait time : ");

		final HubIntRestService service = Mockito.mock(HubIntRestService.class);

		final String hostName = "FakeHostName";

		final DateTime beforeScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime startScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime inScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime endScanTime = new DateTime();
		Thread.sleep(10);
		final DateTime afterScanTime = new DateTime();

		final String serverPath1 = "/Test/Fake/Path";
		final String serverPath2 = "/Test/Fake/Path/Child/";
		final String serverPath3 = "/Test/Fake/File";

		Mockito.when(service.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class)))
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
						sl1.setHost(hostName);
						sl1.setPath(serverPath1);
						sl1.setScanList(historyList);
						final ScanLocationItem sl2 = new ScanLocationItem();
						sl2.setHost(hostName);
						sl2.setPath(serverPath2);
						sl2.setScanList(historyList);
						final ScanLocationItem sl3 = new ScanLocationItem();
						sl3.setHost(hostName);
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

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(5000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		generatorInfo.setScanTargets(scanTargets);
		generatorInfo.setHostname(hostName);
		generatorInfo.setBeforeScanTime(startScanTime);
		generatorInfo.setAfterScanTime(endScanTime);

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("2.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		generator.generateHubReport(logger, null).getReport();
	}

}
