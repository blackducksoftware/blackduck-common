package com.blackducksoftware.integration.hub.global;

public enum HubProxyInfoFieldEnum implements GlobalFieldKey {

	PROXYHOST("hubProxyHost"),
	PROXYPORT("hubProxyPort"),
	PROXYUSERNAME("hubProxyUsername"),
	PROXYPASSWORD("hubProxyPassword"),
	NOPROXYHOSTS("hubNoProxyHosts");

	private String key;

	private HubProxyInfoFieldEnum(final String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
