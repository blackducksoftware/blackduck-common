package com.blackducksoftware.integration.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.blackducksoftware.integration.hub.api.VersionComparison;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.util.TestLogger;

public class HubSupportHelperTest {

    private HubIntRestService getMockedService(String returnVersion) throws Exception {
        HubIntRestService service = Mockito.mock(HubIntRestService.class);
        Mockito.when(service.getHubVersion()).thenReturn(returnVersion);
        return service;
    }

    private HubIntRestService getMockedServiceWithFallBack(String returnVersion, boolean compareSupported) throws Exception {
        HubIntRestService service = getMockedService(returnVersion);
        VersionComparison compare;
        if (compareSupported) {
            compare = new VersionComparison("", "", -1, "");
        } else {
            compare = new VersionComparison("", "", 1, "");
        }
        Mockito.when(service.compareWithHubVersion(Mockito.anyString())).thenReturn(compare);
        return service;
    }

    @Test
    public void testJreProvided() throws Exception {
        HubIntRestService service = getMockedService("3.0.0");
        HubSupportHelper supportHelper = new HubSupportHelper();
        TestLogger logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        assertTrue(supportHelper.isHasBeenChecked());
        assertTrue(supportHelper.isJreProvidedSupport());
        assertTrue(supportHelper.isPolicyApiSupport());

        service = getMockedService("3.0.1");
        supportHelper = new HubSupportHelper();
        logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        assertTrue(supportHelper.isHasBeenChecked());
        assertTrue(supportHelper.isJreProvidedSupport());
        assertTrue(supportHelper.isPolicyApiSupport());
        assertTrue(supportHelper.isCliStatusDirOptionSupport());

        service = getMockedService("3.1.0");
        supportHelper = new HubSupportHelper();
        logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        assertTrue(supportHelper.isHasBeenChecked());
        assertTrue(supportHelper.isJreProvidedSupport());
        assertTrue(supportHelper.isPolicyApiSupport());
        assertTrue(supportHelper.isCliStatusDirOptionSupport());

        service = getMockedService("4.0.0");
        supportHelper = new HubSupportHelper();
        logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        assertTrue(supportHelper.isHasBeenChecked());
        assertTrue(supportHelper.isJreProvidedSupport());
        assertTrue(supportHelper.isPolicyApiSupport());
        assertTrue(supportHelper.isCliStatusDirOptionSupport());

        service = getMockedService("3.0.0-SNAPSHOT");
        supportHelper = new HubSupportHelper();
        logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        assertTrue(supportHelper.isHasBeenChecked());
        assertTrue(!supportHelper.isJreProvidedSupport());
        assertTrue(!supportHelper.isPolicyApiSupport());
        assertTrue(!supportHelper.isCliStatusDirOptionSupport());

        service = getMockedService("2.9.9");
        supportHelper = new HubSupportHelper();
        logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        assertTrue(supportHelper.isHasBeenChecked());
        assertTrue(!supportHelper.isJreProvidedSupport());
        assertTrue(!supportHelper.isPolicyApiSupport());
        assertTrue(!supportHelper.isCliStatusDirOptionSupport());
    }

    @Test
    public void testCheckHubSupportFallback() throws Exception {
        HubIntRestService service = getMockedServiceWithFallBack("Two.one.zero", true);
        HubSupportHelper supportHelper = new HubSupportHelper();
        TestLogger logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        assertTrue(supportHelper.isJreProvidedSupport());
        assertTrue(supportHelper.isPolicyApiSupport());

        service = getMockedServiceWithFallBack("3.0", true);
        supportHelper = new HubSupportHelper();
        logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        assertTrue(supportHelper.isJreProvidedSupport());
        assertTrue(supportHelper.isPolicyApiSupport());

    }

    @Test
    public void testHubVersionApiMissing() throws Exception {
        HubIntRestService service = getMockedService("2.0.1");
        ResourceException cause = new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
        BDRestException exception = new BDRestException("", cause, null);
        Mockito.when(service.getHubVersion()).thenThrow(exception);

        HubSupportHelper supportHelper = new HubSupportHelper();
        TestLogger logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        assertTrue(!supportHelper.isJreProvidedSupport());
        assertTrue(!supportHelper.isPolicyApiSupport());

    }

