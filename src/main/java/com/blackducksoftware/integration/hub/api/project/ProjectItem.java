/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api.project;

import java.util.UUID;

import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.util.HubUrlParser;

public class ProjectItem extends HubItem {
	public static final String PROJECT_URL_IDENTIFIER = "projects";
	public static final String VERSION_LINK = "versions";
	public static final String CANONICAL_VERSION_LINK = "canonicalVersion";

	private final String name;
	private final String source;

	public ProjectItem(final String name, final String source, final MetaInformation _meta) {
		super(_meta);
		this.name = name;
		this.source = source;
	}

	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	public UUID getProjectId() throws MissingUUIDException {
		if (getMeta() == null || getMeta().getHref() == null) {
			return null;
		}
		return HubUrlParser.getUUIDFromURLString(PROJECT_URL_IDENTIFIER, getMeta().getHref());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((getMeta() == null) ? 0 : getMeta().hashCode());
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
		if (!(obj instanceof ProjectItem)) {
			return false;
		}
		final ProjectItem other = (ProjectItem) obj;
		if (getMeta() == null) {
			if (other.getMeta() != null) {
				return false;
			}
		} else if (!getMeta().equals(other.getMeta())) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!source.equals(other.source)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ProjectItem [name=");
		builder.append(name);
		builder.append(", source=");
		builder.append(source);
		builder.append(", _meta=");
		builder.append(getMeta());
		builder.append("]");
		return builder.toString();
	}

}
