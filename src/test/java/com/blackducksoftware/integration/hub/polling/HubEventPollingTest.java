package com.blackducksoftware.integration.hub.polling;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.polling.HubEventPolling;
import com.blackducksoftware.integration.hub.response.ReportMetaInformationItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanHistoryItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanLocationItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanStatus;
import com.blackducksoftware.integration.hub.scan.status.ScanStatusMeta;
import com.blackducksoftware.integration.hub.scan.status.ScanStatusToPoll;
import com.blackducksoftware.integration.hub.util.TestLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HubEventPollingTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private void writeScanStatusToFile(ScanStatusToPoll status, File file) throws IOException {
        Gson gson = new GsonBuilder().create();

        String stringStatus = gson.toJson(status);

        FileWriter writer = new FileWriter(file);
        writer.write(stringStatus);
        writer.close();
    }

    @Test
    public void testIsBomUpToDateStatusFiles() throws Exception {
        ScanStatusMeta meta = new ScanStatusMeta("link");
        ScanStatusToPoll status1 = new ScanStatusToPoll(ScanStatus.REQUESTED_MATCH_JOB.name(), meta);
        ScanStatusToPoll status2 = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), meta);
        ScanStatusToPoll status3 = new ScanStatusToPoll(ScanStatus.SCANNING.name(), meta);
        File scanStatusDir = folder.newFolder();
        File statusFile1 = new File(scanStatusDir, "status1.txt");
        statusFile1.createNewFile();
        File statusFile2 = new File(scanStatusDir, "status2.txt");
        statusFile2.createNewFile();
        File statusFile3 = new File(scanStatusDir, "status3.txt");
        statusFile3.createNewFile();
        writeScanStatusToFile(status1, statusFile1);
        writeScanStatusToFile(status2, statusFile2);
        writeScanStatusToFile(status3, statusFile3);

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        Mockito.when(restService.checkScanStatus(Mockito.anyString())).then(new Answer<ScanStatusToPoll>() {
            @Override
            public ScanStatusToPoll answer(InvocationOnMock invocation) throws Throwable {
                ScanStatusMeta meta = new ScanStatusMeta("link");
                ScanStatusToPoll status = new ScanStatusToPoll(ScanStatus.COMPLETE.name(), meta);
                return status;
            }
        });
        HubEventPolling eventPoller = new HubEventPolling(restService);
        TestLogger logger = new TestLogger();
        assertTrue(eventPoller.isBomUpToDate(3, scanStatusDir.getCanonicalPath(), 20000, logger));

        assertTrue(logger.getOutputString(),
                logger.getOutputString().contains("Checking the directory : " + scanStatusDir.getCanonicalPath() + " for the scan status's."));
        assertTrue(logger.getOutputString(),
                logger.getOutputString().contains("Cleaning up the scan staus files at : " + scanStatusDir.getCanonicalPath()));
        assertTrue(!statusFile1.exists());
        assertTrue(!statusFile2.exists());
        assertTrue(!statusFile3.exists());
        assertTrue(!scanStatusDir.exists());
    }

    @Test
    public void testIsBomUpToDateStatusFilesNotUpToDate() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The Bom has not finished updating from the scan within the specified wait time :");

        ScanStatusMeta meta = new ScanStatusMeta("link");
        ScanStatusToPoll status1 = new ScanStatusToPoll(ScanStatus.REQUESTED_MATCH_JOB.name(), meta);
        ScanStatusToPoll status2 = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), meta);
        ScanStatusToPoll status3 = new ScanStatusToPoll(ScanStatus.SCANNING.name(), meta);
        File scanStatusDir = folder.newFolder();
        File statusFile1 = new File(scanStatusDir, "status1.txt");
        statusFile1.createNewFile();
        File statusFile2 = new File(scanStatusDir, "status2.txt");
        statusFile2.createNewFile();
        File statusFile3 = new File(scanStatusDir, "status3.txt");
        statusFile3.createNewFile();
        writeScanStatusToFile(status1, statusFile1);
        writeScanStatusToFile(status2, statusFile2);
        writeScanStatusToFile(status3, statusFile3);

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        Mockito.when(restService.checkScanStatus(Mockito.anyString())).then(new Answer<ScanStatusToPoll>() {
            @Override
            public ScanStatusToPoll answer(InvocationOnMock invocation) throws Throwable {
                ScanStatusMeta meta = new ScanStatusMeta("link");
                ScanStatusToPoll status = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), meta);
                return status;
            }
        });
        HubEventPolling eventPoller = new HubEventPolling(restService);
        TestLogger logger = new TestLogger();
        eventPoller.isBomUpToDate(3, scanStatusDir.getCanonicalPath(), 15000, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesError() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("There was a problem with one of the scans. Error Status : ");

        ScanStatusMeta meta = new ScanStatusMeta("link");
        ScanStatusToPoll status1 = new ScanStatusToPoll(ScanStatus.REQUESTED_MATCH_JOB.name(), meta);
        ScanStatusToPoll status2 = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), meta);
        ScanStatusToPoll status3 = new ScanStatusToPoll(ScanStatus.SCANNING.name(), meta);
        File scanStatusDir = folder.newFolder();
        File statusFile1 = new File(scanStatusDir, "status1.txt");
        statusFile1.createNewFile();
        File statusFile2 = new File(scanStatusDir, "status2.txt");
        statusFile2.createNewFile();
        File statusFile3 = new File(scanStatusDir, "status3.txt");
        statusFile3.createNewFile();
        writeScanStatusToFile(status1, statusFile1);
        writeScanStatusToFile(status2, statusFile2);
        writeScanStatusToFile(status3, statusFile3);

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        Mockito.when(restService.checkScanStatus(Mockito.anyString())).then(new Answer<ScanStatusToPoll>() {
            @Override
            public ScanStatusToPoll answer(InvocationOnMock invocation) throws Throwable {
                ScanStatusMeta meta = new ScanStatusMeta("link");
                ScanStatusToPoll status = new ScanStatusToPoll(ScanStatus.ERROR.name(), meta);
                return status;
            }
        });
        HubEventPolling eventPoller = new HubEventPolling(restService);
        TestLogger logger = new TestLogger();
        eventPoller.isBomUpToDate(3, scanStatusDir.getCanonicalPath(), 20000, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesIncorrectFileContent() throws Exception {
        ScanStatusMeta meta = new ScanStatusMeta("link");
        File scanStatusDir = folder.newFolder();
        File statusFile1 = new File(scanStatusDir, "status1.txt");
        statusFile1.createNewFile();
        Gson gson = new GsonBuilder().create();

        String stringStatus = gson.toJson(meta);

        FileWriter writer = new FileWriter(statusFile1);
        writer.write(stringStatus);
        writer.close();

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        HubEventPolling eventPoller = new HubEventPolling(restService);
        TestLogger logger = new TestLogger();
        try {
            eventPoller.isBomUpToDate(1, scanStatusDir.getCanonicalPath(), 1000, logger);
        } catch (Exception e) {
            assertTrue(e instanceof HubIntegrationException);
            assertTrue(e.getMessage(), e.getMessage().contains("The scan status file : " + statusFile1.getCanonicalPath()
                    + " does not contain valid scan status json."));
        }
        assertTrue(statusFile1.exists());
        assertTrue(scanStatusDir.exists());
    }

    @Test
    public void testIsBomUpToDateStatusFilesNumberMisMatch() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("There were " + 5 + " scans configured and we found " + 3 + " status files.");

        ScanStatusMeta meta = new ScanStatusMeta("link");
        ScanStatusToPoll status1 = new ScanStatusToPoll(ScanStatus.REQUESTED_MATCH_JOB.name(), meta);
        ScanStatusToPoll status2 = new ScanStatusToPoll(ScanStatus.BUILDING_BOM.name(), meta);
        ScanStatusToPoll status3 = new ScanStatusToPoll(ScanStatus.SCANNING.name(), meta);
        File scanStatusDir = folder.newFolder();
        File statusFile1 = new File(scanStatusDir, "status1.txt");
        statusFile1.createNewFile();
        File statusFile2 = new File(scanStatusDir, "status2.txt");
        statusFile2.createNewFile();
        File statusFile3 = new File(scanStatusDir, "status3.txt");
        statusFile3.createNewFile();
        writeScanStatusToFile(status1, statusFile1);
        writeScanStatusToFile(status2, statusFile2);
        writeScanStatusToFile(status3, statusFile3);

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        HubEventPolling eventPoller = new HubEventPolling(restService);
        TestLogger logger = new TestLogger();
        eventPoller.isBomUpToDate(5, scanStatusDir.getCanonicalPath(), 20000, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesNoScanStatusFiles() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("Can not find the scan status files in the directory provided.");

        File scanStatusDir = folder.newFolder();

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        HubEventPolling eventPoller = new HubEventPolling(restService);
        TestLogger logger = new TestLogger();
        eventPoller.isBomUpToDate(0, scanStatusDir.getCanonicalPath(), 1000, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesNotADirectory() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The scan status directory provided is not a directory.");

        File scanStatusDir = folder.newFile();

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        HubEventPolling eventPoller = new HubEventPolling(restService);
        TestLogger logger = new TestLogger();
        eventPoller.isBomUpToDate(0, scanStatusDir.getCanonicalPath(), 1000, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesDirectoryDNE() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The scan status directory does not exist.");

        File scanStatusDir = new File("/ASSERTFAKE/DOES NOT EXIST/ANYWHERE");

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        HubEventPolling eventPoller = new HubEventPolling(restService);
        TestLogger logger = new TestLogger();
        eventPoller.isBomUpToDate(0, scanStatusDir.getCanonicalPath(), 1000, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesDirectoryNotProvided() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The scan status directory must be a non empty value.");

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        HubEventPolling eventPoller = new HubEventPolling(restService);
        TestLogger logger = new TestLogger();
        eventPoller.isBomUpToDate(0, "", 1000, logger);
    }

    @Test
    public void testIsBomUpToDateStatusFilesDirectoryNull() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The scan status directory must be a non empty value.");

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);
        HubEventPolling eventPoller = new HubEventPolling(restService);
        TestLogger logger = new TestLogger();
        eventPoller.isBomUpToDate(0, null, 1000, logger);
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

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        Mockito.when(restService.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class))).then(new Answer<List<ScanLocationItem>>() {
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
                sl1.setHost(fakeHost);
                sl1.setPath(serverPath1);
                sl1.setScanList(historyList);
                ScanLocationItem sl2 = new ScanLocationItem();
                sl2.setHost(fakeHost);
                sl2.setPath(serverPath2);
                sl2.setScanList(historyList);
                ScanLocationItem sl3 = new ScanLocationItem();
                sl3.setHost(fakeHost);
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
        HubEventPolling eventPoller = new HubEventPolling(restService);

        assertTrue(eventPoller.isBomUpToDate(startScanTime, endScanTime, fakeHost, scanTargets, 5000));
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

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        Mockito.when(restService.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class))).then(new Answer<List<ScanLocationItem>>() {
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
                sl1.setHost(fakeHost);
                sl1.setPath(serverPath1);
                sl1.setScanList(historyList);
                ScanLocationItem sl2 = new ScanLocationItem();
                sl2.setHost(fakeHost);
                sl2.setPath(serverPath2);
                sl2.setScanList(historyList);
                ScanLocationItem sl3 = new ScanLocationItem();
                sl3.setHost(fakeHost);
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
        HubEventPolling eventPoller = new HubEventPolling(restService);
        eventPoller.isBomUpToDate(startScanTime, endScanTime, fakeHost, scanTargets, 1000);
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

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        Mockito.when(restService.getScanLocations(Mockito.anyString(), Mockito.anyListOf(String.class))).then(new Answer<List<ScanLocationItem>>() {
            @Override
            public List<ScanLocationItem> answer(InvocationOnMock invocation) throws Throwable {

                ScanHistoryItem historyBeforeScanTime = new ScanHistoryItem();
                historyBeforeScanTime.setCreatedOn(beforeScanTime.toString());
                historyBeforeScanTime.setStatus(ScanStatus.ERROR);

                ScanHistoryItem historyInScanTime = new ScanHistoryItem();
                historyInScanTime.setCreatedOn(inScanTime.toString());
                historyInScanTime.setStatus(ScanStatus.ERROR);

                ScanHistoryItem historyAfterScanTime = new ScanHistoryItem();
                historyAfterScanTime.setCreatedOn(afterScanTime.toString());
                historyAfterScanTime.setStatus(ScanStatus.MATCHING);

                List<ScanHistoryItem> historyList = new ArrayList<ScanHistoryItem>();
                historyList.add(historyBeforeScanTime);
                historyList.add(historyInScanTime);
                historyList.add(historyAfterScanTime);

                ScanLocationItem sl1 = new ScanLocationItem();
                sl1.setHost(fakeHost);
                sl1.setPath(serverPath1);
                sl1.setScanList(historyList);
                ScanLocationItem sl2 = new ScanLocationItem();
                sl2.setHost(fakeHost);
                sl2.setPath(serverPath2);
                sl2.setScanList(historyList);
                ScanLocationItem sl3 = new ScanLocationItem();
                sl3.setHost(fakeHost);
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
        HubEventPolling eventPoller = new HubEventPolling(restService);
        eventPoller.isBomUpToDate(startScanTime, endScanTime, fakeHost, scanTargets, 5000);
    }

    @Test
    public void testIsReportDoneGeneratingNotDone() throws Exception {
        exception.expect(HubIntegrationException.class);
        exception.expectMessage("The Report has not finished generating in : ");

        // 5 seconds
        final long maximumWait = 1000 * 5;

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        Mockito.when(restService.getReportLinks(Mockito.anyString())).then(new Answer<ReportMetaInformationItem>() {
            @Override
            public ReportMetaInformationItem answer(InvocationOnMock invocation) throws Throwable {
                return new ReportMetaInformationItem(null, null, null, 0, null, null, null, null, null);
            }
        });
        HubEventPolling eventPoller = new HubEventPolling(restService);
        eventPoller.isReportFinishedGenerating("", maximumWait);
    }

    @Test
    public void testIsReportDoneGeneratingDone() throws Exception {
        // 5 seconds
        final long maximumWait = 1000 * 5;

        HubIntRestService restService = Mockito.mock(HubIntRestService.class);

        Mockito.when(restService.getReportLinks(Mockito.anyString())).then(new Answer<ReportMetaInformationItem>() {
            @Override
            public ReportMetaInformationItem answer(InvocationOnMock invocation) throws Throwable {
                return new ReportMetaInformationItem(null, null, null, 0, null, null, "test", null, null);
            }
        });
        HubEventPolling eventPoller = new HubEventPolling(restService);
        eventPoller.isReportFinishedGenerating("", maximumWait);
    }

}
