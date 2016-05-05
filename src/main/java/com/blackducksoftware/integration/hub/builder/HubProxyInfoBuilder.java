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
import com.blackducksoftware.integration.hub.global.HubProxyInfoFieldEnum;

public class HubProxyInfoBuilder extends AbstractBuilder<HubProxyInfoFieldEnum, HubProxyInfo> {

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

	public HubProxyInfoBuilder() {
		super(false);
	}

	public HubProxyInfoBuilder(final boolean eatExceptionsOnSetters) {
		super(eatExceptionsOnSetters);
	}

	@Override
	public ValidationResults<HubProxyInfoFieldEnum, HubProxyInfo> build() {
		final ValidationResults<HubProxyInfoFieldEnum, HubProxyInfo> result = assertValid();
		HubProxyInfo proxyInfo;
		if (StringUtils.isNotBlank(password)) {
			String encryptedProxyPass = null;
			try {
				encryptedProxyPass = PasswordEncrypter.encrypt(password);
			} catch (final IllegalArgumentException e) {
				e.printStackTrace();
			} catch (final EncryptionException e) {
				e.printStackTrace();
			}
			proxyInfo = new HubProxyInfo(host, port, username, encryptedProxyPass, password.length(),
					ignoredProxyHosts);
		} else {

			proxyInfo = new HubProxyInfo(host, port, username, null, 0, ignoredProxyHosts);
		}

		result.setConstructedObject(proxyInfo);
		return result;
	}

	@Override
	public ValidationResults<HubProxyInfoFieldEnum, HubProxyInfo> assertValid() {
		final ValidationResults<HubProxyInfoFieldEnum, HubProxyInfo> result = new ValidationResults<HubProxyInfoFieldEnum, HubProxyInfo>();

		validatePort(result);

		validateCredentials(result);

		validateIgnoreHosts(result);

		return result;
	}

	public boolean validatePort(final ValidationResults<HubProxyInfoFieldEnum, HubProxyInfo> result) {
		boolean valid = true;
		if (StringUtils.isBlank(host)) {
			result.addResult(HubProxyInfoFieldEnum.PROXYHOST,
					new ValidationResult(ValidationResultEnum.WARN, WARN_MSG_PROXY_HOST_NOT_SPECIFIED));
		}
		if (StringUtils.isNotBlank(host) && port < 0) {
			result.addResult(HubProxyInfoFieldEnum.PROXYPORT,
					new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_PROXY_PORT_INVALID));
			valid = false;
		} else if (StringUtils.isBlank(host) && port > 0) {
			result.addResult(HubProxyInfoFieldEnum.PROXYPORT,
					new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_PROXY_HOST_REQUIRED));
			valid = false;
		} else {
			result.addResult(HubProxyInfoFieldEnum.PROXYPORT, new ValidationResult(ValidationResultEnum.OK, ""));
		}

		return valid;
	}

	public boolean validateCredentials(final ValidationResults<HubProxyInfoFieldEnum, HubProxyInfo> result) {
		boolean valid = true;

		if (StringUtils.isBlank(host)) {
			result.addResult(HubProxyInfoFieldEnum.PROXYHOST,
					new ValidationResult(ValidationResultEnum.WARN, WARN_MSG_PROXY_HOST_NOT_SPECIFIED));
		}

		if (StringUtils.isBlank(username) && StringUtils.isBlank(password)) {
			valid = true;
			result.addResult(HubProxyInfoFieldEnum.PROXYUSERNAME, new ValidationResult(ValidationResultEnum.OK, ""));
			result.addResult(HubProxyInfoFieldEnum.PROXYPASSWORD, new ValidationResult(ValidationResultEnum.OK, ""));
		} else if (StringUtils.isNotBlank(getUsername()) && StringUtils.isNotBlank(getPassword())) {
			valid = true;
			result.addResult(HubProxyInfoFieldEnum.PROXYUSERNAME, new ValidationResult(ValidationResultEnum.OK, ""));
			result.addResult(HubProxyInfoFieldEnum.PROXYPASSWORD, new ValidationResult(ValidationResultEnum.OK, ""));
		} else {
			result.addResult(HubProxyInfoFieldEnum.PROXYUSERNAME,
					new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_CREDENTIALS_INVALID));
			result.addResult(HubProxyInfoFieldEnum.PROXYPASSWORD,
					new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_CREDENTIALS_INVALID));
			valid = false;
		}

		return valid;
	}

	public boolean validateIgnoreHosts(final ValidationResults<HubProxyInfoFieldEnum, HubProxyInfo> result) {
		boolean valid = true;

		if (StringUtils.isBlank(host)) {
			result.addResult(HubProxyInfoFieldEnum.PROXYHOST,
					new ValidationResult(ValidationResultEnum.WARN, WARN_MSG_PROXY_HOST_NOT_SPECIFIED));
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
				result.addResult(HubProxyInfoFieldEnum.NOPROXYHOSTS,
						new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_IGNORE_HOSTS_INVALID));
			}
		}

		if (valid) {
			result.addResult(HubProxyInfoFieldEnum.NOPROXYHOSTS, new ValidationResult(ValidationResultEnum.OK, ""));
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

	public void setPort(final String port) {
		this.port = stringToInteger(port);
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
