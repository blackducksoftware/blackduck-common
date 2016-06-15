package com.blackducksoftware.integration.hub.item;

import java.util.ArrayList;
import java.util.List;

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
	private final MetaInformation meta;

	public HubItem(final MetaInformation meta) {
		this.meta = meta;
	}
	public MetaInformation getMeta() {
		return meta;
	}

	public List<String> getLinks(final String linkRel) {
		final List<String> links = new ArrayList<String>();
		if (linksExist()) {
			for (final MetaLink link : getLinks()) {
				if (isRequestedLink(linkRel, link)) {
					links.add(link.getHref());
				}
			}
		}
		return links;
	}

	private List<MetaLink> getLinks() {
		return getMeta().getLinks();
	}

	private boolean isRequestedLink(final String linkRel, final MetaLink link) {
		return link.getRel().equalsIgnoreCase(linkRel);
	}

	private boolean linksExist() {
		return getMeta() != null && getLinks() != null && !getLinks().isEmpty();
	}

	@Override
	public String toString() {
		return "Item [meta=" + meta + "]";
	}

}
