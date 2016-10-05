package com.blackducksoftware.integration.hub.dataservices.extension.transformer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.extension.ConfigurationItem;
import com.blackducksoftware.integration.hub.api.extension.ExtensionRestService;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.dataservices.ItemTransform;
import com.blackducksoftware.integration.hub.dataservices.extension.item.UserConfigItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;

public class UserConfigTransform implements ItemTransform<List<UserConfigItem>, UserItem> {
	private String extensionId;
	private final ExtensionRestService extensionRestService;

	public UserConfigTransform(final ExtensionRestService extensionRestService) {
		this.extensionRestService = extensionRestService;
	}

	@Override
	public List<UserConfigItem> transform(final UserItem item) throws HubItemTransformException {
		try {
			if (!item.isActive()) {
				return Collections.emptyList();
			} else {
				final Map<String, ConfigurationItem> configItems = getUserConfigOptions(extensionId, item);
				final List<UserConfigItem> itemList = new ArrayList<>(configItems.size());
				itemList.add(new UserConfigItem(item, configItems));
				return itemList;
			}
		} catch (final IOException | URISyntaxException | BDRestException | MissingUUIDException e) {
			throw new HubItemTransformException("Error processing user config for extension", e);
		}
	}

	private Map<String, ConfigurationItem> getUserConfigOptions(final String extensionId, final UserItem user)
			throws IOException, URISyntaxException, BDRestException, MissingUUIDException {
		final String userId = user.getUserId().toString();
		final List<ConfigurationItem> userItemList = extensionRestService.getUserConfiguration(extensionId, userId);
		final Map<String, ConfigurationItem> itemMap = createConfigMap(userItemList);
		return itemMap;
	}

	private Map<String, ConfigurationItem> createConfigMap(final List<ConfigurationItem> itemList) {
		final Map<String, ConfigurationItem> itemMap = new HashMap<>(itemList.size());
		for (final ConfigurationItem item : itemList) {
			itemMap.put(item.getName(), item);
		}
		return itemMap;
	}

	public String getExtensionId() {
		return extensionId;
	}

	public void setExtensionId(final String extensionId) {
		this.extensionId = extensionId;
	}
}
