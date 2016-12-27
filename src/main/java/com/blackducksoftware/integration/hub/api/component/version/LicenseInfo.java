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
