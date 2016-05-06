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

import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.global.HubServerConfigFieldEnum;

public class HubServerConfigBuilder extends AbstractBuilder<HubServerConfigFieldEnum, HubServerConfig> {

	public static final String ERROR_MSG_URL_NOT_FOUND = "No Hub Url was found.";
	public static final String ERROR_MSG_URL_NOT_VALID_PREFIX = "This is not a valid URL : ";
	public static final String ERROR_MSG_UNREACHABLE_PREFIX = "Can not reach this server : ";
	public static final String ERROR_MSG_URL_NOT_VALID = "The Hub Url is not a valid URL.";

	public static int DEFAULT_TIMEOUT = 120;

	private String hubUrl;
	private int timeout;
	private HubCredentials credentials;
	private HubProxyInfo proxyInfo;

	public HubServerConfigBuilder() {
		super(false);
	}

	public HubServerConfigBuilder(final boolean eatExceptionsOnSetters) {
		super(eatExceptionsOnSetters);
	}

	@Override
	public ValidationResults<HubServerConfigFieldEnum, HubServerConfig> build() {
		final ValidationResults<HubServerConfigFieldEnum, HubServerConfig> result = assertValid();
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
	public ValidationResults<HubServerConfigFieldEnum, HubServerConfig> assertValid() {
		final ValidationResults<HubServerConfigFieldEnum, HubServerConfig> result = new ValidationResults<HubServerConfigFieldEnum, HubServerConfig>();
		validateHubUrl(result);
		validateTimeout(result, DEFAULT_TIMEOUT);

		return result;
	}

	public void validateHubUrl(final ValidationResults<HubServerConfigFieldEnum, HubServerConfig> result) {
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

	public void validateTimeout(final ValidationResults<HubServerConfigFieldEnum, HubServerConfig> result) {
		validateTimeout(result, null);
	}

	private void validateTimeout(final ValidationResults<HubServerConfigFieldEnum, HubServerConfig> result,
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

}
