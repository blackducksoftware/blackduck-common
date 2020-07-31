package com.synopsys.integration.blackduck.http.client;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.http.HttpHeaders;

public class MockWebServerUtil {
    public static void setupRedirecting(MockWebServer server, String destinationUrl) {
        Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                return new MockResponse()
                        .setResponseCode(308)
                        .addHeader(HttpHeaders.LOCATION, destinationUrl + request.getPath());
            }
        };
        server.setDispatcher(dispatcher);
    }

}
