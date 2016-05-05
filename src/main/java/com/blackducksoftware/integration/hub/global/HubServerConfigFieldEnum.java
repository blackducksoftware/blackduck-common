package com.blackducksoftware.integration.hub.global;

public enum HubServerConfigFieldEnum {

	HUBURL("hubUrl"), CREDENTIALS("hubCredentials"), PROXYINFO("hubProxyInfo"), HUBTIMEOUT("hubTimeout");

	private String key;

	private HubServerConfigFieldEnum(final String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
