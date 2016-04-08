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

public abstract class AbstractLinkedResource {

	private final MetaInformation _meta;

	public AbstractLinkedResource(final MetaInformation _meta) {
		this._meta = _meta;
	}

	public MetaInformation get_meta() {
		return _meta;
	}

	public String getLink(final String linkRel) {
		if (get_meta() != null && get_meta().getLinks() != null && !get_meta().getLinks().isEmpty()) {
			for (final MetaLink link : get_meta().getLinks()) {
				if (link.getRel().equalsIgnoreCase(linkRel)) {
					return link.getHref();
				}
			}
		}
		return null;
	}
}
