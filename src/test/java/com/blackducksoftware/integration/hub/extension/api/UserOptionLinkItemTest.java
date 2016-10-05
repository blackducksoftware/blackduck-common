package com.blackducksoftware.integration.hub.extension.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.extension.UserOptionLinkItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class UserOptionLinkItemTest {

	@Test
	public void testConstructor() {
		final MetaInformation meta = new MetaInformation(null, null, null);
		final UserOptionLinkItem item = new UserOptionLinkItem(meta, "userUrl", "userExtensionOptionsUrl");

		assertEquals("userUrl", item.getUser());
		assertEquals("userExtensionOptionsUrl", item.getExtensionOptions());
	}
}
