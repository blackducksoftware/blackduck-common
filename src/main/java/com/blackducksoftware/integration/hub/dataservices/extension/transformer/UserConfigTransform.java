package com.blackducksoftware.integration.hub.dataservices.extension.transformer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.extension.ConfigurationItem;
import com.blackducksoftware.integration.hub.api.extension.ExtensionConfigRestService;
import com.blackducksoftware.integration.hub.api.extension.UserOptionLinkItem;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.api.user.UserRestService;
import com.blackducksoftware.integration.hub.dataservices.ItemTransform;
import com.blackducksoftware.integration.hub.dataservices.extension.item.UserConfigItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;

public class UserConfigTransform implements ItemTransform<List<UserConfigItem>, UserOptionLinkItem> {
	private final UserRestService userRestService;
	private final ExtensionConfigRestService extensionConfigRestService;

	public UserConfigTransform(final UserRestService userRestService,
			final ExtensionConfigRestService extensionConfigRestService) {
		this.userRestService = userRestService;
		this.extensionConfigRestService = extensionConfigRestService;
	}

	@Override
	public List<UserConfigItem> transform(final UserOptionLinkItem item) throws HubItemTransformException {
		try {
			final UserItem user = userRestService.getItem(item.getUser());
			if (!user.isActive()) {
				return Collections.emptyList();
			} else {
				final Map<String, ConfigurationItem> configItems = getUserConfigOptions(item.getExtensionOptions());
				final List<UserConfigItem> itemList = new ArrayList<>(configItems.size());
				itemList.add(new UserConfigItem(user, configItems));
				return itemList;
			}
		} catch (final IOException | URISyntaxException | BDRestException | MissingUUIDException e) {
			throw new HubItemTransformException("Error processing user config for extension", e);
		}
	}

	private Map<String, ConfigurationItem> getUserConfigOptions(final String userConfigUrl)
			throws IOException, URISyntaxException, BDRestException, MissingUUIDException {
		final List<ConfigurationItem> userItemList = extensionConfigRestService.getUserConfiguration(userConfigUrl);
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
}
