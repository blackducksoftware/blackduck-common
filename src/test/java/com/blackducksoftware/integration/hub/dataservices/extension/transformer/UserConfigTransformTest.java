/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
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
import com.blackducksoftware.integration.hub.api.extension.ExtensionConfigRestService;
import com.blackducksoftware.integration.hub.api.extension.OptionItem;
import com.blackducksoftware.integration.hub.api.extension.OptionTypeEnum;
import com.blackducksoftware.integration.hub.api.extension.UserOptionLinkItem;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.api.user.UserRestService;
import com.blackducksoftware.integration.hub.api.user.UserType;
import com.blackducksoftware.integration.hub.dataservices.extension.item.UserConfigItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class UserConfigTransformTest {

	private UserItem createUserItem(final boolean active) {
		final String id = UUID.randomUUID().toString();
		final MetaInformation meta = new MetaInformation(null, "http://localhost/api/users/" + id, null);
		return new UserItem(meta, "username", "firstName", "lastName", "user@blackducksoftware.com", UserType.INTERNAL,
				active);
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

	private UserOptionLinkItem userOptionItem;
	private UserItem user;
	private List<ConfigurationItem> configItemList;
	private UserRestService userRestService;
	private ExtensionConfigRestService extensionRestService;
	private UserConfigTransform converter;

	public void initTest(final boolean activeUser) throws Exception {
		userOptionItem = Mockito.mock(UserOptionLinkItem.class);
		user = createUserItem(activeUser);
		configItemList = createConfigurationItemList();
		userRestService = Mockito.mock(UserRestService.class);
		Mockito.when(userRestService.getItem(Mockito.anyString())).thenReturn(user);
		extensionRestService = Mockito.mock(ExtensionConfigRestService.class);
		Mockito.when(extensionRestService.getUserConfiguration(Mockito.anyString())).thenReturn(configItemList);
		converter = new UserConfigTransform(userRestService, extensionRestService);
	}

	@Test
	public void testTransform() throws Exception {
		initTest(true);
		final List<UserConfigItem> result = converter.transform(userOptionItem);
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

	@Test
	public void testTransformInactiveUser() throws Exception {
		initTest(false);
		final List<UserConfigItem> result = converter.transform(userOptionItem);
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
}
