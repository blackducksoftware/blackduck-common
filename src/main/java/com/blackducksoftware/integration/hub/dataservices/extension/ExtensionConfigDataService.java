package com.blackducksoftware.integration.hub.dataservices.extension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.extension.ConfigurationItem;
import com.blackducksoftware.integration.hub.api.extension.ExtensionRestService;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.api.user.UserRestService;
import com.blackducksoftware.integration.hub.dataservices.AbstractDataService;
import com.blackducksoftware.integration.hub.dataservices.extension.item.UserConfigItem;
import com.blackducksoftware.integration.hub.dataservices.extension.transformer.UserConfigTransform;
import com.blackducksoftware.integration.hub.dataservices.parallel.ParallelResourceProcessor;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class ExtensionConfigDataService extends AbstractDataService {
	private final IntLogger logger;
	private final UserRestService userService;
	private final ExtensionRestService extensionRestService;
	private final UserConfigTransform userConfigTransform;

	private final ParallelResourceProcessor<UserConfigItem, UserItem> parallelProcessor;

	public ExtensionConfigDataService(final IntLogger logger, final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser, final UserRestService userRestService,
			final ExtensionRestService extensionRestService) {
		super(restConnection, gson, jsonParser);
		this.logger = logger;
		this.userService = userRestService;
		this.extensionRestService = extensionRestService;
		userConfigTransform = new UserConfigTransform(extensionRestService);
		parallelProcessor = new ParallelResourceProcessor<>(logger);
		parallelProcessor.addTransform(UserItem.class, userConfigTransform);

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
			userConfigTransform.setExtensionId(extensionId);
			itemList = parallelProcessor.process(userList);
		} catch (URISyntaxException | BDRestException | IOException e) {
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

	private Map<String, ConfigurationItem> createConfigMap(final List<ConfigurationItem> itemList) {
		final Map<String, ConfigurationItem> itemMap = new HashMap<>(itemList.size());
		for (final ConfigurationItem item : itemList) {
			itemMap.put(item.getName(), item);
		}
		return itemMap;
	}
}
