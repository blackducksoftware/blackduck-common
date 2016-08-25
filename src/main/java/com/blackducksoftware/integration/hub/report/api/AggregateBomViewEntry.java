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
package com.blackducksoftware.integration.hub.report.api;

import java.util.List;

import com.blackducksoftware.integration.hub.api.report.LicenseDefinition;
import com.blackducksoftware.integration.hub.api.report.ProjectData;
import com.blackducksoftware.integration.hub.api.report.ReleaseData;
import com.blackducksoftware.integration.hub.api.report.UserData;
import com.blackducksoftware.integration.hub.api.report.risk.RiskProfile;

@Deprecated
public class AggregateBomViewEntry extends com.blackducksoftware.integration.hub.api.report.AggregateBomViewEntry {

	// Need this package and the objects for backwards compatability
	public AggregateBomViewEntry(final List<String> bomEntryIds, final List<Long> bomViewEntryIds,
			final List<String> matchTypes, final List<String> producerMatchTypes,
			final List<String> componentMatchTypes, final List<String> usages, final List<Boolean> inUses,
			final List<UserData> createdByUsers, final String since, final ProjectData producerProject,
			final List<ReleaseData> producerReleases, final List<LicenseDefinition> licenses,
			final RiskProfile riskProfile, final String policyApprovalStatus) {
		super(bomEntryIds, bomViewEntryIds, matchTypes, producerMatchTypes, componentMatchTypes, usages, inUses,
				createdByUsers, since, producerProject, producerReleases, licenses, riskProfile, policyApprovalStatus);
	}

}
