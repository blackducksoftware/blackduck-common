package com.blackducksoftware.integration.hub.api.component.version;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.api.item.HubResponse;

public class License extends HubItem {
	private final CodeSharingEnum codeSharing;
	private final String name;
	private final OwnershipEnum ownership;
	
	public License(CodeSharingEnum codeSharing, String name, OwnershipEnum ownership) {
		this.codeSharing = codeSharing;
		this.name = name;
		this.ownership = ownership;
	}

	public CodeSharingEnum getCodeSharing() {
		return codeSharing;
	}

	public String getName() {
		return name;
	}

	public OwnershipEnum getOwnership() {
		return ownership;
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
	}
}
