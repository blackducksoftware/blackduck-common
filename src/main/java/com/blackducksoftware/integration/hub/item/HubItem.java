package com.blackducksoftware.integration.hub.item;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

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

	public DateTime getDateTime(final String time) {
		if (StringUtils.isBlank(time)) {
			return null;
		}
		try {
			return new DateTime(time);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return "Item [meta=" + meta + "]";
	}

}
