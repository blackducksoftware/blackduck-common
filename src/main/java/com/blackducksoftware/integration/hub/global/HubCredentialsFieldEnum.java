package com.blackducksoftware.integration.hub.global;

public enum HubCredentialsFieldEnum implements GlobalFieldKey {

	USERNAME("hubUsername"), PASSWORD("hubPassword");

	private String key;

	private HubCredentialsFieldEnum(final String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
