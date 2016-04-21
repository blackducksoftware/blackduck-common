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
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public class HubServerConfigBuilder {
	public static int DEFAULT_TIMEOUT = 120;

	private String hubUrl;

	private String hubUser;

	private String hubPass;

	private int actualPasswordLength;

	private int timeout;

	private String proxyHost;

	private int proxyPort;

	private String proxyUsername;

	private String proxyPassword;

	private String ignoredProxyHosts;

	private List<Pattern> ignoredProxyHostPatterns;

	private boolean hubUrlIgnored;

	public HubServerConfig build(final IntLogger logger) throws HubIntegrationException, MalformedURLException {
		assertValid(logger);

		final HubProxyInfo proxyInfo = new HubProxyInfo(proxyHost, proxyPort, ignoredProxyHosts,
				ignoredProxyHostPatterns, proxyUsername, proxyPassword, hubUrlIgnored);
		final HubCredentials credentials = new HubCredentials(hubUser, hubPass, actualPasswordLength);
		return new HubServerConfig(new URL(hubUrl), timeout, credentials, proxyInfo);
	}

	public void assertValid(final IntLogger logger) throws HubIntegrationException {
		boolean valid = true;
		if (!validateProxyConfig(logger)) {
			valid = false;
		}
		if (!validateHubUrl(logger)) {
			valid = false;
		}
		if (!validateHubCredentials(logger)) {
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
			Proxy proxy = null;
			if (proxyHost != null && proxyPort != 0 && !hubUrlIgnored) {
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
				if (proxy != null && proxy != Proxy.NO_PROXY) {

					if (proxyUsername != null && proxyPassword != null) {
						Authenticator.setDefault(new Authenticator() {
							@Override
							public PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
							}
						});
					} else {
						Authenticator.setDefault(null);
					}
				}
			}
			URLConnection connection = null;
			if (proxy != null) {
				connection = hubURL.openConnection(proxy);
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

	public boolean validateHubCredentials(final IntLogger logger) {
		boolean valid = true;
		if (!validateHubUser(logger)) {
			valid = false;
		}
		if (!validateHubPassword(logger)) {
			valid = false;
		}
		return valid;
	}

	public boolean validateHubUser(final IntLogger logger) {
		boolean valid = true;
		if (null == hubUser) {
			valid = false;
			logger.error("No Hub Username was found.");
		}
		return valid;
	}

	public boolean validateHubPassword(final IntLogger logger) {
		boolean valid = true;
		if (null == hubPass) {
			valid = false;
			logger.error("No Hub Password was found.");
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

	public boolean validateProxyConfig(final IntLogger logger) {
		boolean valid = true;
		if (hubUrl != null && ignoredProxyHostPatterns != null && !ignoredProxyHostPatterns.isEmpty()) {
			try {
				final URL hubURL = new URL(hubUrl);
				if (checkMatchingNoProxyHostPatterns(hubURL.getHost(), ignoredProxyHostPatterns)) {
					setHubUrlIgnored(true);
				}
			} catch (final Exception e) {
				logger.error(e);
				valid = false;
			}
		}
		return valid;
	}

	public void setHubUrl(final String hubUrl) {
		this.hubUrl = StringUtils.trimToNull(hubUrl);
	}

	public void setHubUser(final String hubUser) {
		this.hubUser = StringUtils.trimToNull(hubUser);
	}

	public void setHubPass(final String hubPass)
			throws IllegalArgumentException, EncryptionException {
		final String password = StringUtils.trimToNull(hubPass);
		if (password != null) {
			setActualPasswordLength(password.length());
			this.hubPass = PasswordEncrypter.encrypt(password);
		}
	}

	public void setHubPassEncrypted(final String hubPassEncrypted, final int actualPasswordLength) {
		this.hubPass = hubPassEncrypted;
		setActualPasswordLength(actualPasswordLength);
	}

	private void setActualPasswordLength(final int actualPasswordLength) {
		this.actualPasswordLength = actualPasswordLength;
	}

	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	public void setTimeout(final String timeout) {
		setTimeout(stringToInteger(timeout));
	}

	public void setProxyHost(final String proxyHost) {
		this.proxyHost = StringUtils.trimToNull(proxyHost);
	}

	public void setProxyPort(final int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public void setProxyPort(final String proxyPort) {
		setProxyPort(stringToInteger(proxyPort));
	}

	public void setProxyUsername(final String proxyUsername) {
		this.proxyUsername = StringUtils.trimToNull(proxyUsername);
	}

	public void setProxyPassword(final String proxyPassword) {
		this.proxyPassword = StringUtils.trimToNull(proxyPassword);
	}

	public void setIgnoredProxyHosts(final String ignoredProxyHosts) {
		this.ignoredProxyHosts = StringUtils.trimToNull(ignoredProxyHosts);
		ignoredProxyHostPatterns = getNoProxyHostPatterns();
	}

	public void setIgnoredProxyHosts(final List<Pattern> noProxyHostsPatterns) {
		ignoredProxyHostPatterns = noProxyHostsPatterns;
		final StringBuilder builder = new StringBuilder();
		if (noProxyHostsPatterns != null && !noProxyHostsPatterns.isEmpty()) {
			for (final Pattern pattern : noProxyHostsPatterns) {
				if (builder.length() == 0) {
					builder.append(pattern.pattern());
				} else {
					builder.append("," + pattern.pattern());
				}
			}
		}
		ignoredProxyHosts = StringUtils.trimToNull(builder.toString());
	}

	public void setHubUrlIgnored(final boolean hubUrlIgnored) {
		this.hubUrlIgnored = hubUrlIgnored;
	}

	public boolean isHubUrlIgnored() {
		return hubUrlIgnored;
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

	public List<Pattern> getNoProxyHostPatterns() {
		final List<Pattern> noProxyHostsPatterns = new ArrayList<Pattern>();
		if (StringUtils.isNotBlank(ignoredProxyHosts)) {
			String[] ignoreHosts = null;
			if (StringUtils.isNotBlank(ignoredProxyHosts)) {
				if (ignoredProxyHosts.contains(",")) {
					ignoreHosts = ignoredProxyHosts.split(",");
					for (final String ignoreHost : ignoreHosts) {
						final Pattern pattern = Pattern.compile(ignoreHost.trim());
						noProxyHostsPatterns.add(pattern);
					}
				} else {
					final Pattern pattern = Pattern.compile(ignoredProxyHosts);
					noProxyHostsPatterns.add(pattern);
				}
			}
		}
		return noProxyHostsPatterns;
	}

	/**
	 * Checks the list of user defined host names that should be connected to
	 * directly and not go through the proxy. If the hostToMatch matches any of
	 * these hose names then this method returns true.
	 *
	 */
	public boolean checkMatchingNoProxyHostPatterns(final String hostToMatch, final List<Pattern> noProxyHosts) {
		if (noProxyHosts == null || noProxyHosts.isEmpty()) {
			// User did not specify any hosts to ignore the proxy
			return false;
		}
		boolean match = false;
		if (!StringUtils.isBlank(hostToMatch) && !noProxyHosts.isEmpty()) {

			for (final Pattern pattern : noProxyHosts) {
				final Matcher m = pattern.matcher(hostToMatch);
				while (m.find()) {
					match = true;
					break;
				}
				if (match) {
					break;
				}
			}
		}
		return match;
	}

}
