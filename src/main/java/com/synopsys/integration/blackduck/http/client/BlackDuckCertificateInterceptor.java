package com.synopsys.integration.blackduck.http.client;

import java.io.IOException;
import java.security.cert.Certificate;

import javax.net.ssl.SSLSession;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

public class BlackDuckCertificateInterceptor implements HttpResponseInterceptor {
    @Override
    public void process(HttpResponse response, HttpContext context) throws IOException {
        ManagedHttpClientConnection routedConnection = (ManagedHttpClientConnection) context.getAttribute(HttpCoreContext.HTTP_CONNECTION);
        SSLSession sslSession = routedConnection.getSSLSession();
        if (sslSession != null) {
            Certificate[] certificates = sslSession.getPeerCertificates();
            context.setAttribute(DefaultBlackDuckHttpClient.PEER_CERTIFICATES, certificates);
        }
    }

}
