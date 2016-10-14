package com.blackducksoftware.integration.hub.api.item;

import java.util.ArrayList;
import java.util.List;

public class HubItemFilterUtil<T extends HubItem> {

	public List<T> getAccessibleItems(final List<T> hubItems) {
		final List<T> accessibleItems = new ArrayList<>();
		for (final T hubItem : hubItems) {
			if (hubItem.getMeta().isAccessible()) {
				accessibleItems.add(hubItem);
			}
		}
		return accessibleItems;
	}
}
