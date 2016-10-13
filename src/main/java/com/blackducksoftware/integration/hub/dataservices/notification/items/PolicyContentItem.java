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
package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.net.URISyntaxException;
import java.util.Date;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.util.HubUrlParser;

public class PolicyContentItem extends NotificationContentItem {
	private final String componentUrl;

	public PolicyContentItem(final Date createdAt, final ProjectVersion projectVersion, final String componentName,
			final String componentVersion, final String componentUrl, final String componentVersionUrl) {
		super(createdAt, projectVersion, componentName, componentVersion, componentVersionUrl);
		this.componentUrl = componentUrl;
	}

	public String getComponentUrl() {
		return componentUrl;
	}

	public String getComponentRelativeUrl() throws URISyntaxException {
		return HubUrlParser.getRelativeUrl(componentUrl);
	}
}
