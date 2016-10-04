package com.blackducksoftware.integration.hub.dataservices.extension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.ExtensionRestService;
import com.blackducksoftware.integration.hub.api.extension.ConfigurationItem;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.api.user.UserRestService;
import com.blackducksoftware.integration.hub.dataservices.AbstractDataService;
import com.blackducksoftware.integration.hub.dataservices.extension.items.UserConfigItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class ExtensionConfigDataService extends AbstractDataService {
	private final IntLogger logger;
	private final UserRestService userService;
	private final ExtensionRestService extensionRestService;

	public ExtensionConfigDataService(final IntLogger logger, final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser, final UserRestService userRestService,
			final ExtensionRestService extensionRestService) {
		super(restConnection, gson, jsonParser);
		this.logger = logger;
		this.userService = userRestService;
		this.extensionRestService = extensionRestService;
	}

	public Map<String, ConfigurationItem> getGlobalConfigMap(final String extensionId) {
		Map<String, ConfigurationItem> globalConfigMap = new HashMap<>();
		try {
			globalConfigMap = createGlobalConfigMap(extensionId);
		} catch (IOException | URISyntaxException | BDRestException e) {
			logger.error("Error creating global configurationMap", e);
		}
		return globalConfigMap;
	}

	public List<UserConfigItem> getUserConfigList(final String extensionId) {
		List<UserConfigItem> itemList = new LinkedList<>();
		try {
			final List<UserItem> userList = userService.getAllUsers();
			itemList = createUserConfigItemList(extensionId, userList);
		} catch (URISyntaxException | BDRestException | IOException | MissingUUIDException e) {
			logger.error("Error creating user configuration", e);
		}

		return itemList;
	}

	private Map<String, ConfigurationItem> createGlobalConfigMap(final String extensionId)
			throws IOException, URISyntaxException, BDRestException {
		final List<ConfigurationItem> itemList = extensionRestService.getGlobalOptions(extensionId);
		final Map<String, ConfigurationItem> itemMap = createConfigMap(itemList);
		return itemMap;
	}

	private List<UserConfigItem> createUserConfigItemList(final String extensionId, final List<UserItem> userList)
			throws IOException, URISyntaxException, BDRestException, MissingUUIDException {
		final List<UserConfigItem> itemList = new ArrayList<>(userList.size());
		for (final UserItem user : userList) {
			if (user.isActive()) { // only get active users extension config
				final Map<String, ConfigurationItem> configItems = getUserConfigOptions(extensionId, user);
				itemList.add(new UserConfigItem(user, configItems));
			}
		}
		return itemList;
	}

	private Map<String, ConfigurationItem> getUserConfigOptions(final String extensionId, final UserItem user)
			throws IOException, URISyntaxException, BDRestException, MissingUUIDException {
		// get user ID
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
}
