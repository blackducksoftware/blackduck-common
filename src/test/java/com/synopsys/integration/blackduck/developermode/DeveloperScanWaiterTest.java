package com.synopsys.integration.blackduck.developermode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathSingleResponse;
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
        BomMatchDeveloperView expectedView = new BomMatchDeveloperView();
        Response response = Mockito.mock(Response.class);
        Mockito.when(blackDuckApiClient.get(Mockito.any(BlackDuckPath.class))).thenReturn(response);
        Mockito.when(response.isStatusCodeSuccess()).thenReturn(true);
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(BlackDuckPathSingleResponse.class))).thenReturn(expectedView);
        DeveloperScanWaiter waiter = new DeveloperScanWaiter(logger, blackDuckApiClient);
        UUID scanId = UUID.randomUUID();
        long timeoutInSeconds = 10;
        int waitInSeconds = 30;
        BomMatchDeveloperView result = waiter.checkScanResult(scanId, timeoutInSeconds, waitInSeconds);

        assertEquals(expectedView, result);
    }

    @Test
    public void testWaitFailed() throws Exception {
        BufferedIntLogger logger = new BufferedIntLogger();
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        BomMatchDeveloperView expectedView = new BomMatchDeveloperView();
        Response response = Mockito.mock(Response.class);
        Mockito.when(blackDuckApiClient.get(Mockito.any(BlackDuckPath.class))).thenReturn(response);
        Mockito.when(response.isStatusCodeSuccess()).thenReturn(false);
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(BlackDuckPathSingleResponse.class))).thenReturn(expectedView);
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
