package com.blackducksoftware.integration.hub.extension.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.extension.ExtensionItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ExtensionItemTest {
	private static final String INFO_URL = "infoUrl";
	private static final String DESCRIPTION = "description";
	private static final String NAME = "name";

	@Test
	public void testConstructor() {
		final MetaInformation meta = new MetaInformation(null, null, null);
		final ExtensionItem item = new ExtensionItem(meta, NAME, DESCRIPTION, INFO_URL, true);

		assertEquals(NAME, item.getName());
		assertEquals(DESCRIPTION, item.getDescription());
		assertEquals(INFO_URL, item.getInfoUrl());
		assertTrue(item.isAuthenticated());
	}
}
