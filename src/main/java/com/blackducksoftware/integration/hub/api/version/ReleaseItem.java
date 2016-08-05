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
package com.blackducksoftware.integration.hub.api.version;

import java.util.UUID;

import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.util.HubUrlParser;

public class ReleaseItem extends HubItem {
	public static final String PROJECT_URL_IDENTIFIER = "projects";
	public static final String VERSION_URL_IDENTIFIER = "versions";

	public static final String VERSION_REPORT_LINK = "versionReport";
	public static final String RISK_PROFILE_LINK = "riskProfile";
	public static final String POLICY_STATUS_LINK = "policy-status";

	private final String versionName;
	private final String phase;
	private final String distribution;
	private final String source;

	public ReleaseItem(final String versionName, final String phase, final String distribution, final String source,
			final MetaInformation _meta) {
		super(_meta);
		this.versionName = versionName;
		this.phase = phase;
		this.distribution = distribution;
		this.source = source;
	}

	public String getVersionName() {
		return versionName;
	}

	public String getPhase() {
		return phase;
	}

	public PhaseEnum getPhaseEnum() {
		return PhaseEnum.valueOf(phase);
	}

	public String getDistribution() {
		return distribution;
	}

	public DistributionEnum getDistributionEnum() {
		return DistributionEnum.valueOf(distribution);
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

	public UUID getVersionId() throws MissingUUIDException {
		if (getMeta() == null || getMeta().getHref() == null) {
			return null;
		}
		return HubUrlParser.getUUIDFromURLString(VERSION_URL_IDENTIFIER, getMeta().getHref());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((distribution == null) ? 0 : distribution.hashCode());
		result = prime * result + ((phase == null) ? 0 : phase.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((versionName == null) ? 0 : versionName.hashCode());
		result = prime * result + ((getMeta() == null) ? 0 : getMeta().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ReleaseItem)) {
			return false;
		}
		final ReleaseItem other = (ReleaseItem) obj;
		if (getMeta() == null) {
			if (other.getMeta() != null) {
				return false;
			}
		} else if (!getMeta().equals(other.getMeta())) {
			return false;
		}
		if (distribution == null) {
			if (other.distribution != null) {
				return false;
			}
		} else if (!distribution.equals(other.distribution)) {
			return false;
		}
		if (phase == null) {
			if (other.phase != null) {
				return false;
			}
		} else if (!phase.equals(other.phase)) {
			return false;
		}
		if (source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!source.equals(other.source)) {
			return false;
		}
		if (versionName == null) {
			if (other.versionName != null) {
				return false;
			}
		} else if (!versionName.equals(other.versionName)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ReleaseItem [versionName=");
		builder.append(versionName);
		builder.append(", phase=");
		builder.append(phase);
		builder.append(", distribution=");
		builder.append(distribution);
		builder.append(", source=");
		builder.append(source);
		builder.append(", _meta=");
		builder.append(getMeta());
		builder.append("]");
		return builder.toString();
	}

}
