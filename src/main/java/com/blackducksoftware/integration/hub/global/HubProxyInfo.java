/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.global;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.util.AuthenticatorUtil;
import com.blackducksoftware.integration.util.proxy.ProxyUtil;

public class HubProxyInfo implements Serializable {
    private static final long serialVersionUID = -7476704373593358472L;

    private final String host;

    private final int port;

    private final HubCredentials proxyCredentials;

    private final String ignoredProxyHosts;

    public HubProxyInfo(final String host, final int port, final HubCredentials proxyCredentials,
            final String ignoredProxyHosts) {
        this.host = host;
        this.port = port;
        this.proxyCredentials = proxyCredentials;
        this.ignoredProxyHosts = ignoredProxyHosts;
    }

    public URLConnection openConnection(final URL url) throws IOException {
        final Proxy proxy = getProxy(url);
        return url.openConnection(proxy);
    }

    public Proxy getProxy(final URL url) {
        if (shouldUseProxyForUrl(url)) {
            final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            setDefaultAuthenticator();
            return proxy;
        }
        return Proxy.NO_PROXY;
    }

    public boolean shouldUseProxyForUrl(final URL url) {
        if (StringUtils.isBlank(host) || port <= 0) {
            return false;
        }
        final List<Pattern> ignoredProxyHostPatterns = ProxyUtil.getIgnoredProxyHostPatterns(ignoredProxyHosts);
        return !ProxyUtil.shouldIgnoreHost(url.getHost(), ignoredProxyHostPatterns);
    }

    public void setDefaultAuthenticator() {
        if (getUsername() != null && getEncryptedPassword() != null) {
            try {
                AuthenticatorUtil.setAuthenticator(getProxyCredentials().getUsername(),
                        PasswordDecrypter.decrypt(getProxyCredentials().getEncryptedPassword()));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        } else {
            AuthenticatorUtil.resetAuthenticator();
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("HubProxyInfo [host=");
        builder.append(host);
        builder.append(", port=");
        builder.append(port);
        builder.append(", username=");
        builder.append(getUsername());
        builder.append(", encryptedPassword=");
        builder.append(getEncryptedPassword());
        builder.append(", actualPasswordLength=");
        builder.append(getActualPasswordLength());
        builder.append(", ignoredProxyHosts=");
        builder.append(ignoredProxyHosts);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((proxyCredentials == null) ? 0 : proxyCredentials.hashCode());
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((ignoredProxyHosts == null) ? 0 : ignoredProxyHosts.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HubProxyInfo)) {
            return false;
        }
        final HubProxyInfo other = (HubProxyInfo) obj;
        if (getProxyCredentials() == null) {
            if (other.getProxyCredentials() != null) {
                return false;
            }
        } else if (!getProxyCredentials().equals(other.getProxyCredentials())) {
            return false;
        }

        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (ignoredProxyHosts == null) {
            if (other.ignoredProxyHosts != null) {
                return false;
            }
        } else if (!ignoredProxyHosts.equals(other.ignoredProxyHosts)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }

        return true;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        if (getProxyCredentials() == null) {
            return null;
        } else {
            return getProxyCredentials().getUsername();
        }
    }

    public String getEncryptedPassword() {
        if (getProxyCredentials() == null) {
            return null;
        } else {
            return getProxyCredentials().getEncryptedPassword();
        }
    }

    public String getDecryptedPassword() throws IllegalArgumentException, EncryptionException {
        if (getProxyCredentials() == null) {
            return null;
        } else {
            return getProxyCredentials().getDecryptedPassword();
        }
    }

    public String getMaskedPassword() {
        if (getProxyCredentials() == null) {
            return null;
        } else {
            return getProxyCredentials().getMaskedPassword();
        }
    }

    public int getActualPasswordLength() {
        if (getProxyCredentials() == null) {
            return 0;
        } else {
            return getProxyCredentials().getActualPasswordLength();
        }
    }

    public String getIgnoredProxyHosts() {
        return ignoredProxyHosts;
    }

    private HubCredentials getProxyCredentials() {
        return proxyCredentials;
    }

}
