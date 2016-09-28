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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.vulnerabilities.VulnerabilityRestService;

public class ProjectAggregateBuilder {

	private final ProjectVersion projectVersion;
	private final Date startDate;
	private final Date endDate;
	private int addedVulnCount = 0;
	private int updatedVulnCount = 0;
	private int deletedVulnCount = 0;
	private int policyViolationCount = 0;
	private int policyOverrideCount = 0;
	private int vulnerabilityCount = 0;
	private int totalCount = 0;

	private final Map<String, ComponentAggregateBuilder> compBuilderMap;
	private final VulnerabilityRestService vulnerabilityRestService;

	public ProjectAggregateBuilder() {
		projectVersion = new ProjectVersion();
		this.startDate = new Date();
		this.endDate = new Date();
		compBuilderMap = new HashMap<>();
		vulnerabilityRestService = null;
	}

	private ProjectAggregateBuilder(final Date startDate, final Date endDate, final ProjectVersion projectVersion,
			final Map<String, ComponentAggregateBuilder> compBuilderMap,
			final VulnerabilityRestService vulnerabilitiesRestService) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.projectVersion = projectVersion;
		this.compBuilderMap = compBuilderMap;
		this.vulnerabilityRestService = vulnerabilitiesRestService;
	}

	public ProjectVersion getProjectVersion() {
		return projectVersion;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public ProjectAggregateBuilder updateProjectVersion(final ProjectVersion projectVersion) {
		return new ProjectAggregateBuilder(startDate, endDate, projectVersion, compBuilderMap,
				vulnerabilityRestService);
	}

	public ProjectAggregateBuilder updateDateRange(final Date startDate, final Date endDate) {
		return new ProjectAggregateBuilder(startDate, endDate, projectVersion, compBuilderMap,
				vulnerabilityRestService);
	}

	public ProjectAggregateBuilder updateVulnerabilitiesRestService(
			final VulnerabilityRestService vulnerabilitiesRestService) {
		return new ProjectAggregateBuilder(startDate, endDate, projectVersion, compBuilderMap,
				vulnerabilitiesRestService);
	}

	public ProjectAggregateData build() {
		final List<ComponentAggregateData> compList = getComponentList();
		return new ProjectAggregateData(startDate, endDate, projectVersion, policyViolationCount, policyOverrideCount,
				vulnerabilityCount, totalCount, addedVulnCount, updatedVulnCount, deletedVulnCount, compList);
	}

	public void increment(final NotificationContentItem item) {
		final String compKey = getComponentKey(item);
		ComponentAggregateBuilder compBuilder;
		if (compBuilderMap.containsKey(compKey)) {
			compBuilder = compBuilderMap.get(compKey);
		} else {
			compBuilder = new ComponentAggregateBuilder();
			compBuilder.setComponentName(item.getComponentName());
			compBuilder.setComponentVersion(item.getComponentVersion());
			if (item.getComponentId() != null) {
				compBuilder.setComponentId(item.getComponentId().toString());
			}
			if (item.getComponentVersionId() != null) {
				compBuilder.setComponentVersionId(item.getComponentVersionId().toString());
			}
			compBuilderMap.put(compKey, compBuilder);
		}
		compBuilder.increment(item);
	}

	private String getComponentKey(final NotificationContentItem item) {
		return item.getComponentName() + "-" + item.getComponentVersion();
	}

	private List<ComponentAggregateData> getComponentList() {
		final List<ComponentAggregateData> compList = new ArrayList<>(compBuilderMap.size());

		for (final Map.Entry<String, ComponentAggregateBuilder> entry : compBuilderMap.entrySet()) {
			final ComponentAggregateBuilder compBuilder = entry.getValue();
			compBuilder.setRestService(vulnerabilityRestService);
			final ComponentAggregateData compData = compBuilder.build();
			policyViolationCount += compData.getPolicyViolationCount();
			policyOverrideCount += compData.getPolicyOverrideCount();
			vulnerabilityCount += compData.getVulnerabilityCount();
			totalCount += compData.getTotal();
			addedVulnCount += compData.getVulnAddedCount();
			updatedVulnCount += compData.getVulnUpdatedCount();
			deletedVulnCount += compData.getVulnDeletedCount();
			compList.add(compData);
		}

		return compList;
	}
}
