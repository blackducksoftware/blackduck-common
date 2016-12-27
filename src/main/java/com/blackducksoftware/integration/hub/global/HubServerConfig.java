/**
 * Hub Common
 *
 * Copyright (C) 2016 Black Duck Software, Inc..
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
 */
package com.blackducksoftware.integration.hub.global;

import java.io.Serializable;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.log.IntLogger;

public class HubServerConfig implements Serializable {
    private static final long serialVersionUID = -1581638027683631935L;

    private final URL hubUrl;

    private final int timeoutSeconds;

    private final HubCredentials credentials;

    private final HubProxyInfo proxyInfo;

    public HubServerConfig(final URL url, final int timeoutSeconds, final HubCredentials credentials,
            final HubProxyInfo proxyInfo) {
        this.hubUrl = url;
        this.timeoutSeconds = timeoutSeconds;
        this.credentials = credentials;
        this.proxyInfo = proxyInfo;
    }

    public boolean shouldUseProxyForHub() {
        return proxyInfo != null && proxyInfo.shouldUseProxyForUrl(hubUrl);
    }

    public void print(final IntLogger logger) {
        if (getHubUrl() != null) {
            logger.alwaysLog("--> Hub Server Url : " + getHubUrl());
        }
        if (StringUtils.isNotBlank(getGlobalCredentials().getUsername())) {
            logger.alwaysLog("--> Hub User : " + getGlobalCredentials().getUsername());
        }

        if (proxyInfo != null) {
            if (StringUtils.isNotBlank(proxyInfo.getHost())) {
                logger.alwaysLog("--> Proxy Host : " + proxyInfo.getHost());
            }
            if (proxyInfo.getPort() > 0) {
                logger.alwaysLog("--> Proxy Port : " + proxyInfo.getPort());
            }
            if (StringUtils.isNotBlank(proxyInfo.getIgnoredProxyHosts())) {
                logger.alwaysLog("--> No Proxy Hosts : " + proxyInfo.getIgnoredProxyHosts());
            }
            if (StringUtils.isNotBlank(proxyInfo.getUsername())) {
                logger.alwaysLog("--> Proxy Username : " + proxyInfo.getUsername());
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("HubServerConfig [hubUrl=");
        builder.append(hubUrl);
        builder.append(", timeout=");
        builder.append(timeoutSeconds);
        builder.append(", hubCredentials=");
        builder.append(credentials);
        builder.append(", proxyInfo=");
        builder.append(proxyInfo);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((credentials == null) ? 0 : credentials.hashCode());
        result = prime * result + ((hubUrl == null) ? 0 : hubUrl.hashCode());
        result = prime * result + ((proxyInfo == null) ? 0 : proxyInfo.hashCode());
        result = prime * result + timeoutSeconds;
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
        if (!(obj instanceof HubServerConfig)) {
            return false;
        }
        final HubServerConfig other = (HubServerConfig) obj;
        if (credentials == null) {
            if (other.credentials != null) {
                return false;
            }
        } else if (!credentials.equals(other.credentials)) {
            return false;
        }
        if (hubUrl == null) {
            if (other.hubUrl != null) {
                return false;
            }
        } else if (!hubUrl.equals(other.hubUrl)) {
            return false;
        }
        if (proxyInfo == null) {
            if (other.proxyInfo != null) {
                return false;
            }
        } else if (!proxyInfo.equals(other.proxyInfo)) {
            return false;
        }
        if (timeoutSeconds != other.timeoutSeconds) {
            return false;
        }
        return true;
    }

    public URL getHubUrl() {
        return hubUrl;
    }

    public HubCredentials getGlobalCredentials() {
        return credentials;
    }

    public HubProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public int getTimeout() {
        return timeoutSeconds;
    }

}
