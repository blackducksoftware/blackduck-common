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
package com.blackducksoftware.integration.hub.extension.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.extension.ConfigurationItem;
import com.blackducksoftware.integration.hub.api.extension.OptionItem;
import com.blackducksoftware.integration.hub.api.extension.OptionTypeEnum;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ConfigurationItemTest {

	private List<OptionItem> createOptions() {
		final List<OptionItem> itemList = new ArrayList<>();
		itemList.add(new OptionItem("optionName", "optionTitle"));
		return itemList;
	}

	private List<String> createValueList() {
		final List<String> itemList = new ArrayList<>();
		itemList.add("value");
		return itemList;
	}

	@Test
	public void testConfigurationItemConstructor() {
		final MetaInformation meta = null;
		final String name = "name";
		final OptionTypeEnum optionType = OptionTypeEnum.STRING;
		final String title = "title";
		final boolean required = true;
		final boolean singleValue = true;
		final String description = "description";
		final List<OptionItem> options = createOptions();
		final List<String> value = createValueList();

		final ConfigurationItem item = new ConfigurationItem(meta, name, optionType, title, required, singleValue,
				description, options, value);

		assertNull(item.getMeta());
		assertEquals(name, item.getName());
		assertEquals(optionType, item.getOptionType());
		assertEquals(title, item.getTitle());
		assertTrue(item.isRequired());
		assertTrue(item.isSingleValue());
		assertEquals(description, item.getDescription());
		assertEquals(options, item.getOptions());
		assertEquals(value, item.getValue());
	}
}
