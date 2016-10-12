package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.net.URISyntaxException;
import java.util.Date;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.util.HubUrlParser;

public class PolicyContentItem extends NotificationContentItem {
	private final String componentUrl;

	public PolicyContentItem(final Date createdAt, final ProjectVersion projectVersion, final String componentName,
			final String componentVersion, final String componentUrl, final String componentVersionUrl) {
		super(createdAt, projectVersion, componentName, componentVersion, componentVersionUrl);
		this.componentUrl = componentUrl;
	}

	public String getComponentUrl() {
		return componentUrl;
	}

	public String getComponentRelativeUrl() throws URISyntaxException {
		return HubUrlParser.getRelativeUrl(componentUrl);
	}
}
