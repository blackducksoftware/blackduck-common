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
import com.synopsys.integration.rest.response.Response;

public class DeveloperScanWaiterTest {

    @Test
    public void testWaitSuccess() throws Exception {
        BufferedIntLogger logger = new BufferedIntLogger();
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        List<BomMatchDeveloperView> expectedResults = new ArrayList<>();
        expectedResults.add(new BomMatchDeveloperView());
        Response response = Mockito.mock(Response.class);
        Mockito.when(blackDuckApiClient.get(Mockito.any(BlackDuckPath.class))).thenReturn(response);
        Mockito.when(response.isStatusCodeSuccess()).thenReturn(true);
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(expectedResults);
        DeveloperScanWaiter waiter = new DeveloperScanWaiter(logger, blackDuckApiClient);
        UUID scanId = UUID.randomUUID();
        long timeoutInSeconds = 10;
        int waitInSeconds = 30;
        List<BomMatchDeveloperView> results = waiter.checkScanResult(scanId, timeoutInSeconds, waitInSeconds);

        assertEquals(expectedResults, results);
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
        long timeoutInSeconds = 10;
        int waitInSeconds = 30;
        try {
            waiter.checkScanResult(scanId, timeoutInSeconds, waitInSeconds);
            fail();
        } catch (IntegrationException | InterruptedException ex) {
            // pass
        }
    }
}
