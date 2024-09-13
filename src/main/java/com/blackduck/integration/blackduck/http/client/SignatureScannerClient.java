/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http.client;

import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.client.IntHttpClient;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.request.Request;
import com.blackduck.integration.rest.response.Response;
import com.google.gson.Gson;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.Optional;

public class SignatureScannerClient extends IntHttpClient {
    public static final String PEER_CERTIFICATES = "PEER_CERTIFICATES";

    private Certificate serverCertificate;

    public SignatureScannerClient(BlackDuckHttpClient blackDuckHttpClient) {
        super(blackDuckHttpClient.getLogger(), blackDuckHttpClient.getGson(), blackDuckHttpClient.getTimeoutInSeconds(), blackDuckHttpClient.isAlwaysTrustServerCertificate(), blackDuckHttpClient.getProxyInfo());
    }

    public SignatureScannerClient(IntLogger logger, Gson gson, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo) {
        super(logger, gson, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo);
    }

    public SignatureScannerClient(IntLogger logger, Gson gson, int timeoutInSeconds, ProxyInfo proxyInfo, SSLContext sslContext) {
        super(logger, gson, timeoutInSeconds, proxyInfo, sslContext);
    }

    @Override
    protected void addToHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
        super.addToHttpClientBuilder(httpClientBuilder, defaultRequestConfigBuilder);
        httpClientBuilder.addInterceptorLast(new BlackDuckCertificateInterceptor());
        httpClientBuilder.setConnectionReuseStrategy((httpResponse, httpContext) -> true);
    }

    /*
    Deprecated in favor of SignatureScannerClient::executeGetRequest.
    Black Duck does not handle HEAD requests properly, resulting in too much data for firewalls to allow in a HEAD request.
    The new mechanism uses a saved Black Duck version for comparison rather than modified file timestamps.
     */
    @Deprecated
    @Override
    public Optional<Response> executeGetRequestIfModifiedSince(Request getRequest, long timeToCheck) throws IntegrationException, IOException {
        HttpContext httpContext = new BasicHttpContext();
        Optional<Response> response = super.executeGetRequestIfModifiedSince(getRequest, timeToCheck, httpContext);
        saveCertificates(httpContext);
        return response;
    }

    public Response executeGetRequest(Request getRequest) throws IntegrationException {
        HttpContext httpContext = new BasicHttpContext();
        Response response = super.execute(getRequest, httpContext);
        saveCertificates(httpContext);
        return response;
    }

    private void saveCertificates(HttpContext httpContext) {
        Certificate[] peerCertificates = (Certificate[]) httpContext.getAttribute(PEER_CERTIFICATES);
        if (null != peerCertificates) {
            serverCertificate = peerCertificates[0];
        }
    }

    public Certificate getServerCertificate() {
        return serverCertificate;
    }

}
