package com.blackducksoftware.integration.hub.global;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public class HubProxyInfoBuilder {
	public static final String MSG_PROXY_INVALID_CONFIG = "The proxy information not valid - please check the log for the specific issues.";
	public static final String ERROR_MSG_IGNORE_HOSTS_INVALID = "Proxy ignore hosts does not compile to a valid regular expression.";
	public static final String ERROR_MSG_CREDENTIALS_INVALID = "Proxy username and password must both be populated or both be empty.";
	public static final String ERROR_MSG_PROXY_PORT_INVALID = "Proxy port must be greater than 0.";
	public static final String WARN_MSG_PROXY_HOST_NOT_SPECIFIED = "The proxy host not specified.";

	private String host;
	private int port;
	private String username;
	private String password;
	private String ignoredProxyHosts;

	public HubProxyInfo build(final IntLogger logger)
			throws IllegalArgumentException, EncryptionException, HubIntegrationException {
		assertValid(logger);
		return new HubProxyInfo(host, port, username, password, ignoredProxyHosts);
	}

	public void assertValid(final IntLogger logger) throws HubIntegrationException {
		boolean valid = true;

		if (!validatePort(logger)) {
			valid = false;
		}

		if (!validateCredentials(logger)) {
			valid = false;
		}

		if (!validateIgnoreHosts(logger)) {
			valid = false;
		}

		if (!valid) {
			throw new HubIntegrationException(MSG_PROXY_INVALID_CONFIG);
		}
	}

	public boolean validatePort(final IntLogger logger) {
		boolean valid = true;
		if (StringUtils.isBlank(host)) {
			logger.warn(WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		}
		if (StringUtils.isNotBlank(host) && port <= 0) {
			logger.error(ERROR_MSG_PROXY_PORT_INVALID);
			valid = false;
		}
		return valid;
	}

	public boolean validateCredentials(final IntLogger logger) {
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

	public boolean validateIgnoreHosts(final IntLogger logger) {
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
