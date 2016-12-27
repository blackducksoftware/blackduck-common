/**
 * Hub Common
 *
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
 */
package com.blackducksoftware.integration.hub.api.component.version;

import java.util.List;

import com.blackducksoftware.integration.hub.api.item.HubResponse;

public class LicenseInfo extends HubResponse {
	
	private final String licensesType;
	private final List<License> licenses;
	
	public LicenseInfo(String licensesType, List<License> licenses){
		this.licensesType = licensesType;
		this.licenses = licenses;
	}

	public String getLicensesType() {
		return licensesType;
	}

	public List<License> getLicenses() {
		return licenses;
	}
	
	
}
