package com.blackducksoftware.integration.hub;

import java.util.HashMap;
import java.util.Map;

public class CIEnvironmentVariables {
	public static final String BDS_CACERTS_OVERRIDE = "BDS_CACERTS_OVERRIDE";

	private static final Map<String, String> environmentVariables = new HashMap<String, String>();

	public void putAll(final Map<String, String> map) {
		CIEnvironmentVariables.environmentVariables.putAll(map);
	}

	public static boolean containsKey(final String key) {
		return environmentVariables.containsKey(key);
	}

	public static String getValue(final String key) {
		return environmentVariables.get(key);
	}

}
