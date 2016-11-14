package com.blackducksoftware.integration.hub.api.component.id;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ComponentIdItem extends HubItem {

	private final String description;
	private final String name;
	private final ComponentSourceEnum source;

	public ComponentIdItem(final MetaInformation meta, final String description, final String name,
			final ComponentSourceEnum source) {
		super(meta);
		this.description = description;
		this.name = name;
		this.source = source;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public ComponentSourceEnum getSource() {
		return source;
	}

}
