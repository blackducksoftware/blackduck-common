package com.blackducksoftware.integration.hub.item;

import java.util.List;

public class HubItemList {
    private int totalCount;
    private List<Item> items;

    public int getTotalCount() {
	return totalCount;
    }

    public List<Item> getItems() {
	return items;
    }

    public void setTotalCount(int totalCount) {
	this.totalCount = totalCount;
    }

    public void setItems(List<Item> items) {
	this.items = items;
    }

    @Override
    public String toString() {
	return "HubItemList [totalCount=" + totalCount + ", items=" + items
		+ "]";
    }

}
