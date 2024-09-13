/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http.client;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.security.cert.Certificate;

public class BlackDuckCertificateInterceptor implements HttpResponseInterceptor {
    @Override
    public void process(HttpResponse response, HttpContext context) throws IOException {
        ManagedHttpClientConnection routedConnection = (ManagedHttpClientConnection) context.getAttribute(HttpCoreContext.HTTP_CONNECTION);
        if (routedConnection.isOpen()) {
            SSLSession sslSession = routedConnection.getSSLSession();
            if (sslSession != null) {
                Certificate[] certificates = sslSession.getPeerCertificates();
                context.setAttribute(SignatureScannerClient.PEER_CERTIFICATES, certificates);
            }
        }
    }

}
