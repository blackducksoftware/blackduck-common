package com.blackducksoftware.integration.hub.api.extension;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class UserOptionLinkItem extends HubItem {
	private final String user;
	private final String extensionOptions;

	public UserOptionLinkItem(final MetaInformation meta, final String user, final String extensionOptions) {
		super(meta);
		this.user = user;
		this.extensionOptions = extensionOptions;
	}

	public String getUser() {
		return user;
	}

	public String getExtensionOptions() {
		return extensionOptions;
	}
}
