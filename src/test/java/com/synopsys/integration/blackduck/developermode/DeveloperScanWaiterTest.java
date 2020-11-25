package com.synopsys.integration.blackduck.developermode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.manual.view.BomMatchDeveloperView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;

public class DeveloperScanWaiterTest {

    @Test
    public void testWaitSuccess() throws Exception {
        UUID scanId = UUID.randomUUID();
        List<BomMatchDeveloperView> expectedResults = new ArrayList<>();
        expectedResults.add(new BomMatchDeveloperView());
        BufferedIntLogger logger = new BufferedIntLogger();

        HttpUrl url = Mockito.mock(HttpUrl.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Response response = Mockito.mock(Response.class);
        Mockito.when(blackDuckApiClient.getUrl(Mockito.any())).thenReturn(url);
        Mockito.when(blackDuckApiClient.get(Mockito.any(BlackDuckPath.class))).thenReturn(response);
        Mockito.when(blackDuckApiClient.getArrayResponse(Mockito.any(), Mockito.eq(BomMatchDeveloperView.class))).thenReturn(expectedResults);
        Mockito.when(response.isStatusCodeSuccess()).thenReturn(true);
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(expectedResults);
        DeveloperScanWaiter waiter = new DeveloperScanWaiter(logger, blackDuckApiClient);

        long timeoutInSeconds = 2;
        int waitInSeconds = 1;
        List<BomMatchDeveloperView> results = waiter.checkScanResult(scanId, timeoutInSeconds, waitInSeconds);

        assertEquals(expectedResults, results);
    }

    @Test
    public void testWaitLongerThanTimeout() throws Exception {
        BufferedIntLogger logger = new BufferedIntLogger();
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Response response = Mockito.mock(Response.class);
        Mockito.when(blackDuckApiClient.get(Mockito.any(BlackDuckPath.class))).thenReturn(response);
        Mockito.when(response.isStatusCodeSuccess()).thenReturn(false);
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(new ArrayList<>());
        DeveloperScanWaiter waiter = new DeveloperScanWaiter(logger, blackDuckApiClient);
        UUID scanId = UUID.randomUUID();
        long timeoutInSeconds = 1;
        int waitInSeconds = 2;
        try {
            waiter.checkScanResult(scanId, timeoutInSeconds, waitInSeconds);
            fail();
        } catch (IntegrationException | InterruptedException ex) {
            // pass
        }
    }

    @Test
    public void testWaitFailed() throws Exception {
        BufferedIntLogger logger = new BufferedIntLogger();
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        BomMatchDeveloperView expectedView = new BomMatchDeveloperView();
        Response response = Mockito.mock(Response.class);
        Mockito.when(blackDuckApiClient.get(Mockito.any(BlackDuckPath.class))).thenReturn(response);
        Mockito.when(response.isStatusCodeSuccess()).thenReturn(false);
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(new ArrayList<>());
        DeveloperScanWaiter waiter = new DeveloperScanWaiter(logger, blackDuckApiClient);
        UUID scanId = UUID.randomUUID();
        long timeoutInSeconds = 2;
        int waitInSeconds = 1;
        try {
            waiter.checkScanResult(scanId, timeoutInSeconds, waitInSeconds);
            fail();
        } catch (IntegrationException | InterruptedException ex) {
            // pass
        }
    }
}
