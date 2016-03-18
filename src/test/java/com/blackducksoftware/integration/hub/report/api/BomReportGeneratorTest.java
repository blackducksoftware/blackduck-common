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
import com.blackducksoftware.integration.hub.response.ReportFormatEnum;
import com.blackducksoftware.integration.hub.response.ReportMetaInformationItem;
import com.blackducksoftware.integration.hub.response.ReportMetaInformationItem.ReportMetaItem;
import com.blackducksoftware.integration.hub.response.ReportMetaInformationItem.ReportMetaLinkItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanHistoryItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanLocationItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanStatus;
import com.blackducksoftware.integration.hub.scan.status.ScanStatusMeta;
import com.blackducksoftware.integration.hub.scan.status.ScanStatusToPoll;
import com.blackducksoftware.integration.hub.util.TestLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BomReportGeneratorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void generateReportWithScanStatusFiles() throws Exception {
        HubIntRestService service = new HubIntRestService("FakeHubUrl");
        service = Mockito.spy(service);

        HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
        generatorInfo.setMaximumWaitTime(20000);
        generatorInfo.setProjectId("FakeProjectId");
        generatorInfo.setVersionId("FakeVersionId");
        List<String> scanTargets = new ArrayList<String>();
        scanTargets.add("test");
        generatorInfo.setScanTargets(scanTargets);

        File scanStatusDirectory = folder.newFolder();

        generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
        File file = new File(scanStatusDirectory, "scanStatus.txt");

        ScanStatusMeta _meta = new ScanStatusMeta("FakeLink");
        ScanStatusToPoll statusBuilding = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), _meta);
        Gson gson = new GsonBuilder().create();

        FileWriter writer = new FileWriter(file);
        writer.write(gson.toJson(statusBuilding));
        writer.close();

        ScanStatusToPoll statusComplete = new ScanStatusToPoll(ScanStatus.COMPLETE.name(), _meta);
        Mockito.doReturn(statusComplete).when(service).checkScanStatus(Mockito.anyString());
        Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.anyString(), Mockito.any(ReportFormatEnum.class));

        List<ReportMetaLinkItem> links = new ArrayList<ReportMetaLinkItem>();
        ReportMetaLinkItem linkItem = new ReportMetaLinkItem("content", "FakeContentLink");
        links.add(linkItem);
        ReportMetaItem reportMeta = new ReportMetaItem(null, null, links);
        ReportMetaInformationItem reportInfo = new ReportMetaInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, "Finished", null,
                reportMeta);

        Mockito.doReturn(reportInfo).when(service).getReportLinks(Mockito.anyString());

        List<AggregateBomViewEntry> bomEntries = new ArrayList<AggregateBomViewEntry>();
        VersionReport report = new VersionReport(null, bomEntries);

        Mockito.doReturn(report).when(service).getReportContent(Mockito.anyString());
        Mockito.doReturn(204).when(service).deleteHubReport(Mockito.anyString(), Mockito.anyString());

        generatorInfo.setService(service);

        TestLogger logger = new TestLogger();
        HubSupportHelper supportHelper = new HubSupportHelper();

        Mockito.doReturn("3.0.0").when(service).getHubVersion();
        supportHelper.checkHubSupport(service, logger);

        BomReportGenerator generator = new BomReportGenerator(generatorInfo, supportHelper);
        assertEquals(report, generator.generateHubReport(logger).getReport());
        String output = logger.getOutputString();
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

        HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
        generatorInfo.setMaximumWaitTime(20000);
        generatorInfo.setProjectId("FakeProjectId");
        generatorInfo.setVersionId("FakeVersionId");
        List<String> scanTargets = new ArrayList<String>();
        scanTargets.add("test");
        generatorInfo.setScanTargets(scanTargets);

        File scanStatusDirectory = folder.newFolder();

        generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
        File file = new File(scanStatusDirectory, "scanStatus.txt");

        ScanStatusMeta _meta = new ScanStatusMeta("FakeLink");
        ScanStatusToPoll statusBuilding = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), _meta);
        Gson gson = new GsonBuilder().create();

        FileWriter writer = new FileWriter(file);
        writer.write(gson.toJson(statusBuilding));
        writer.close();

        ScanStatusToPoll statusComplete = new ScanStatusToPoll(ScanStatus.COMPLETE.name(), _meta);
        Mockito.doReturn(statusComplete).when(service).checkScanStatus(Mockito.anyString());
        Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.anyString(), Mockito.any(ReportFormatEnum.class));

        List<ReportMetaLinkItem> links = new ArrayList<ReportMetaLinkItem>();
        ReportMetaItem reportMeta = new ReportMetaItem(null, null, links);
        ReportMetaInformationItem reportInfo = new ReportMetaInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, "Finished", null,
                reportMeta);

        Mockito.doReturn(reportInfo).when(service).getReportLinks(Mockito.anyString());

        generatorInfo.setService(service);

        TestLogger logger = new TestLogger();
        HubSupportHelper supportHelper = new HubSupportHelper();

        Mockito.doReturn("3.0.0").when(service).getHubVersion();
        supportHelper.checkHubSupport(service, logger);

        BomReportGenerator generator = new BomReportGenerator(generatorInfo, supportHelper);
        generator.generateHubReport(logger).getReport();
    }

    @Test
    public void generateReportWithScanStatusFilesReportNotFinishedGenerating() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The Report has not finished generating in : ");

        HubIntRestService service = new HubIntRestService("FakeHubUrl");
        service = Mockito.spy(service);

        HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
        generatorInfo.setMaximumWaitTime(5000);
        generatorInfo.setProjectId("FakeProjectId");
        generatorInfo.setVersionId("FakeVersionId");
        List<String> scanTargets = new ArrayList<String>();
        scanTargets.add("test");
        generatorInfo.setScanTargets(scanTargets);

        File scanStatusDirectory = folder.newFolder();

        generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
        File file = new File(scanStatusDirectory, "scanStatus.txt");

        ScanStatusMeta _meta = new ScanStatusMeta("FakeLink");
        ScanStatusToPoll statusBuilding = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), _meta);
        Gson gson = new GsonBuilder().create();

        FileWriter writer = new FileWriter(file);
        writer.write(gson.toJson(statusBuilding));
        writer.close();

        ScanStatusToPoll statusComplete = new ScanStatusToPoll(ScanStatus.COMPLETE.name(), _meta);
        Mockito.doReturn(statusComplete).when(service).checkScanStatus(Mockito.anyString());
        Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.anyString(), Mockito.any(ReportFormatEnum.class));

        ReportMetaInformationItem reportInfo = new ReportMetaInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, null, null,
                null);

        Mockito.doReturn(reportInfo).when(service).getReportLinks(Mockito.anyString());

        generatorInfo.setService(service);

        TestLogger logger = new TestLogger();
        HubSupportHelper supportHelper = new HubSupportHelper();

        Mockito.doReturn("3.0.0").when(service).getHubVersion();
        supportHelper.checkHubSupport(service, logger);

        BomReportGenerator generator = new BomReportGenerator(generatorInfo, supportHelper);
        generator.generateHubReport(logger).getReport();
    }

    @Test
    public void generateReportWithScanStatusFilesBomNotUpToDate() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The Bom has not finished updating from the scan within the specified wait time : ");

        HubIntRestService service = new HubIntRestService("FakeHubUrl");
        service = Mockito.spy(service);

        HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
        generatorInfo.setMaximumWaitTime(5000);
        generatorInfo.setProjectId("FakeProjectId");
        generatorInfo.setVersionId("FakeVersionId");
        List<String> scanTargets = new ArrayList<String>();
        scanTargets.add("test");
        generatorInfo.setScanTargets(scanTargets);

        File scanStatusDirectory = folder.newFolder();

        generatorInfo.setScanStatusDirectory(scanStatusDirectory.getCanonicalPath());
        File file = new File(scanStatusDirectory, "scanStatus.txt");

        ScanStatusMeta _meta = new ScanStatusMeta("FakeLink");
        ScanStatusToPoll statusBuilding = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), _meta);
        Gson gson = new GsonBuilder().create();

        FileWriter writer = new FileWriter(file);
        writer.write(gson.toJson(statusBuilding));
        writer.close();

        Mockito.doReturn(statusBuilding).when(service).checkScanStatus(Mockito.anyString());

        generatorInfo.setService(service);

        TestLogger logger = new TestLogger();
        HubSupportHelper supportHelper = new HubSupportHelper();

        Mockito.doReturn("3.0.0").when(service).getHubVersion();
        supportHelper.checkHubSupport(service, logger);

        BomReportGenerator generator = new BomReportGenerator(generatorInfo, supportHelper);
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
            public List<ScanLocationItem> answer(InvocationOnMock invocation) throws Throwable {
                ScanHistoryItem historyBeforeScanTime = new ScanHistoryItem();
                historyBeforeScanTime.setCreatedOn(beforeScanTime.toString());
                historyBeforeScanTime.setStatus(ScanStatus.ERROR);

                ScanHistoryItem historyInScanTime = new ScanHistoryItem();
                historyInScanTime.setCreatedOn(inScanTime.toString());
                historyInScanTime.setStatus(ScanStatus.COMPLETE);

                ScanHistoryItem historyAfterScanTime = new ScanHistoryItem();
                historyAfterScanTime.setCreatedOn(afterScanTime.toString());
                historyAfterScanTime.setStatus(ScanStatus.MATCHING);

                List<ScanHistoryItem> historyList = new ArrayList<ScanHistoryItem>();
                historyList.add(historyBeforeScanTime);
                historyList.add(historyInScanTime);
                historyList.add(historyAfterScanTime);

                ScanLocationItem sl1 = new ScanLocationItem();
                sl1.setHost(hostName);
                sl1.setPath(serverPath1);
                sl1.setScanList(historyList);
                ScanLocationItem sl2 = new ScanLocationItem();
                sl2.setHost(hostName);
                sl2.setPath(serverPath2);
                sl2.setScanList(historyList);
                ScanLocationItem sl3 = new ScanLocationItem();
                sl3.setHost(hostName);
                sl3.setPath(serverPath3);
                sl3.setScanList(historyList);

                List<ScanLocationItem> items = new ArrayList<ScanLocationItem>();
                items.add(sl1);
                items.add(sl2);
                items.add(sl3);

                return items;
            }
        });

        List<String> scanTargets = new ArrayList<String>();
        scanTargets.add("Test/Fake/Path/Child");
        scanTargets.add("Test\\Fake\\File");

        HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
        generatorInfo.setMaximumWaitTime(20000);
        generatorInfo.setProjectId("FakeProjectId");
        generatorInfo.setVersionId("FakeVersionId");
        generatorInfo.setScanTargets(scanTargets);
        generatorInfo.setHostname(hostName);
        generatorInfo.setBeforeScanTime(startScanTime);
        generatorInfo.setAfterScanTime(endScanTime);

        Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.anyString(), Mockito.any(ReportFormatEnum.class));

        List<ReportMetaLinkItem> links = new ArrayList<ReportMetaLinkItem>();
        ReportMetaLinkItem linkItem = new ReportMetaLinkItem("content", "FakeContentLink");
        links.add(linkItem);
        ReportMetaItem reportMeta = new ReportMetaItem(null, null, links);
        ReportMetaInformationItem reportInfo = new ReportMetaInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, "Finished", null,
                reportMeta);

        Mockito.doReturn(reportInfo).when(service).getReportLinks(Mockito.anyString());

        List<AggregateBomViewEntry> bomEntries = new ArrayList<AggregateBomViewEntry>();
        VersionReport report = new VersionReport(null, bomEntries);

        Mockito.doReturn(report).when(service).getReportContent(Mockito.anyString());
        Mockito.doReturn(204).when(service).deleteHubReport(Mockito.anyString(), Mockito.anyString());

        generatorInfo.setService(service);

        TestLogger logger = new TestLogger();
        HubSupportHelper supportHelper = new HubSupportHelper();

        Mockito.doReturn("2.0.0").when(service).getHubVersion();
        supportHelper.checkHubSupport(service, logger);

        BomReportGenerator generator = new BomReportGenerator(generatorInfo, supportHelper);
        assertEquals(report, generator.generateHubReport(logger).getReport());
        String output = logger.getOutputString();
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
            public List<ScanLocationItem> answer(InvocationOnMock invocation) throws Throwable {
                ScanHistoryItem historyBeforeScanTime = new ScanHistoryItem();
                historyBeforeScanTime.setCreatedOn(beforeScanTime.toString());
                historyBeforeScanTime.setStatus(ScanStatus.ERROR);

                ScanHistoryItem historyInScanTime = new ScanHistoryItem();
                historyInScanTime.setCreatedOn(inScanTime.toString());
                historyInScanTime.setStatus(ScanStatus.COMPLETE);

                ScanHistoryItem historyAfterScanTime = new ScanHistoryItem();
                historyAfterScanTime.setCreatedOn(afterScanTime.toString());
                historyAfterScanTime.setStatus(ScanStatus.MATCHING);

                List<ScanHistoryItem> historyList = new ArrayList<ScanHistoryItem>();
                historyList.add(historyBeforeScanTime);
                historyList.add(historyInScanTime);
                historyList.add(historyAfterScanTime);

                ScanLocationItem sl1 = new ScanLocationItem();
                sl1.setHost(hostName);
                sl1.setPath(serverPath1);
                sl1.setScanList(historyList);
                ScanLocationItem sl2 = new ScanLocationItem();
                sl2.setHost(hostName);
                sl2.setPath(serverPath2);
                sl2.setScanList(historyList);
                ScanLocationItem sl3 = new ScanLocationItem();
                sl3.setHost(hostName);
                sl3.setPath(serverPath3);
                sl3.setScanList(historyList);

                List<ScanLocationItem> items = new ArrayList<ScanLocationItem>();
                items.add(sl1);
                items.add(sl2);
                items.add(sl3);

                return items;
            }
        });

        List<String> scanTargets = new ArrayList<String>();
        scanTargets.add("Test/Fake/Path/Child");
        scanTargets.add("Test\\Fake\\File");

        HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
        generatorInfo.setMaximumWaitTime(20000);
        generatorInfo.setProjectId("FakeProjectId");
        generatorInfo.setVersionId("FakeVersionId");
        generatorInfo.setScanTargets(scanTargets);
        generatorInfo.setHostname(hostName);
        generatorInfo.setBeforeScanTime(startScanTime);
        generatorInfo.setAfterScanTime(endScanTime);

        Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.anyString(), Mockito.any(ReportFormatEnum.class));

        List<ReportMetaLinkItem> links = new ArrayList<ReportMetaLinkItem>();
        ReportMetaItem reportMeta = new ReportMetaItem(null, null, links);
        ReportMetaInformationItem reportInfo = new ReportMetaInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, "Finished", null,
                reportMeta);

        Mockito.doReturn(reportInfo).when(service).getReportLinks(Mockito.anyString());

        Mockito.doReturn(204).when(service).deleteHubReport(Mockito.anyString(), Mockito.anyString());

        generatorInfo.setService(service);

        TestLogger logger = new TestLogger();
        HubSupportHelper supportHelper = new HubSupportHelper();

        Mockito.doReturn("2.0.0").when(service).getHubVersion();
        supportHelper.checkHubSupport(service, logger);

        BomReportGenerator generator = new BomReportGenerator(generatorInfo, supportHelper);
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
            public List<ScanLocationItem> answer(InvocationOnMock invocation) throws Throwable {
                ScanHistoryItem historyBeforeScanTime = new ScanHistoryItem();
                historyBeforeScanTime.setCreatedOn(beforeScanTime.toString());
                historyBeforeScanTime.setStatus(ScanStatus.ERROR);

                ScanHistoryItem historyInScanTime = new ScanHistoryItem();
                historyInScanTime.setCreatedOn(inScanTime.toString());
                historyInScanTime.setStatus(ScanStatus.COMPLETE);

                ScanHistoryItem historyAfterScanTime = new ScanHistoryItem();
                historyAfterScanTime.setCreatedOn(afterScanTime.toString());
                historyAfterScanTime.setStatus(ScanStatus.MATCHING);

                List<ScanHistoryItem> historyList = new ArrayList<ScanHistoryItem>();
                historyList.add(historyBeforeScanTime);
                historyList.add(historyInScanTime);
                historyList.add(historyAfterScanTime);

                ScanLocationItem sl1 = new ScanLocationItem();
                sl1.setHost(hostName);
                sl1.setPath(serverPath1);
                sl1.setScanList(historyList);
                ScanLocationItem sl2 = new ScanLocationItem();
                sl2.setHost(hostName);
                sl2.setPath(serverPath2);
                sl2.setScanList(historyList);
                ScanLocationItem sl3 = new ScanLocationItem();
                sl3.setHost(hostName);
                sl3.setPath(serverPath3);
                sl3.setScanList(historyList);

                List<ScanLocationItem> items = new ArrayList<ScanLocationItem>();
                items.add(sl1);
                items.add(sl2);
                items.add(sl3);

                return items;
            }
        });

        List<String> scanTargets = new ArrayList<String>();
        scanTargets.add("Test/Fake/Path/Child");
        scanTargets.add("Test\\Fake\\File");

        HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
        generatorInfo.setMaximumWaitTime(5000);
        generatorInfo.setProjectId("FakeProjectId");
        generatorInfo.setVersionId("FakeVersionId");
        generatorInfo.setScanTargets(scanTargets);
        generatorInfo.setHostname(hostName);
        generatorInfo.setBeforeScanTime(startScanTime);
        generatorInfo.setAfterScanTime(endScanTime);

        Mockito.doReturn("FakeReportUrl").when(service).generateHubReport(Mockito.anyString(), Mockito.any(ReportFormatEnum.class));

        ReportMetaInformationItem reportInfo = new ReportMetaInformationItem(ReportFormatEnum.JSON.name(), null, null, 0, null, null, null, null,
                null);

        Mockito.doReturn(reportInfo).when(service).getReportLinks(Mockito.anyString());

        generatorInfo.setService(service);

        TestLogger logger = new TestLogger();
        HubSupportHelper supportHelper = new HubSupportHelper();

        Mockito.doReturn("2.0.0").when(service).getHubVersion();
        supportHelper.checkHubSupport(service, logger);

        BomReportGenerator generator = new BomReportGenerator(generatorInfo, supportHelper);
        generator.generateHubReport(logger).getReport();
    }

    @Test
    public void generateReportWithCodeLocationsBomNotUpToDate() throws Exception {
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
            public List<ScanLocationItem> answer(InvocationOnMock invocation) throws Throwable {

                ScanHistoryItem historyBeforeScanTime = new ScanHistoryItem();
                historyBeforeScanTime.setCreatedOn(beforeScanTime.toString());
                historyBeforeScanTime.setStatus(ScanStatus.ERROR);

                ScanHistoryItem historyInScanTime = new ScanHistoryItem();
                historyInScanTime.setCreatedOn(inScanTime.toString());
                historyInScanTime.setStatus(ScanStatus.BUILDING_BOM);

                ScanHistoryItem historyAfterScanTime = new ScanHistoryItem();
                historyAfterScanTime.setCreatedOn(afterScanTime.toString());
                historyAfterScanTime.setStatus(ScanStatus.MATCHING);

                List<ScanHistoryItem> historyList = new ArrayList<ScanHistoryItem>();
                historyList.add(historyBeforeScanTime);
                historyList.add(historyInScanTime);
                historyList.add(historyAfterScanTime);

                ScanLocationItem sl1 = new ScanLocationItem();
                sl1.setHost(hostName);
                sl1.setPath(serverPath1);
                sl1.setScanList(historyList);
                ScanLocationItem sl2 = new ScanLocationItem();
                sl2.setHost(hostName);
                sl2.setPath(serverPath2);
                sl2.setScanList(historyList);
                ScanLocationItem sl3 = new ScanLocationItem();
                sl3.setHost(hostName);
                sl3.setPath(serverPath3);
                sl3.setScanList(historyList);

                List<ScanLocationItem> items = new ArrayList<ScanLocationItem>();
                items.add(sl1);
                items.add(sl2);
                items.add(sl3);

                return items;
            }
        });

        List<String> scanTargets = new ArrayList<String>();
        scanTargets.add("Test/Fake/Path/Child");
        scanTargets.add("Test\\Fake\\File");

        HubReportGenerationInfo generatorInfo = new HubReportGenerationInfo();
        generatorInfo.setMaximumWaitTime(5000);
        generatorInfo.setProjectId("FakeProjectId");
        generatorInfo.setVersionId("FakeVersionId");
        generatorInfo.setScanTargets(scanTargets);
        generatorInfo.setHostname(hostName);
        generatorInfo.setBeforeScanTime(startScanTime);
        generatorInfo.setAfterScanTime(endScanTime);

        generatorInfo.setService(service);

        TestLogger logger = new TestLogger();
        HubSupportHelper supportHelper = new HubSupportHelper();

        Mockito.doReturn("2.0.0").when(service).getHubVersion();
        supportHelper.checkHubSupport(service, logger);

        BomReportGenerator generator = new BomReportGenerator(generatorInfo, supportHelper);
        generator.generateHubReport(logger).getReport();
    }

}
