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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public class HubServerConfigBuilder {
	public static int DEFAULT_TIMEOUT = 120;

	private String hubUrl;
	private int timeout;
	private HubCredentials credentials;
	private HubProxyInfo proxyInfo;

	public HubServerConfig build(final IntLogger logger) throws HubIntegrationException, MalformedURLException {
		assertValid(logger);

		return new HubServerConfig(new URL(hubUrl), timeout, credentials, proxyInfo);
	}

	public void assertValid(final IntLogger logger) throws HubIntegrationException {
		boolean valid = true;

		if (!validateHubUrl(logger)) {
			valid = false;
		}

		if (!validateTimeout(logger, DEFAULT_TIMEOUT)) {
			valid = false;
		}

		if (!valid) {
			throw new HubIntegrationException(
					"The server configuration is not valid - please check the log for the specific issues.");
		}
	}

	public boolean validateHubUrl(final IntLogger logger) {
		boolean valid = true;
		if (hubUrl == null) {
			logger.error("No Hub Url was found.");
			return false;
		}

		URL hubURL = null;
		try {
			hubURL = new URL(hubUrl);
			hubURL.toURI();
		} catch (final MalformedURLException e) {
			logger.error("The Hub Url is not a valid URL.");
			valid = false;
		} catch (final URISyntaxException e) {
			logger.error("The Hub Url is not a valid URL.");
			valid = false;
		}

		if (hubURL == null) {
			return valid;
		}

		try {
			URLConnection connection = null;
			if (null != proxyInfo) {
				connection = proxyInfo.openConnection(hubURL);
			} else {
				connection = hubURL.openConnection();
			}
			connection.getContent();
		} catch (final IOException ioe) {
			logger.error("Can not reach this server : " + hubUrl, ioe);
			valid = false;
		} catch (final RuntimeException e) {
			logger.error("This is not a valid URL : " + hubUrl, e);
			valid = false;
		}

		return valid;
	}

	public boolean validateTimeout(final IntLogger logger) {
		return validateTimeout(logger, null);
	}

	private boolean validateTimeout(final IntLogger logger, final Integer defaultTimeout) {
		boolean valid = true;
		if (defaultTimeout != null && timeout <= 0) {
			timeout = defaultTimeout;
		} else if (timeout <= 0) {
			logger.error("The Timeout must be greater than 0.");
			valid = false;
		}
		return valid;
	}

	public void setHubUrl(final String hubUrl) {
		this.hubUrl = StringUtils.trimToNull(hubUrl);
	}

	public void setTimeout(final String timeout) {
		setTimeout(stringToInteger(timeout));
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	public HubCredentials getCredentials() {
		return credentials;
	}

	public void setCredentials(final HubCredentials credentials) {
		this.credentials = credentials;
	}

	public HubProxyInfo getProxyInfo() {
		return proxyInfo;
	}

	public void setProxyInfo(final HubProxyInfo proxyInfo) {
		this.proxyInfo = proxyInfo;
	}

	public String getHubUrl() {
		return hubUrl;
	}

	private int stringToInteger(final String integer) {
		final String integerString = StringUtils.trimToNull(integer);
		if (integerString != null) {
			try {
				return Integer.valueOf(integerString);
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("The String : " + integer + " , is not an Integer.", e);
			}
		}
		return 0;
	}

}
