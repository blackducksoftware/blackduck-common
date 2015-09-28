package com.blackducksoftware.integration.hub.response;

import java.util.List;

public class ReleasesList {

    private List<ReleaseItem> items;

    public List<ReleaseItem> getItems() {
        return items;
    }

    public void setItems(List<ReleaseItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ReleasesList [items=");
        builder.append(items);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((items == null) ? 0 : items.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ReleasesList other = (ReleasesList) obj;
        if (items == null) {
            if (other.items != null) {
                return false;
            }
        } else if (!items.equals(other.items)) {
            return false;
        }
        return true;
    }

}
