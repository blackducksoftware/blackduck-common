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
package com.blackducksoftware.integration.hub.builder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubProxyInfoFieldEnum;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.global.HubServerConfigFieldEnum;

public class HubServerConfigBuilder extends AbstractBuilder<GlobalFieldKey, HubServerConfig> {

	public static final String ERROR_MSG_URL_NOT_FOUND = "No Hub Url was found.";
	public static final String ERROR_MSG_URL_NOT_VALID_PREFIX = "This is not a valid URL : ";
	public static final String ERROR_MSG_UNREACHABLE_PREFIX = "Can not reach this server : ";
	public static final String ERROR_MSG_URL_NOT_VALID = "The Hub Url is not a valid URL.";

	public static final String ERROR_MSG_AUTHENTICATED_PROXY_WITH_HTTPS = "Using an authenticated proxy to connect to an http Hub server is not supported.";

	public static int DEFAULT_TIMEOUT = 120;

	private String hubUrl;
	private String timeout;
	private String username;
	private String password;
	private int passwordLength;
	private String proxyHost;
	private String proxyPort;
	private String proxyUsername;
	private String proxyPassword;
	private int proxyPasswordLength;
	private String ignoredProxyHosts;

	HubProxyInfo proxyInfo;
	HubCredentials credentials;

	public HubServerConfigBuilder() {
		super(false);
	}

	public HubServerConfigBuilder(final boolean shouldUseDefaultValues) {
		super(shouldUseDefaultValues);
	}

	@Override
	public ValidationResults<GlobalFieldKey, HubServerConfig> build() {
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = assertValid();

		URL hubURL = null;
		try {
			hubURL = new URL(hubUrl);
		} catch (final MalformedURLException e) {
		}
		final HubServerConfig config = new HubServerConfig(hubURL, NumberUtils.toInt(timeout), credentials, proxyInfo);
		result.setConstructedObject(config);
		return result;
	}

	@Override
	public ValidationResults<GlobalFieldKey, HubServerConfig> assertValid() {
		final ValidationResults<GlobalFieldKey, HubProxyInfo> proxyResult = assertProxyValid();
		final ValidationResults<GlobalFieldKey, HubCredentials> credentialResult = assertCredentialsValid();
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		result.addAllResults(proxyResult.getResultMap());
		result.addAllResults(credentialResult.getResultMap());
		validateHubUrl(result);
		if (shouldUseDefaultValues()) {
			validateTimeout(result, DEFAULT_TIMEOUT);
		} else {
			validateTimeout(result, null);
		}
		return result;
	}

	public ValidationResults<GlobalFieldKey, HubProxyInfo> assertProxyValid() {
		ValidationResults<GlobalFieldKey, HubProxyInfo> result = null;
		final HubProxyInfoBuilder proxyBuilder = new HubProxyInfoBuilder(shouldUseDefaultValues());
		proxyBuilder.setHost(proxyHost);
		proxyBuilder.setPort(proxyPort);
		proxyBuilder.setIgnoredProxyHosts(ignoredProxyHosts);
		proxyBuilder.setUsername(proxyUsername);
		proxyBuilder.setPassword(proxyPassword);
		if (proxyPasswordLength > 0) {
			proxyBuilder.setPasswordLength(proxyPasswordLength);
		}
		result = proxyBuilder.build();
		proxyInfo = result.getConstructedObject();
		return result;
	}

	public ValidationResults<GlobalFieldKey, HubCredentials> assertCredentialsValid() {
		ValidationResults<GlobalFieldKey, HubCredentials> result = null;
		final HubCredentialsBuilder credentialsBuilder = new HubCredentialsBuilder(shouldUseDefaultValues());
		credentialsBuilder.setUsername(username);
		credentialsBuilder.setPassword(password);
		if (passwordLength > 0) {
			credentialsBuilder.setPasswordLength(passwordLength);
		}
		result = credentialsBuilder.build();
		credentials = result.getConstructedObject();
		return result;
	}

	public void validateHubUrl(final ValidationResults<GlobalFieldKey, HubServerConfig> result) {
		assertProxyValid();
		if (hubUrl == null) {
			result.addResult(HubServerConfigFieldEnum.HUBURL,
					new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_URL_NOT_FOUND));
			return;
		}

