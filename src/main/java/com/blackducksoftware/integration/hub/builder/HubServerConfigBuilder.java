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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.global.HubServerConfigFieldEnum;

public class HubServerConfigBuilder extends AbstractBuilder<GlobalFieldKey, HubServerConfig> {

	public static final String ERROR_MSG_URL_NOT_FOUND = "No Hub Url was found.";
	public static final String ERROR_MSG_URL_NOT_VALID_PREFIX = "This is not a valid URL : ";
	public static final String ERROR_MSG_UNREACHABLE_PREFIX = "Can not reach this server : ";
	public static final String ERROR_MSG_URL_NOT_VALID = "The Hub Url is not a valid URL.";

	public static int DEFAULT_TIMEOUT = 120;

	private String hubUrl;
	private int timeout;
	private String username;
	private String password;
	private int passwordLength;
	private String proxyHost;
	private int proxyPort;
	private String proxyUsername;
	private String proxyPassword;
	private int proxyPasswordLength;
	private String ignoredProxyHosts;

	HubProxyInfo proxyInfo;
	HubCredentials credentials;

	public HubServerConfigBuilder() {
		super(false);
	}

	public HubServerConfigBuilder(final boolean eatExceptionsOnSetters) {
		super(eatExceptionsOnSetters);
	}

	@Override
	public ValidationResults<GlobalFieldKey, HubServerConfig> build() {
		final ValidationResults<GlobalFieldKey, HubProxyInfo> proxyResult = assertProxyValid();
		final ValidationResults<GlobalFieldKey, HubCredentials> credentialResult = assertCredentialsValid();
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = assertValid();
		result.addAllResults(proxyResult.getResultMap());
		result.addAllResults(credentialResult.getResultMap());

		URL hubURL = null;
		try {
			hubURL = new URL(hubUrl);
		} catch (final MalformedURLException e) {
		}
		final HubServerConfig config = new HubServerConfig(hubURL, timeout, credentials, proxyInfo);
		result.setConstructedObject(config);
		return result;
	}

	@Override
	public ValidationResults<GlobalFieldKey, HubServerConfig> assertValid() {
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		validateHubUrl(result);
		validateTimeout(result, DEFAULT_TIMEOUT);
		return result;
	}

	public ValidationResults<GlobalFieldKey, HubProxyInfo> assertProxyValid() {
		ValidationResults<GlobalFieldKey, HubProxyInfo> result = null;
		final HubProxyInfoBuilder proxyBuilder = new HubProxyInfoBuilder(shouldEatExceptionsOnSetters());
		proxyBuilder.setHost(proxyHost);
		proxyBuilder.setPort(proxyPort);
		proxyBuilder.setIgnoredProxyHosts(ignoredProxyHosts);
		proxyBuilder.setUsername(proxyUsername);
		proxyBuilder.setPassword(proxyPassword);
		proxyBuilder.setPasswordLength(proxyPasswordLength);
		result = proxyBuilder.build();
		proxyInfo = result.getConstructedObject();
		return result;
	}

	public ValidationResults<GlobalFieldKey, HubCredentials> assertCredentialsValid() {
		ValidationResults<GlobalFieldKey, HubCredentials> result = null;
		final HubCredentialsBuilder credentialsBuilder = new HubCredentialsBuilder(shouldEatExceptionsOnSetters());
		credentialsBuilder.setUsername(username);
		credentialsBuilder.setPassword(password);
		credentialsBuilder.setPasswordLength(passwordLength);
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
		if (defaultTimeout != null && timeout <= 0) {
			timeout = defaultTimeout;
		} else if (timeout <= 0) {
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
		setTimeout(stringToInteger(timeout));
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(final int timeout) {
		this.timeout = timeout;
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

	public void setPasswordLength(final int passwordLength) {
		this.passwordLength = passwordLength;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(final String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(final int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public void setProxyPort(final String proxyPort) {
		setProxyPort(stringToInteger(proxyPort));
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
