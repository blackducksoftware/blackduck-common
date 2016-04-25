/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.global;

import java.io.Serializable;
import java.net.URL;

public class HubServerConfig implements Serializable {
	private static final long serialVersionUID = -1581638027683631935L;

	private final URL hubUrl;
	private final int timeout;
	private final HubCredentials credentials;
	private final HubProxyInfo proxyInfo;

	public HubServerConfig(final URL url, final int timeout, final HubCredentials credentials,
			final HubProxyInfo proxyInfo) {
		this.hubUrl = url;
		this.timeout = timeout;
		this.credentials = credentials;
		this.proxyInfo = proxyInfo;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubServerConfig [hubUrl=");
		builder.append(hubUrl);
		builder.append(", timeout=");
		builder.append(timeout);
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
		result = prime * result + timeout;
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
		if (timeout != other.timeout) {
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
		return timeout;
	}

}
