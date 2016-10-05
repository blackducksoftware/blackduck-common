package com.blackducksoftware.integration.hub.dataservices.extension.item;

import java.util.Map;

import com.blackducksoftware.integration.hub.api.extension.ConfigurationItem;
import com.blackducksoftware.integration.hub.api.user.UserItem;

public class UserConfigItem {
	private final UserItem user;
	private final Map<String, ConfigurationItem> configMap;

	public UserConfigItem(final UserItem user, final Map<String, ConfigurationItem> configItems) {
		this.user = user;
		this.configMap = configItems;
	}

	public UserItem getUser() {
		return user;
	}

	public Map<String, ConfigurationItem> getConfigMap() {
		return configMap;
	}
}
