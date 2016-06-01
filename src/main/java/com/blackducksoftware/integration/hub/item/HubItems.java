package com.blackducksoftware.integration.hub.item;

import java.util.List;

/**
 * A list of items as returned by the Hub.
 *
 * @author sbillings
 *
 */
public class HubItems {
	private int totalCount;
	private List<HubItem> items;

	public int getTotalCount() {
		return totalCount;
	}

	public List<HubItem> getItems() {
		return items;
	}

	public void setTotalCount(final int totalCount) {
		this.totalCount = totalCount;
	}

	public void setItems(final List<HubItem> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "HubItemList [totalCount=" + totalCount + ", items=" + items
				+ "]";
	}

}
