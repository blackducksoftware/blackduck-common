package com.blackducksoftware.integration.hub;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.response.ReportMetaInformationItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanHistoryItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanLocationItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanStatus;

public class HubEventPollingTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

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
