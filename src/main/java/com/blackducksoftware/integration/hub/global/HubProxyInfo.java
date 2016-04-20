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
import java.util.List;
import java.util.regex.Pattern;

public class HubProxyInfo implements Serializable {

	private static final long serialVersionUID = 8813395288911907788L;

	private final String host;

	private final Integer port;

	private final String proxyUsername;

	private final String proxyPassword;

	private final String ignoredProxyHosts;

	private final List<Pattern> ignoredProxyHostPatterns;

	private final boolean hubUrlIgnored;


	public HubProxyInfo(final String host, final Integer port, final String noProxyHosts,
			final List<Pattern> noProxyHostsPatterns, final String proxyUsername, final String proxyPassword,
			final boolean hubUrlIgnored) {
		this.host = host;
		this.port = port;
		this.ignoredProxyHosts = noProxyHosts;
		this.ignoredProxyHostPatterns = noProxyHostsPatterns;
		this.proxyUsername = proxyUsername;
		this.proxyPassword = proxyPassword;
		this.hubUrlIgnored = hubUrlIgnored;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public String getIgnoredProxyHosts() {
		return ignoredProxyHosts;
	}

	public List<Pattern> getIgnoredProxyHostPatterns() {
		return ignoredProxyHostPatterns;
	}

	public boolean isHubUrlIgnored() {
		return hubUrlIgnored;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubProxyInfo [host=");
		builder.append(host);
		builder.append(", port=");
		builder.append(port);
		builder.append(", proxyUsername=");
		builder.append(proxyUsername);
		builder.append(", proxyPassword=");
		builder.append(proxyPassword);
		builder.append(", ignoredProxyHosts=");
		builder.append(ignoredProxyHosts);
		builder.append(", ignoredProxyHostPatterns=");
		builder.append(ignoredProxyHostPatterns);
		builder.append(", hubUrlIgnored=");
		builder.append(hubUrlIgnored);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + (hubUrlIgnored ? 1231 : 1237);
		result = prime * result + ((ignoredProxyHostPatterns == null) ? 0 : ignoredProxyHostPatterns.hashCode());
		result = prime * result + ((ignoredProxyHosts == null) ? 0 : ignoredProxyHosts.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result + ((proxyPassword == null) ? 0 : proxyPassword.hashCode());
		result = prime * result + ((proxyUsername == null) ? 0 : proxyUsername.hashCode());
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
		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}
		if (hubUrlIgnored != other.hubUrlIgnored) {
			return false;
		}
		if (ignoredProxyHostPatterns == null) {
			if (other.ignoredProxyHostPatterns != null) {
				return false;
			}
		} else if (!ignoredProxyHostPatterns.equals(other.ignoredProxyHostPatterns)) {
			return false;
		}
		if (ignoredProxyHosts == null) {
			if (other.ignoredProxyHosts != null) {
				return false;
			}
		} else if (!ignoredProxyHosts.equals(other.ignoredProxyHosts)) {
			return false;
		}
		if (port == null) {
			if (other.port != null) {
				return false;
			}
		} else if (!port.equals(other.port)) {
			return false;
		}
		if (proxyPassword == null) {
			if (other.proxyPassword != null) {
				return false;
			}
		} else if (!proxyPassword.equals(other.proxyPassword)) {
			return false;
		}
		if (proxyUsername == null) {
			if (other.proxyUsername != null) {
				return false;
			}
		} else if (!proxyUsername.equals(other.proxyUsername)) {
			return false;
		}
		return true;
	}

}
