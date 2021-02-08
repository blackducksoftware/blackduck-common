package com.synopsys.integration.blackduck;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.response.Response;

public class SslTest {
    @Test
    public void testSslConnections() throws IOException, IntegrationException {
        BufferedIntLogger logger = new BufferedIntLogger();
        logger.info("hello info!");

        IntHttpClient intHttpClient = new IntHttpClient(logger, 120, false, ProxyInfo.NO_PROXY_INFO);

        //        getResponse(intHttpClient, "https://www.synopsys.com/company.html");
        getResponse(intHttpClient, "https://int-bdstarlabs.dc1.lan/");

        //        checkUrl(blackDuckServer);
        //
        //        checkUrl("https://www.synopsys.com/company.html");
        //        checkUrl("http://www.google.com/");

        Arrays.stream(LogLevel.values())
            .map(logger::getOutputString)
            .forEach(System.out::println);
    }

    private void getResponse(IntHttpClient intHttpClient, String url) throws IntegrationException {
        HttpGet get = new HttpGet(url);
        Response response = intHttpClient.execute(get);
        System.out.println(response);
        System.out.println(response.getContentString());
    }

    private void checkUrl(String serverUrl) throws IOException {
        URL url = new URL(serverUrl);
        URLConnection urlConnection = url.openConnection();
        System.out.println(urlConnection.getContentLength());
        urlConnection.connect();
        System.out.println(urlConnection.getContentLength());
    }
}
