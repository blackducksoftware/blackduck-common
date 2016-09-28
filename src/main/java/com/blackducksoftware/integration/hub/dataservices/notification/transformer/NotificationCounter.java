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
package com.blackducksoftware.integration.hub.dataservices.notification.transformer;

import java.util.Map;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.ProjectAggregateBuilder;

public class NotificationCounter {
	private final Map<String, ProjectAggregateBuilder> countBuilderMap;

	public NotificationCounter(final Map<String, ProjectAggregateBuilder> countBuilderMap) {
		this.countBuilderMap = countBuilderMap;
	}

	public void count(final NotificationContentItem item) {
		final ProjectAggregateBuilder builder = getCountBuilder(item.getProjectVersion());
		builder.increment(item);
	}

	public Map<String, ProjectAggregateBuilder> getCountBuilderMap() {
		return countBuilderMap;
	}

	public ProjectAggregateBuilder getCountBuilder(final ProjectVersion projectVersion) {
		final String key = projectVersion.getProjectVersionLink();
		if (getCountBuilderMap().containsKey(key)) {
			return getCountBuilderMap().get(key);
		} else {
			ProjectAggregateBuilder builder = new ProjectAggregateBuilder();
			if (key != null) {
				builder = builder.updateProjectVersion(projectVersion);
				getCountBuilderMap().put(key, builder);
			}
			return builder;
		}
	}

}
