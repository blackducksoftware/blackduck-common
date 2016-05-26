/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.policy.api;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class PolicyStatus {
	private final String overallStatus;

	private final String updatedAt;

	private final List<ComponentVersionStatusCount> componentVersionStatusCounts;

	private final MetaInformation _meta;

	public PolicyStatus(final String overallStatus, final String updatedAt, final List<ComponentVersionStatusCount> componentVersionStatusCounts, final MetaInformation _meta) {
		this.overallStatus = overallStatus;
		this.updatedAt = updatedAt;
		this.componentVersionStatusCounts = componentVersionStatusCounts;
		this._meta = _meta;
	}

	public String getOverallStatus() {
		return overallStatus;
	}

	public PolicyStatusEnum getOverallStatusEnum() {
		return PolicyStatusEnum.getPolicyStatusEnum(overallStatus);
	}

	public ComponentVersionStatusCount getCountInViolation() {
		if (componentVersionStatusCounts == null || componentVersionStatusCounts.isEmpty()) {
			return null;
		}
		for (final ComponentVersionStatusCount count : componentVersionStatusCounts) {
			if (count.getPolicyStatusFromName() == PolicyStatusEnum.IN_VIOLATION) {
				return count;
			}
		}
		return null;
	}

	public ComponentVersionStatusCount getCountNotInViolation() {
		if (componentVersionStatusCounts == null || componentVersionStatusCounts.isEmpty()) {
			return null;
		}
		for (final ComponentVersionStatusCount count : componentVersionStatusCounts) {
			if (count.getPolicyStatusFromName() == PolicyStatusEnum.NOT_IN_VIOLATION) {
				return count;
			}
		}
		return null;
	}

	public ComponentVersionStatusCount getCountInViolationOverridden() {
		if (componentVersionStatusCounts == null || componentVersionStatusCounts.isEmpty()) {
			return null;
		}
		for (final ComponentVersionStatusCount count : componentVersionStatusCounts) {
			if (count.getPolicyStatusFromName() == PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN) {
				return count;
			}
		}
		return null;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public DateTime getUpdatedAtTime() {
		if (StringUtils.isBlank(updatedAt)) {
			return null;
		}
		try {
			return new DateTime(updatedAt);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	public MetaInformation get_meta() {
		return _meta;
	}

	public List<ComponentVersionStatusCount> getComponentVersionStatusCounts() {
		return componentVersionStatusCounts;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_meta == null) ? 0 : _meta.hashCode());
		result = prime * result + ((componentVersionStatusCounts == null) ? 0 : componentVersionStatusCounts.hashCode());
		result = prime * result + ((overallStatus == null) ? 0 : overallStatus.hashCode());
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
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
		if (!(obj instanceof PolicyStatus)) {
			return false;
		}
		final PolicyStatus other = (PolicyStatus) obj;
		if (_meta == null) {
			if (other._meta != null) {
				return false;
			}
		} else if (!_meta.equals(other._meta)) {
			return false;
		}
		if (componentVersionStatusCounts == null) {
			if (other.componentVersionStatusCounts != null) {
				return false;
			}
		} else if (!componentVersionStatusCounts.equals(other.componentVersionStatusCounts)) {
			return false;
		}
		if (overallStatus == null) {
			if (other.overallStatus != null) {
				return false;
			}
		} else if (!overallStatus.equals(other.overallStatus)) {
			return false;
		}
		if (updatedAt == null) {
			if (other.updatedAt != null) {
				return false;
			}
		} else if (!updatedAt.equals(other.updatedAt)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PolicyStatus [overallStatus=");
		builder.append(overallStatus);
		builder.append(", updatedAt=");
		builder.append(updatedAt);
		builder.append(", componentVersionStatusCounts=");
		builder.append(componentVersionStatusCounts);
		builder.append(", _meta=");
		builder.append(_meta);
		builder.append("]");
		return builder.toString();
	}

}
