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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.vulnerabilities.VulnerabilityItem;
import com.blackducksoftware.integration.hub.api.vulnerabilities.VulnerabilityRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;

public class ComponentAggregateBuilder {

	private String componentName;
	private String componentVersion;
	private String componentVersionUrl;
	private int addedVulnCount;
	private int updatedVulnCount;
	private int deletedVulnCount;
	private final List<PolicyViolationContentItem> policyViolationSet;
	private final List<PolicyOverrideContentItem> policyOverrideSet;
	private final List<VulnerabilityContentItem> vulnerabilityList;
	private VulnerabilityRestService restService;

	public ComponentAggregateBuilder() {
		policyViolationSet = new ArrayList<>();
		policyOverrideSet = new ArrayList<>();
		vulnerabilityList = new ArrayList<>();
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(final String componentName) {
		this.componentName = componentName;
	}

	public String getComponentVersion() {
		return componentVersion;
	}

	public void setComponentVersion(final String componentVersion) {
		this.componentVersion = componentVersion;
	}

	public void setComponentVersionUrl(final String componentVersionUrl) {
		this.componentVersionUrl = componentVersionUrl;
	}

	public String getComponentVersionUrl() {
		return componentVersionUrl;
	}

	public VulnerabilityRestService getRestService() {
		return restService;
	}

	public void setRestService(final VulnerabilityRestService restService) {
		this.restService = restService;
	}

	public int getTotal() {
		return getPolicyViolationCount() + getPolicyOverrideCount() + getVulnerabilityCount();
	}

	public int getAddedVulnCount() {
		return addedVulnCount;
	}

	public void setAddedVulnCount(final int addedVulnCount) {
		this.addedVulnCount = addedVulnCount;
	}

	public int getUpdatedVulnCount() {
		return updatedVulnCount;
	}

	public void setUpdatedVulnCount(final int updatedVulnCount) {
		this.updatedVulnCount = updatedVulnCount;
	}

	public int getDeletedVulnCount() {
		return deletedVulnCount;
	}

	public void setDeletedVulnCount(final int deletedVulnCount) {
		this.deletedVulnCount = deletedVulnCount;
	}

	private void addPolicyViolation(final PolicyViolationContentItem item) {
		policyViolationSet.add(item);
	}

	private void addPolicyOverride(final PolicyOverrideContentItem item) {
		policyOverrideSet.add(item);
	}

	private void addVulnerability(final VulnerabilityContentItem item) {
		vulnerabilityList.add(item);
	}

	public void increment(final NotificationContentItem item) {
		if (item instanceof PolicyOverrideContentItem) {
			addPolicyOverride((PolicyOverrideContentItem) item);
		} else if (item instanceof PolicyViolationContentItem) {
			// order matters PolicyOverrideContentItem is a sub-class of
			// PolicyViolationContentItem need to check it first.
			addPolicyViolation((PolicyViolationContentItem) item);
		} else if (item instanceof VulnerabilityContentItem) {
			incrementVulnerabilityCounts((VulnerabilityContentItem) item);
		}
	}

	private void incrementVulnerabilityCounts(final VulnerabilityContentItem item) {
		addVulnerability(item);
		if (item.getAddedVulnList() != null) {
			addedVulnCount += item.getAddedVulnList().size();
		}

		if (item.getUpdatedVulnList() != null) {
			updatedVulnCount += item.getUpdatedVulnList().size();
		}

		if (item.getDeletedVulnList() != null) {
			deletedVulnCount += item.getDeletedVulnList().size();
		}
	}

	public int getPolicyViolationCount() {
		if (policyViolationSet == null) {
			return 0;
		} else {
			return policyViolationSet.size();
		}
	}

	public int getPolicyOverrideCount() {
		if (policyOverrideSet == null) {
			return 0;
		} else {
			return policyOverrideSet.size();
		}
	}

	public int getVulnerabilityCount() {
		if (vulnerabilityList == null) {
			return 0;
		} else {
			return vulnerabilityList.size();
		}
	}

	public ComponentAggregateData build() {
		List<VulnerabilityItem> itemList = new ArrayList<>();
		final ComponentVulnerabilitySummaryBuilder summaryBuilder = new ComponentVulnerabilitySummaryBuilder();

		if (getVulnerabilityCount() > 0) { // received vulnerability
			// notifications
			if (getRestService() != null) {
				try {
					itemList = getRestService().getComponentVersionVulnerabilities(getComponentVersionUrl());
				} catch (IOException | URISyntaxException | BDRestException e) {
				}
			}
			summaryBuilder.setComponentName(getComponentName());
			summaryBuilder.setComponentVersion(getComponentVersion());
			summaryBuilder.setVulnerabilityList(itemList);
		}
		final ComponentVulnerabilitySummary vulnSummary = summaryBuilder.build();
		return new ComponentAggregateData(getComponentName(), getComponentVersion(), policyViolationSet,
				policyOverrideSet, vulnerabilityList, addedVulnCount, updatedVulnCount, deletedVulnCount, vulnSummary);
	}

}
