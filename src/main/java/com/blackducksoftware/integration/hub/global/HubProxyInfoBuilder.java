package com.blackducksoftware.integration.hub.global;

import com.blackducksoftware.integration.hub.exception.EncryptionException;

public class HubProxyInfoBuilder {
	private String host;
	private int port;
	private String username;
	private String password;
	private String ignoredProxyHosts;

	public HubProxyInfo build() throws IllegalArgumentException, EncryptionException {
		return new HubProxyInfo(host, port, username, password, ignoredProxyHosts);
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
