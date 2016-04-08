/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.meta;

import java.util.List;

public class MetaInformation {
	private final List<String> allow;

	private final String href;

	private final List<MetaLink> links;

	public MetaInformation(final List<String> allow, final String href, final List<MetaLink> links) {
		this.allow = allow;
		this.href = href;
		this.links = links;
	}

	public List<String> getAllow() {
		return allow;
	}

	public String getHref() {
		return href;
	}

	public List<MetaLink> getLinks() {
		return links;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allow == null) ? 0 : allow.hashCode());
		result = prime * result + ((href == null) ? 0 : href.hashCode());
		result = prime * result + ((links == null) ? 0 : links.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MetaInformation)) {
			return false;
		}
		final MetaInformation other = (MetaInformation) obj;
		if (allow == null) {
			if (other.allow != null) {
				return false;
			}
		} else if (!allow.equals(other.allow)) {
			return false;
		}
		if (href == null) {
			if (other.href != null) {
				return false;
			}
		} else if (!href.equals(other.href)) {
			return false;
		}
		if (links == null) {
			if (other.links != null) {
				return false;
			}
		} else if (!links.equals(other.links)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("MetaInformation [allow=");
		builder.append(allow);
		builder.append(", href=");
		builder.append(href);
		builder.append(", links=");
		builder.append(links);
		builder.append("]");
		return builder.toString();
	}

}
