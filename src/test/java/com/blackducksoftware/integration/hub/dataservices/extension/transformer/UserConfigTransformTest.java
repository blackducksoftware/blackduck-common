package com.blackducksoftware.integration.hub.dataservices.extension.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.api.extension.ConfigurationItem;
import com.blackducksoftware.integration.hub.api.extension.ExtensionRestService;
import com.blackducksoftware.integration.hub.api.extension.OptionItem;
import com.blackducksoftware.integration.hub.api.extension.OptionTypeEnum;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.api.user.UserType;
import com.blackducksoftware.integration.hub.dataservices.extension.item.UserConfigItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class UserConfigTransformTest {

	private UserItem createUserItem() {
		final String id = UUID.randomUUID().toString();
		final MetaInformation meta = new MetaInformation(null, "http://localhost/api/users/" + id, null);
		return new UserItem(meta, "username", "firstName", "lastName", "user@blackducksoftware.com", UserType.INTERNAL,
				true);
	}

	private List<ConfigurationItem> createConfigurationItemList() {
		final MetaInformation meta = new MetaInformation(null, null, null);
		List<String> valueList = new ArrayList<>();
		valueList.add("a value");
		final List<OptionItem> options = new ArrayList<>();
		final ConfigurationItem item1 = new ConfigurationItem(meta, "item1", OptionTypeEnum.STRING, "itemTitle", true,
				true, "a description", options, valueList);
		valueList = new ArrayList<>();
		valueList.add("another value");
		final ConfigurationItem item2 = new ConfigurationItem(meta, "item2", OptionTypeEnum.STRING, "itemTitle", true,
				true, "another description", options, valueList);
		final List<ConfigurationItem> itemList = new ArrayList<>(2);
		itemList.add(item1);
		itemList.add(item2);
		return itemList;
	}

	@Test
	public void testTransform() throws Exception {
		final List<ConfigurationItem> configItemList = createConfigurationItemList();
		final ExtensionRestService extensionRestService = Mockito.mock(ExtensionRestService.class);
		Mockito.when(extensionRestService.getUserConfiguration(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(configItemList);

		final UserConfigTransform converter = new UserConfigTransform(extensionRestService);
		final UserItem user = createUserItem();
		final List<UserConfigItem> result = converter.transform(user);

		assertNotNull(result);
		assertEquals(1, result.size());
		final UserConfigItem userConfigItem = result.get(0);
		assertEquals(user, userConfigItem.getUser());

		for (final ConfigurationItem configItem : configItemList) {
			final String key = configItem.getName();
			assertTrue(userConfigItem.getConfigMap().containsKey(key));
			assertEquals(configItem, userConfigItem.getConfigMap().get(key));
		}
	}
}
