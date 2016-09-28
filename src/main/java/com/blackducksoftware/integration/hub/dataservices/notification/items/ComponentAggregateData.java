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

import java.util.List;

public class ComponentAggregateData {
	private final String componentName;
	private final String componentVersion;
	private final int vulnAddedCount;
	private final int vulnUpdatedCount;
	private final int vulnDeletedCount;
	private final List<PolicyViolationContentItem> policyViolationList;
	private final List<PolicyOverrideContentItem> policyOverrideList;
	private final List<VulnerabilityContentItem> vulnerabilityList;
	private final ComponentVulnerabilitySummary vulnerabilitySummary;

	public ComponentAggregateData(final String componentName, final String componentVersion,
			final List<PolicyViolationContentItem> policyViolationList,
			final List<PolicyOverrideContentItem> policyOverrideList,
			final List<VulnerabilityContentItem> vulnerabilityList, final int vulnAddedCount,
			final int vulnUpdatedCount, final int vulnDeletedCount,
			final ComponentVulnerabilitySummary vulnerabilitySummary) {
		this.componentName = componentName;
		this.componentVersion = componentVersion;
		this.vulnAddedCount = vulnAddedCount;
		this.vulnUpdatedCount = vulnUpdatedCount;
		this.vulnDeletedCount = vulnDeletedCount;
		this.policyViolationList = policyViolationList;
		this.policyOverrideList = policyOverrideList;
		this.vulnerabilityList = vulnerabilityList;
		this.vulnerabilitySummary = vulnerabilitySummary;
	}

	public String getComponentName() {
		return componentName;
	}

	public String getComponentVersion() {
		return componentVersion;
	}

	public int getTotal() {
		return getPolicyViolationCount() + getPolicyOverrideCount() + getVulnerabilityCount();
	}

	public int getPolicyViolationCount() {
		if (policyViolationList == null) {
			return 0;
		} else {
			return policyViolationList.size();
		}
	}

	public int getPolicyOverrideCount() {
		if (policyOverrideList == null) {
			return 0;
		} else {
			return policyOverrideList.size();
		}
	}

	public int getVulnerabilityCount() {
		if (vulnerabilityList == null) {
			return 0;
		} else {
			return vulnerabilityList.size();
		}
	}

	public int getVulnAddedCount() {
		return vulnAddedCount;
	}

	public int getVulnUpdatedCount() {
		return vulnUpdatedCount;
	}

	public int getVulnDeletedCount() {
		return vulnDeletedCount;
	}

	public List<PolicyViolationContentItem> getPolicyViolationList() {
		return policyViolationList;
	}

	public List<PolicyOverrideContentItem> getPolicyOverrideList() {
		return policyOverrideList;
	}

	public List<VulnerabilityContentItem> getVulnerabilityList() {
		return vulnerabilityList;
	}

	public ComponentVulnerabilitySummary getVulnerabilitySummary() {
		return vulnerabilitySummary;
	}
}
