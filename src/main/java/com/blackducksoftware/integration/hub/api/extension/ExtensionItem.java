package com.blackducksoftware.integration.hub.api.extension;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ExtensionItem extends HubItem {

	private final String name;
	private final String description;
	private final String infoUrl;
	private final boolean authenticated;

	public ExtensionItem(final MetaInformation meta, final String name, final String description, final String infoUrl,
			final boolean authenticated) {
		super(meta);
		this.name = name;
		this.description = description;
		this.infoUrl = infoUrl;
		this.authenticated = authenticated;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getInfoUrl() {
		return infoUrl;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}
}
