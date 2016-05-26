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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public class HubServerConfigBuilder {
	public static final String ERROR_MSG_URL_NOT_FOUND = "No Hub Url was found.";
	public static final String ERROR_MSG_URL_NOT_VALID_PREFIX = "This is not a valid URL : ";
	public static final String ERROR_MSG_UNREACHABLE_PREFIX = "Can not reach this server : ";
	public static final String ERROR_MSG_URL_NOT_VALID = "The Hub Url is not a valid URL.";

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
			logger.error(ERROR_MSG_URL_NOT_FOUND);
			return false;
		}

		URL hubURL = null;
		try {
			hubURL = new URL(hubUrl);
			hubURL.toURI();
		} catch (final MalformedURLException e) {
			logger.error(ERROR_MSG_URL_NOT_VALID);
			valid = false;
		} catch (final URISyntaxException e) {
			logger.error(ERROR_MSG_URL_NOT_VALID);
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
			logger.error(ERROR_MSG_UNREACHABLE_PREFIX + hubUrl, ioe);
			valid = false;
		} catch (final RuntimeException e) {
			logger.error(ERROR_MSG_URL_NOT_VALID_PREFIX + hubUrl, e);
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
