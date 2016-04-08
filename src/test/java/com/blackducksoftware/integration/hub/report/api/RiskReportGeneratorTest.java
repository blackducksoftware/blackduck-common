/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.report.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.scan.api.ScanHistoryItem;
import com.blackducksoftware.integration.hub.scan.api.ScanLocationItem;
import com.blackducksoftware.integration.hub.scan.status.ScanStatus;
import com.blackducksoftware.integration.hub.scan.status.ScanStatusToPoll;
import com.blackducksoftware.integration.hub.util.TestLogger;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RiskReportGeneratorTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void generateReportWithScanStatusFiles() throws Exception {
		HubIntRestService service = new HubIntRestService("FakeHubUrl");
		service = Mockito.spy(service);

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(20000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		final List<String> scanTargets = new ArrayList<String>();
		scanTargets.add("test");
		generatorInfo.setScanTargets(scanTargets);

		final File scanStatusDirectory = folder.newFolder();

		generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
		final File file = new File(scanStatusDirectory, "scanStatus.txt");

		final MetaInformation _meta = new MetaInformation(null, "link", null);
		final ScanStatusToPoll statusBuilding = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), _meta);
		final Gson gson = new GsonBuilder().create();

		final FileWriter writer = new FileWriter(file);
		writer.write(gson.toJson(statusBuilding));
		writer.close();

		final ScanStatusToPoll statusComplete = new ScanStatusToPoll(ScanStatus.COMPLETE.name(), _meta);
		Mockito.doReturn(statusComplete).when(service).checkScanStatus(Mockito.anyString());
		Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.any(ReleaseItem.class),
				Mockito.any(ReportFormatEnum.class));

		final List<MetaLink> links = new ArrayList<MetaLink>();
		final MetaLink linkItem = new MetaLink("content", "FakeContentLink");
		links.add(linkItem);
		final MetaInformation reportMeta = new MetaInformation(null, null, links);
		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, "Finished", null,
				reportMeta);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		final List<AggregateBomViewEntry> bomEntries = new ArrayList<AggregateBomViewEntry>();
		final VersionReport report = new VersionReport(null, bomEntries);

		Mockito.doReturn(report).when(service).getReportContent(Mockito.anyString());
		Mockito.doReturn(204).when(service).deleteHubReport(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("3.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		assertEquals(report, generator.generateHubReport(logger).getReport());
		final String output = logger.getOutputString();
		assertTrue(output, output.contains("Waiting for the bom to be updated with the scan results."));
		assertTrue(output, output.contains("The bom has been updated, generating the report."));
		assertTrue(output, output.contains("Finished retrieving the report."));
	}

	@Test
	public void generateReportWithScanStatusFilesNoContentLink() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("Could not find content link for the report at : ");

		HubIntRestService service = new HubIntRestService("FakeHubUrl");
		service = Mockito.spy(service);

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(20000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		final List<String> scanTargets = new ArrayList<String>();
		scanTargets.add("test");
		generatorInfo.setScanTargets(scanTargets);

		final File scanStatusDirectory = folder.newFolder();

		generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
		final File file = new File(scanStatusDirectory, "scanStatus.txt");

		final MetaInformation _meta = new MetaInformation(null, "link", null);
		final ScanStatusToPoll statusBuilding = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), _meta);
		final Gson gson = new GsonBuilder().create();

		final FileWriter writer = new FileWriter(file);
		writer.write(gson.toJson(statusBuilding));
		writer.close();

		final ScanStatusToPoll statusComplete = new ScanStatusToPoll(ScanStatus.COMPLETE.name(), _meta);
		Mockito.doReturn(statusComplete).when(service).checkScanStatus(Mockito.anyString());
		Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.any(ReleaseItem.class),
				Mockito.any(ReportFormatEnum.class));

		final List<MetaLink> links = new ArrayList<MetaLink>();
		final MetaInformation reportMeta = new MetaInformation(null, null, links);
		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, "Finished", null,
				reportMeta);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("3.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		generator.generateHubReport(logger).getReport();
	}

	@Test
	public void generateReportWithScanStatusFilesReportNotFinishedGenerating() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("The Report has not finished generating in : ");

		HubIntRestService service = new HubIntRestService("FakeHubUrl");
		service = Mockito.spy(service);

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(5000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		final List<String> scanTargets = new ArrayList<String>();
		scanTargets.add("test");
		generatorInfo.setScanTargets(scanTargets);

		final File scanStatusDirectory = folder.newFolder();

		generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
		final File file = new File(scanStatusDirectory, "scanStatus.txt");

		final MetaInformation _meta = new MetaInformation(null, "link", null);
		final ScanStatusToPoll statusBuilding = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), _meta);
		final Gson gson = new GsonBuilder().create();

		final FileWriter writer = new FileWriter(file);
		writer.write(gson.toJson(statusBuilding));
		writer.close();

		final ScanStatusToPoll statusComplete = new ScanStatusToPoll(ScanStatus.COMPLETE.name(), _meta);
		Mockito.doReturn(statusComplete).when(service).checkScanStatus(Mockito.anyString());
		Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.any(ReleaseItem.class),
				Mockito.any(ReportFormatEnum.class));

		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, null, null,
				null);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("3.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		generator.generateHubReport(logger).getReport();
	}

	@Test
	public void generateReportWithScanStatusFilesRiskNotUpToDate() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("The Bom has not finished updating from the scan within the specified wait time : ");

		HubIntRestService service = new HubIntRestService("FakeHubUrl");
		service = Mockito.spy(service);

		final HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
		generatorInfo.setMaximumWaitTime(5000);
		generatorInfo.setProject(new ProjectItem(null, null, null));
		generatorInfo.setVersion(new ReleaseItem(null, null, null, null, null));
		final List<String> scanTargets = new ArrayList<String>();
		scanTargets.add("test");
		generatorInfo.setScanTargets(scanTargets);

		final File scanStatusDirectory = folder.newFolder();

		generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
		final File file = new File(scanStatusDirectory, "scanStatus.txt");

		final MetaInformation _meta = new MetaInformation(null, "link", null);
		final ScanStatusToPoll statusBuilding = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), _meta);
		final Gson gson = new GsonBuilder().create();

		final FileWriter writer = new FileWriter(file);
		writer.write(gson.toJson(statusBuilding));
		writer.close();

		Mockito.doReturn(statusBuilding).when(service).checkScanStatus(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("3.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		generator.generateHubReport(logger).getReport();
	}

	@Test
	public void generateReportWithCodeLocations() throws Exception {
		HubIntRestService service = new HubIntRestService("FakeHubUrl");
		service = Mockito.spy(service);

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

		Mockito.when(service.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class))).then(new Answer<List<ScanLocationItem>>() {
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

				final List<ScanHistoryItem> historyList = new ArrayList<ScanHistoryItem>();
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

				final List<ScanLocationItem> items = new ArrayList<ScanLocationItem>();
				items.add(sl1);
				items.add(sl2);
				items.add(sl3);

				return items;
			}
		});

		final List<String> scanTargets = new ArrayList<String>();
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
				Mockito.any(ReportFormatEnum.class));

		final List<MetaLink> links = new ArrayList<MetaLink>();
		final MetaLink linkItem = new MetaLink("content", "FakeContentLink");
		links.add(linkItem);
		final MetaInformation reportMeta = new MetaInformation(null, null, links);
		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, "Finished", null,
				reportMeta);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		final List<AggregateBomViewEntry> RiskEntries = new ArrayList<AggregateBomViewEntry>();
		final VersionReport report = new VersionReport(null, RiskEntries);

		Mockito.doReturn(report).when(service).getReportContent(Mockito.anyString());
		Mockito.doReturn(204).when(service).deleteHubReport(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("2.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		assertEquals(report, generator.generateHubReport(logger).getReport());
		final String output = logger.getOutputString();
		assertTrue(output, output.contains("Waiting for the bom to be updated with the scan results."));
		assertTrue(output, output.contains("The bom has been updated, generating the report."));
		assertTrue(output, output.contains("Finished retrieving the report."));
	}

	@Test
	public void generateReportWithCodeLocationsNoContentLink() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("Could not find content link for the report at : ");

		HubIntRestService service = new HubIntRestService("FakeHubUrl");
		service = Mockito.spy(service);

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

		Mockito.when(service.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class))).then(new Answer<List<ScanLocationItem>>() {
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

				final List<ScanHistoryItem> historyList = new ArrayList<ScanHistoryItem>();
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

				final List<ScanLocationItem> items = new ArrayList<ScanLocationItem>();
				items.add(sl1);
				items.add(sl2);
				items.add(sl3);

				return items;
			}
		});

		final List<String> scanTargets = new ArrayList<String>();
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
				Mockito.any(ReportFormatEnum.class));

		final List<MetaLink> links = new ArrayList<MetaLink>();
		final MetaInformation reportMeta = new MetaInformation(null, null, links);
		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, "Finished", null,
				reportMeta);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		Mockito.doReturn(204).when(service).deleteHubReport(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("2.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		generator.generateHubReport(logger).getReport();
	}

	@Test
	public void generateReportWithCodeLocationsNotFinishedGeneratingReport() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("The Report has not finished generating in : ");

		HubIntRestService service = new HubIntRestService("FakeHubUrl");
		service = Mockito.spy(service);

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

		Mockito.when(service.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class))).then(new Answer<List<ScanLocationItem>>() {
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

				final List<ScanHistoryItem> historyList = new ArrayList<ScanHistoryItem>();
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

				final List<ScanLocationItem> items = new ArrayList<ScanLocationItem>();
				items.add(sl1);
				items.add(sl2);
				items.add(sl3);

				return items;
			}
		});

		final List<String> scanTargets = new ArrayList<String>();
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
				Mockito.any(ReportFormatEnum.class));

		final ReportInformationItem reportInfo = new ReportInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, null, null,
				null);

		Mockito.doReturn(reportInfo).when(service).getReportInformation(Mockito.anyString());

		generatorInfo.setService(service);

		final TestLogger logger = new TestLogger();
		final HubSupportHelper supportHelper = new HubSupportHelper();

		Mockito.doReturn("2.0.0").when(service).getHubVersion();
		supportHelper.checkHubSupport(service, logger);

		final RiskReportGenerator generator = new RiskReportGenerator(generatorInfo, supportHelper);
		generator.generateHubReport(logger).getReport();
	}

	@Test
	public void generateReportWithCodeLocationsRiskNotUpToDate() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("The Bom has not finished updating from the scan within the specified wait time : ");

		HubIntRestService service = new HubIntRestService("FakeHubUrl");
		service = Mockito.spy(service);

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

		Mockito.when(service.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class))).then(new Answer<List<ScanLocationItem>>() {
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

				final List<ScanHistoryItem> historyList = new ArrayList<ScanHistoryItem>();
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

				final List<ScanLocationItem> items = new ArrayList<ScanLocationItem>();
				items.add(sl1);
				items.add(sl2);
				items.add(sl3);

				return items;
			}
		});

		final List<String> scanTargets = new ArrayList<String>();
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
		generator.generateHubReport(logger).getReport();
	}

}
