package com.synopsys.integration.blackduck.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;

public class RapidScanWaiterTest {
    @Test
    public void testWaitSuccess() throws Exception {
        List<DeveloperScanComponentResultView> expectedResults = new ArrayList<>();
        expectedResults.add(new DeveloperScanComponentResultView());
        BufferedIntLogger logger = new BufferedIntLogger();

        HttpUrl url = Mockito.mock(HttpUrl.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Response response = Mockito.mock(Response.class);

        Mockito.when(blackDuckApiClient.execute(Mockito.any(BlackDuckResponseRequest.class))).thenReturn(response);
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.any(UrlMultipleResponses.class))).thenReturn(expectedResults);
        Mockito.when(response.isStatusCodeSuccess()).thenReturn(true);

        RapidScanWaiter waiter = new RapidScanWaiter(logger, blackDuckApiClient);

        long timeoutInSeconds = 2;
        int waitInSeconds = 1;
        List<DeveloperScanComponentResultView> results = waiter.checkScanResult(url, timeoutInSeconds, waitInSeconds);

        assertEquals(expectedResults, results);
    }

    @Test
    public void testWaitLongerThanTimeout() throws Exception {
        BufferedIntLogger logger = new BufferedIntLogger();
        HttpUrl url = Mockito.mock(HttpUrl.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Response response = Mockito.mock(Response.class);

        Mockito.when(blackDuckApiClient.execute(Mockito.any(BlackDuckResponseRequest.class))).thenReturn(response);
        Mockito.when(response.isStatusCodeSuccess()).thenReturn(false);
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.any(UrlMultipleResponses.class))).thenReturn(new ArrayList<>());

        RapidScanWaiter waiter = new RapidScanWaiter(logger, blackDuckApiClient);
        long timeoutInSeconds = 1;
        int waitInSeconds = 2;
        try {
            waiter.checkScanResult(url, timeoutInSeconds, waitInSeconds);
            fail();
        } catch (IntegrationException | InterruptedException ex) {
            // pass
        }
    }

    @Test
    public void testWaitFailed() throws Exception {
        BufferedIntLogger logger = new BufferedIntLogger();
        HttpUrl url = Mockito.mock(HttpUrl.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Response response = Mockito.mock(Response.class);

        Mockito.when(blackDuckApiClient.execute(Mockito.any(BlackDuckResponseRequest.class))).thenReturn(response);
        Mockito.when(response.isStatusCodeSuccess()).thenReturn(false);
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.any(UrlMultipleResponses.class))).thenReturn(new ArrayList<>());

        RapidScanWaiter waiter = new RapidScanWaiter(logger, blackDuckApiClient);
        long timeoutInSeconds = 2;
        int waitInSeconds = 1;
        try {
            waiter.checkScanResult(url, timeoutInSeconds, waitInSeconds);
            fail();
        } catch (IntegrationException | InterruptedException ex) {
            // pass
        }
    }

}
