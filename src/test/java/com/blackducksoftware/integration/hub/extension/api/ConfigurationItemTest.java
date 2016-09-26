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

	private List<String> createDefaultValue() {
		final List<String> itemList = new ArrayList<>();
		itemList.add("defaultValue");
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
		final List<String> defaultValue = createDefaultValue();

		final ConfigurationItem item = new ConfigurationItem(meta, name, optionType, title, required, singleValue,
				description, options, defaultValue);

		assertNull(item.getMeta());
		assertEquals(name, item.getName());
		assertEquals(optionType, item.getOptionType());
		assertEquals(title, item.getTitle());
		assertTrue(item.isRequired());
		assertTrue(item.isSingleValue());
		assertEquals(description, item.getDescription());
		assertEquals(options, item.getOptions());
		assertEquals(defaultValue, item.getDefaultValue());
	}
}
