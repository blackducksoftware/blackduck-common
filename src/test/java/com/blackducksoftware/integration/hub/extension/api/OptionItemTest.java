package com.blackducksoftware.integration.hub.extension.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.extension.OptionItem;

public class OptionItemTest {

	@Test
	public void testOptionItemConstructor() {
		final String name = "name";
		final String title = "title";
		final OptionItem item = new OptionItem(name, title);

		assertEquals(name, item.getName());
		assertEquals(title, item.getTitle());
	}
}
