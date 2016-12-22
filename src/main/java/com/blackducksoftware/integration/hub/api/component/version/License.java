package com.blackducksoftware.integration.hub.api.component.version;

import java.util.List;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.hub.api.item.HubResponse;

public class License extends HubResponse {
	
	private final String name;
	private final String ownership;
	private final String codeSharing;
	private final List<String> licenses;
	private final String license;
	
	public License(){
		this(null, null, null, null, null);
	}
	
	public License(String name, String ownership, String codeSharing, List<String> licenses, String license){
		this.name = name;
		this.ownership = ownership;
		this.codeSharing = codeSharing;
		this.licenses = licenses;
		this.license = license;
	}

	public String getName() {
		return name;
	}

	public String getOwnership() {
		return ownership;
	}

	public String getCodeSharing() {
		return codeSharing;
	}

	public List<String> getLicenses() {
		return licenses;
	}

	public String getLicense() {
		return license;
	}
	
	public String getLicenseId(){
		String[] urlElements = license.split("/");
		if(urlElements.length > 0){
			return urlElements[urlElements.length-1];
		} else {
			//FIXME proper exception handling
			return "";
		}
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
	}
}
