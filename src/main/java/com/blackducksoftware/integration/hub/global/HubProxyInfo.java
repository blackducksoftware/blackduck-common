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

import java.io.IOException;
import java.io.Serializable;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.hub.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.hub.exception.EncryptionException;

public class HubProxyInfo implements Serializable {
	private static final long serialVersionUID = 8813395288911907788L;

	private final String host;
	private final int port;
	private final String username;
	private final String encryptedPassword;
	private final int actualPasswordLength;
	private final String ignoredProxyHosts;

	public HubProxyInfo(final String host, final int port, final String username, final String password,
			final String ignoredProxyHosts) throws IllegalArgumentException, EncryptionException {
		this.host = host;
		this.port = port;
		this.username = username;
		if (StringUtils.isNotBlank(password)) {
			actualPasswordLength = password.length();
			this.encryptedPassword = PasswordEncrypter.encrypt(password);
		} else {
			actualPasswordLength = 0;
			this.encryptedPassword = null;
		}
		this.ignoredProxyHosts = ignoredProxyHosts;
	}

	public URLConnection openConnection(final URL url) throws IOException {
		if (shouldUseProxyForUrl(url)) {
			final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
			setDefaultAuthenticator();

			return url.openConnection(proxy);
		}

		return url.openConnection();
	}

	public boolean shouldUseProxyForUrl(final URL url) {
		final List<Pattern> ignoredProxyHostPatterns = getIgnoredProxyHostPatterns();
		return !shouldIgnoreHost(url.getHost(), ignoredProxyHostPatterns);
	}

	public void setDefaultAuthenticator() {
		if (username != null && encryptedPassword != null) {
			Authenticator.setDefault(new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					try {
						return new PasswordAuthentication(username,
								PasswordDecrypter.decrypt(encryptedPassword).toCharArray());
					} catch (final Exception e) {
					}
					return null;
				}
			});
		} else {
			Authenticator.setDefault(null);
		}
	}

	/**
	 * Checks the list of user defined host names that should be connected to
	 * directly and not go through the proxy. If the hostToMatch matches any of
	 * these hose names then this method returns true.
	 *
	 */
	private boolean shouldIgnoreHost(final String hostToMatch, final List<Pattern> ignoredProxyHostPatterns) {
		if (StringUtils.isBlank(hostToMatch) || null == ignoredProxyHostPatterns
				|| ignoredProxyHostPatterns.isEmpty()) {
			return false;
		}

		for (final Pattern ignoredProxyHostPattern : ignoredProxyHostPatterns) {
			final Matcher m = ignoredProxyHostPattern.matcher(hostToMatch);
			return m.find();
		}
		return false;
	}

	private List<Pattern> getIgnoredProxyHostPatterns() {
		final List<Pattern> ignoredProxyHostPatterns = new ArrayList<Pattern>();
		if (StringUtils.isNotBlank(ignoredProxyHosts)) {
			String[] ignoreHosts = null;
			if (ignoredProxyHosts.contains(",")) {
				ignoreHosts = ignoredProxyHosts.split(",");
				for (final String ignoreHost : ignoreHosts) {
					final Pattern pattern = Pattern.compile(ignoreHost.trim());
					ignoredProxyHostPatterns.add(pattern);
				}
			} else {
				final Pattern pattern = Pattern.compile(ignoredProxyHosts);
				ignoredProxyHostPatterns.add(pattern);
			}
		}
		return ignoredProxyHostPatterns;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubProxyInfo [host=");
		builder.append(host);
		builder.append(", port=");
		builder.append(port);
		builder.append(", username=");
		builder.append(username);
		builder.append(", encryptedPassword=");
		builder.append(encryptedPassword);
		builder.append(", actualPasswordLength=");
		builder.append(actualPasswordLength);
		builder.append(", ignoredProxyHosts=");
		builder.append(ignoredProxyHosts);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + actualPasswordLength;
		result = prime * result + ((encryptedPassword == null) ? 0 : encryptedPassword.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((ignoredProxyHosts == null) ? 0 : ignoredProxyHosts.hashCode());
		result = prime * result + port;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		if (actualPasswordLength != other.actualPasswordLength) {
			return false;
		}
		if (encryptedPassword == null) {
			if (other.encryptedPassword != null) {
				return false;
			}
		} else if (!encryptedPassword.equals(other.encryptedPassword)) {
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
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
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
		return username;
	}

	public String getEncryptedPassword() {
		return encryptedPassword;
	}

	public String getMaskedPassword() {
		final char[] array = new char[actualPasswordLength];
		Arrays.fill(array, '*');
		return new String(array);
	}

	public int getActualPasswordLength() {
		return actualPasswordLength;
	}

	public String getIgnoredProxyHosts() {
		return ignoredProxyHosts;
	}


}
