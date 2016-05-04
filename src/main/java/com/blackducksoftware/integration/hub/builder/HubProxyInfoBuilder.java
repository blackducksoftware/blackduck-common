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
package com.blackducksoftware.integration.hub.builder;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;

public class HubProxyInfoBuilder extends AbstractBuilder {

	public static final String MSG_PROXY_INVALID_CONFIG = "The proxy information not valid - please check the log for the specific issues.";
	public static final String ERROR_MSG_IGNORE_HOSTS_INVALID = "Proxy ignore hosts does not compile to a valid regular expression.";
	public static final String ERROR_MSG_CREDENTIALS_INVALID = "Proxy username and password must both be populated or both be empty.";
	public static final String ERROR_MSG_PROXY_PORT_INVALID = "Proxy port must be greater than 0.";
	public static final String ERROR_MSG_PROXY_HOST_REQUIRED = "Proxy port specified, but proxy host not specified.";
	public static final String WARN_MSG_PROXY_HOST_NOT_SPECIFIED = "The proxy host not specified.";

	private String host;
	private int port;
	private String username;
	private String password;
	private String ignoredProxyHosts;

	@Override
	public ValidationResult<HubProxyInfo> build() {
		final ValidationResult<HubProxyInfo> result = assertValid();
		String encryptedProxyPass = null;
		try {
			encryptedProxyPass = PasswordEncrypter.encrypt(password);
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final EncryptionException e) {
			e.printStackTrace();
		}
		new HubProxyInfo(host, port, username, encryptedProxyPass, password.length(), ignoredProxyHosts);
		return result;
	}

	@Override
	public ValidationResult<HubProxyInfo> assertValid() {
		final ValidationResult<HubProxyInfo> result = null;

		validatePort(result);

		validateCredentials(result);

		validateIgnoreHosts(result);

		// if (!valid) {
		// // throw new HubIntegrationException(MSG_PROXY_INVALID_CONFIG);
		// }
		return null;
	}

	public boolean validatePort(final ValidationResult<HubProxyInfo> result) {
		boolean valid = true;
		if (StringUtils.isBlank(host)) {
			logger.warn(WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		}
		if (StringUtils.isNotBlank(host) && port < 0) {
			logger.error(ERROR_MSG_PROXY_PORT_INVALID);
			valid = false;
		} else if (StringUtils.isBlank(host) && port > 0) {
			logger.error(ERROR_MSG_PROXY_HOST_REQUIRED);
			valid = false;
		}

		return valid;
	}

	public boolean validateCredentials(final ValidationResult<HubProxyInfo> result) {
		boolean valid = true;

		if (StringUtils.isBlank(host)) {
			logger.warn(WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		}

		if (StringUtils.isBlank(username) && StringUtils.isBlank(password)) {
			valid = true;
		} else if (StringUtils.isNotBlank(getUsername()) && StringUtils.isNotBlank(getPassword())) {
			valid = true;
		} else {
			logger.error(ERROR_MSG_CREDENTIALS_INVALID);
			valid = false;
		}

		return valid;
	}

	public boolean validateIgnoreHosts(final ValidationResult<HubProxyInfo> result) {
		boolean valid = true;

		if (StringUtils.isBlank(host)) {
			logger.warn(WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		}

		if (StringUtils.isNotBlank(ignoredProxyHosts)) {
			try {
				if (ignoredProxyHosts.contains(",")) {
					String[] ignoreHosts = null;
					ignoreHosts = ignoredProxyHosts.split(",");
					for (final String ignoreHost : ignoreHosts) {
						Pattern.compile(ignoreHost.trim());
					}
				} else {
					Pattern.compile(ignoredProxyHosts);
				}
			} catch (final PatternSyntaxException ex) {
				valid = false;
				logger.error(ERROR_MSG_IGNORE_HOSTS_INVALID);
			}
		}
		return valid;
	}

	public String getHost() {
		return host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	public void setPort(final String port, final Integer defaultPort) {
		this.port = stringToInteger(port, defaultPort);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getIgnoredProxyHosts() {
		return ignoredProxyHosts;
	}

	public void setIgnoredProxyHosts(final String ignoredProxyHosts) {
		this.ignoredProxyHosts = ignoredProxyHosts;
	}

}
