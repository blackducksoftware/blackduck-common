package com.blackducksoftware.integration.hub.item;

import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;
import com.google.gson.annotations.SerializedName;

/**
 * An Item (project, notification, etc.) as returned from the Hub.
 *
 * @author sbillings
 *
 */
public class HubItem {

	@SerializedName("_meta")
	private MetaInformation meta;

	public MetaInformation getMeta() {
		return meta;
	}

	public void setMeta(final MetaInformation meta) {
		this.meta = meta;
	}

	public String getLink(final String linkRel) {
		if (getMeta() != null && getMeta().getLinks() != null
				&& !getMeta().getLinks().isEmpty()) {
			for (final MetaLink link : getMeta().getLinks()) {
				if (link.getRel().equalsIgnoreCase(linkRel)) {
					return link.getHref();
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "Item [meta=" + meta + "]";
	}

}
