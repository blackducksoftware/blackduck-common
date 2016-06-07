package com.blackducksoftware.integration.hub;

import java.util.HashMap;
import java.util.Map;

public class CIEnvironmentVariables {
	public static final String BDS_CACERTS_OVERRIDE = "BDS_CACERTS_OVERRIDE";

	private final Map<String, String> environmentVariables = new HashMap<String, String>();

	public void putAll(final Map<String, String> map) {
		environmentVariables.putAll(map);
	}

	public void put(final String key, final String value) {
		environmentVariables.put(key, value);
	}

	public boolean containsKey(final String key) {
		return environmentVariables.containsKey(key);
	}

	public String getValue(final String key) {
		return environmentVariables.get(key);
	}

}
