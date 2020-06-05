package com.synopsys.integration.blackduck.rest;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class IntRedirectStrategy implements RedirectStrategy {
    private LaxRedirectStrategy defaultRedirectStrategy = new LaxRedirectStrategy();

    @Override
    public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws ProtocolException {
        boolean isRedirected = defaultRedirectStrategy.isRedirected(httpRequest, httpResponse, httpContext);
        System.out.println("redirect strategy:isRedirected -> " + isRedirected);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == 308) {
            return true;
        }
        return isRedirected;
    }

    @Override
    public HttpUriRequest getRedirect(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws ProtocolException {
        HttpUriRequest redirect = defaultRedirectStrategy.getRedirect(httpRequest, httpResponse, httpContext);
        System.out.println("redirect strategy:getRedirect -> " + redirect.getURI().toString());
        return redirect;
    }

}
