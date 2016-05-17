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
