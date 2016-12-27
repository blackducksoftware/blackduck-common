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
package com.blackducksoftware.integration.hub.dataservice.license;

import java.util.LinkedList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.component.Component;
import com.blackducksoftware.integration.hub.api.component.ComponentRequestService;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionRequestService;
import com.blackducksoftware.integration.hub.api.component.version.License;
import com.blackducksoftware.integration.hub.api.component.version.LicenseInfo;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;

public class LicenseDataService extends HubRequestService {
	private final ComponentVersionRequestService componentVersionRequestService;
	private final ComponentRequestService componentRequestService;
	
	public LicenseDataService(RestConnection restConnection) {
		this(restConnection, new ComponentVersionRequestService(restConnection), new ComponentRequestService(restConnection));
	}
	
	public LicenseDataService(RestConnection restConnection, ComponentVersionRequestService componentVersionRequestService, ComponentRequestService componentRequestService){
		super(restConnection);
		
		this.componentVersionRequestService = componentVersionRequestService;
		this.componentRequestService = componentRequestService;
	}
	
	public List<License> getAllLicenses(final String namespace, final String groupId, final String artifactId, final String version) throws HubIntegrationException{
		Component component = componentRequestService.getExactComponentMatch(namespace, groupId, artifactId, version);
		String componentUrl = component.getComponent();
		String versionUrl = component.getVersion();
		
		String[] componentSegmentArr = componentUrl.split("/");
		String[] versionSegmentArr = versionUrl.split("/");
		
		//Return a blank list if split does not work
		if(componentSegmentArr.length == 0 || versionSegmentArr.length == 0){
			return new LinkedList<License>();
		}
		
		String componentSegStr = componentSegmentArr[componentSegmentArr.length-1];
		String versionSegStr = versionSegmentArr[versionSegmentArr.length-1];
		
		return componentVersionRequestService.getAllLicenses(componentSegStr, versionSegStr);
	}
	
	public LicenseInfo getLicensesInfoFromCompVersion(final String namespace, final String groupId, final String artifactId, final String version) throws HubIntegrationException {
		Component component = componentRequestService.getExactComponentMatch(namespace, groupId, artifactId, version);
		String componentUrl = component.getComponent();
		String versionUrl = component.getVersion();
		
		String[] componentSegmentArr = componentUrl.split("/");
		String[] versionSegmentArr = versionUrl.split("/");
		
		String componentSegStr = componentSegmentArr[componentSegmentArr.length-1];
		String versionSegStr = versionSegmentArr[versionSegmentArr.length-1];
		
		return componentVersionRequestService.getLicenseInfo(componentSegStr, versionSegStr);
	}
}
