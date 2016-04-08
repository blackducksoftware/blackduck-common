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
package com.blackducksoftware.integration.hub.report.api;

public class ProjectData {

	private final String id;

	private final String name;

	private final Boolean restructured;

	public ProjectData(final String id,
			final String name, final Boolean restructured) {
		this.id = id;
		this.name = name;
		this.restructured = restructured;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Boolean getRestructured() {
		return restructured;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((restructured == null) ? 0 : restructured.hashCode());
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
		if (!(obj instanceof ProjectData)) {
			return false;
		}
		final ProjectData other = (ProjectData) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (restructured == null) {
			if (other.restructured != null) {
				return false;
			}
		} else if (!restructured.equals(other.restructured)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ProjectData [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", restructured=");
		builder.append(restructured);
		builder.append("]");
		return builder.toString();
	}

}
