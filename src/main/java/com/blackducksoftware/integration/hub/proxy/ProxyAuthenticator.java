
package com.blackducksoftware.integration.hub.proxy;

import java.io.IOException;
import java.util.List;

import okhttp3.Authenticator;
import okhttp3.Challenge;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Proxy authenticator which is the same code ripped out of okhttp-digest
 */
public class ProxyAuthenticator implements Authenticator {

    public static final String PROXY_AUTH = "Proxy-Authenticate";

    public static final String PROXY_AUTH_RESP = "Proxy-Authorization";

    public static final String WWW_AUTH = "WWW-Authenticate";

    public static final String WWW_AUTH_RESP = "Authorization";

    private final String username;

    private final String password;

    private boolean proxy;

    private boolean basicAuth;

    public ProxyAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if (route.proxy() != null) {
            setProxy(true);
        }
        checkAuthScheme(response);
        if (isBasicAuth()) {
            return authenticateBasic(response);
        }
        return null;
    }

    private void checkAuthScheme(Response response) {
        List<Challenge> challenges = response.challenges();
        for (Challenge challenge : challenges) {
            if ("Basic".equalsIgnoreCase(challenge.scheme())) {
                setBasicAuth(true);
            }
        }
    }

    public synchronized Request authenticateBasic(Response response) throws IOException {
        String headerKey;
        if (isProxy()) {
            headerKey = PROXY_AUTH_RESP;
        } else {
            headerKey = WWW_AUTH_RESP;
        }
        String credential = Credentials.basic(username, password);
        return response.request().newBuilder().header(headerKey, credential).build();
    }

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    public boolean isBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(boolean basicAuth) {
        this.basicAuth = basicAuth;
    }

}