    @Test
    public void testHubVersionApiRestFailure() throws Exception {
        HubIntRestService service = getMockedService("2.0.1");
        ResourceException cause = new ResourceException(Status.CLIENT_ERROR_PAYMENT_REQUIRED);
        final String errorMessage = "error";
        BDRestException exception = new BDRestException(errorMessage, cause, null);
        Mockito.when(service.getHubVersion()).thenThrow(exception);

        HubSupportHelper supportHelper = new HubSupportHelper();
        TestLogger logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        assertTrue(!supportHelper.isJreProvidedSupport());
        assertTrue(!supportHelper.isPolicyApiSupport());

        assertTrue(logger.getOutputString().contains(errorMessage));
        assertTrue(logger.getOutputString().contains(Status.CLIENT_ERROR_PAYMENT_REQUIRED.getReasonPhrase()));
    }

    @Test
    public void testHubVersionApiRestFailureDifferentEror() throws Exception {
        HubIntRestService service = getMockedService("2.0.1");
        Exception cause = new Exception();
        final String errorMessage = "error";
        BDRestException exception = new BDRestException(errorMessage, cause, null);
        Mockito.when(service.getHubVersion()).thenThrow(exception);

        HubSupportHelper supportHelper = new HubSupportHelper();
        TestLogger logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        assertTrue(!supportHelper.isJreProvidedSupport());
        assertTrue(!supportHelper.isPolicyApiSupport());
        assertTrue(logger.getOutputString().contains(errorMessage));
    }

    @Test
    public void testGetLinuxCLIWrapperLink() throws Exception {
        assertEquals("testUrl/download/scan.cli.zip", HubSupportHelper.getLinuxCLIWrapperLink("testUrl"));
        assertEquals("testUrl/download/scan.cli.zip", HubSupportHelper.getLinuxCLIWrapperLink("testUrl/"));
        assertEquals("http://testSite/download/scan.cli.zip", HubSupportHelper.getLinuxCLIWrapperLink("http://testSite/"));
        assertEquals("http://testSite/download/scan.cli.zip", HubSupportHelper.getLinuxCLIWrapperLink("http://testSite"));

        try {
            HubSupportHelper.getLinuxCLIWrapperLink(" ");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
        }

        try {
            HubSupportHelper.getLinuxCLIWrapperLink(null);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
        }
    }

    @Test
    public void testGetWindowsCLIWrapperLink() throws Exception {
        assertEquals("testUrl/download/scan.cli-windows.zip", HubSupportHelper.getWindowsCLIWrapperLink("testUrl"));
        assertEquals("testUrl/download/scan.cli-windows.zip", HubSupportHelper.getWindowsCLIWrapperLink("testUrl/"));
        assertEquals("http://testSite/download/scan.cli-windows.zip", HubSupportHelper.getWindowsCLIWrapperLink("http://testSite/"));
        assertEquals("http://testSite/download/scan.cli-windows.zip", HubSupportHelper.getWindowsCLIWrapperLink("http://testSite"));

        try {
            HubSupportHelper.getWindowsCLIWrapperLink(" ");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
        }

        try {
            HubSupportHelper.getWindowsCLIWrapperLink(null);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
        }
    }

    @Test
    public void testGetOSXCLIWrapperLink() throws Exception {
        assertEquals("testUrl/download/scan.cli-macosx.zip", HubSupportHelper.getOSXCLIWrapperLink("testUrl"));
        assertEquals("testUrl/download/scan.cli-macosx.zip", HubSupportHelper.getOSXCLIWrapperLink("testUrl/"));
        assertEquals("http://testSite/download/scan.cli-macosx.zip", HubSupportHelper.getOSXCLIWrapperLink("http://testSite/"));
        assertEquals("http://testSite/download/scan.cli-macosx.zip", HubSupportHelper.getOSXCLIWrapperLink("http://testSite"));

        try {
            HubSupportHelper.getOSXCLIWrapperLink(" ");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
        }

        try {
            HubSupportHelper.getOSXCLIWrapperLink(null);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
        }
    }

}
