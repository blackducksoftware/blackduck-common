package com.blackducksoftware.integration.hub.item;

import java.util.List;

public class HubItemList {
    private int totalCount;
    private List<HubItem> items;

    public int getTotalCount() {
	return totalCount;
    }

    public List<HubItem> getItems() {
	return items;
    }

    public void setTotalCount(int totalCount) {
	this.totalCount = totalCount;
    }

    public void setItems(List<HubItem> items) {
	this.items = items;
    }

    @Override
    public String toString() {
	return "HubItemList [totalCount=" + totalCount + ", items=" + items
		+ "]";
    }

}