		URL hubURL = null;
		try {
			hubURL = new URL(hubUrl);
			hubURL.toURI();
		} catch (final MalformedURLException e) {
			result.addResult(HubServerConfigFieldEnum.HUBURL,
					new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_URL_NOT_VALID));
		} catch (final URISyntaxException e) {
			result.addResult(HubServerConfigFieldEnum.HUBURL,
					new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_URL_NOT_VALID));
		}

		if (hubURL == null) {
			return;
		}

		try {
			URLConnection connection = null;
			if (null != proxyInfo) {
				if (!hubURL.getProtocol().equals("https") && proxyInfo.getUsername() != null
						&& proxyInfo.getEncryptedPassword() != null) {
					result.addResult(HubProxyInfoFieldEnum.PROXYUSERNAME,
							new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_AUTHENTICATED_PROXY_WITH_HTTPS));
					return;
				}
				connection = proxyInfo.openConnection(hubURL);
			} else {
				connection = hubURL.openConnection();
			}
			connection.getContent();
		} catch (final IOException ioe) {
			result.addResult(HubServerConfigFieldEnum.HUBURL, new ValidationResult(ValidationResultEnum.ERROR,
					ERROR_MSG_UNREACHABLE_PREFIX + hubUrl, ioe));
			return;
		} catch (final RuntimeException e) {
			result.addResult(HubServerConfigFieldEnum.HUBURL, new ValidationResult(ValidationResultEnum.ERROR,
					ERROR_MSG_URL_NOT_VALID_PREFIX + hubUrl, e));
			return;
		}

		result.addResult(HubServerConfigFieldEnum.HUBURL, new ValidationResult(ValidationResultEnum.OK, ""));
	}

	public void validateTimeout(final ValidationResults<GlobalFieldKey, HubServerConfig> result) {
		validateTimeout(result, null);
	}

	private void validateTimeout(final ValidationResults<GlobalFieldKey, HubServerConfig> result,
			final Integer defaultTimeout) {
		if (shouldUseDefaultValues() && defaultTimeout != null) {
			int timeoutToValidate = 0;
			try {
				timeoutToValidate = stringToInteger(timeout);
			} catch (final IllegalArgumentException e) {
				timeout = String.valueOf(defaultTimeout);
			}
			if (timeoutToValidate <= 0) {
				timeout = String.valueOf(defaultTimeout);
			}
			return;
		}
		if (StringUtils.isBlank(timeout)) {
			result.addResult(HubServerConfigFieldEnum.HUBTIMEOUT,
					new ValidationResult(ValidationResultEnum.ERROR, "No Hub Timeout was found."));
			return;
		}
		int timeoutToValidate = 0;
		try {
			timeoutToValidate = stringToInteger(timeout);
		} catch (final IllegalArgumentException e) {
			result.addResult(HubServerConfigFieldEnum.HUBTIMEOUT,
					new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
			return;
		}
		if (timeoutToValidate <= 0) {
			result.addResult(HubServerConfigFieldEnum.HUBTIMEOUT,
					new ValidationResult(ValidationResultEnum.ERROR, "The Timeout must be greater than 0."));
		} else {
			result.addResult(HubServerConfigFieldEnum.HUBTIMEOUT, new ValidationResult(ValidationResultEnum.OK, ""));
		}
	}

	public void setHubUrl(final String hubUrl) {
		this.hubUrl = StringUtils.trimToNull(hubUrl);
	}

	public void setTimeout(final String timeout) {
		this.timeout = timeout;
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(final int timeout) {
		setTimeout(String.valueOf(timeout));
	}

	public String getHubUrl() {
		return hubUrl;
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

	public int getPasswordLength() {
		return passwordLength;
	}

	/**
	 * IMPORTANT : The password length should only be set if the password is
	 * already encrypted
	 */
	public void setPasswordLength(final int passwordLength) {
		this.passwordLength = passwordLength;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(final String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(final int proxyPort) {
		setProxyPort(String.valueOf(proxyPort));
	}

	public void setProxyPort(final String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public void setProxyUsername(final String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(final String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public int getProxyPasswordLength() {
		return proxyPasswordLength;
	}

	/**
	 * IMPORTANT : The proxy password length should only be set if the proxy password is
	 * already encrypted
	 */
	public void setProxyPasswordLength(final int proxyPasswordLength) {
		this.proxyPasswordLength = proxyPasswordLength;
	}

	public String getIgnoredProxyHosts() {
		return ignoredProxyHosts;
	}

	public void setIgnoredProxyHosts(final String ignoredProxyHosts) {
		this.ignoredProxyHosts = ignoredProxyHosts;
	}


}
