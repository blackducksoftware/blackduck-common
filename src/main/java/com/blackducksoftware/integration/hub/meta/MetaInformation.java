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
