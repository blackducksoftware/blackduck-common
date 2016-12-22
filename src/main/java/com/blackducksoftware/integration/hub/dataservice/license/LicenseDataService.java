package com.blackducksoftware.integration.hub.dataservice.license;

import java.util.LinkedList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.component.Component;
import com.blackducksoftware.integration.hub.api.component.ComponentRequestService;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionRequestService;
import com.blackducksoftware.integration.hub.api.component.version.License;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;

public class LicenseDataService extends HubRequestService {
	private final ComponentVersionRequestService componentVersionRequestService;
	private final ComponentRequestService componentRequestService;
	
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
		
		if(componentSegmentArr.length == 0 || versionSegmentArr.length == 0){
			return new LinkedList<License>();
		}
		
		String componentSegStr = componentSegmentArr[componentSegmentArr.length-1];
		String versionSegStr = versionSegmentArr[versionSegmentArr.length-1];
		
		return componentVersionRequestService.getAllLicenses(componentSegStr, versionSegStr);
	}
}
