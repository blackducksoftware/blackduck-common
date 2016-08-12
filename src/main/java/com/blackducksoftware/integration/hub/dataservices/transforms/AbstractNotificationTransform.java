package com.blackducksoftware.integration.hub.dataservices.transforms;

import java.util.List;

import com.blackducksoftware.integration.hub.api.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.NotificationRestService;
import com.blackducksoftware.integration.hub.api.PolicyRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.dataservices.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public abstract class AbstractNotificationTransform {
	private final NotificationRestService notificationService;
	private final ProjectVersionRestService projectVersionService;
	private final PolicyRestService policyService;
	private final VersionBomPolicyRestService bomVersionPolicyService;
	private final ComponentVersionRestService componentVersionService;

	public AbstractNotificationTransform(final NotificationRestService notificationService,
			final ProjectVersionRestService projectVersionService, final PolicyRestService policyService,
			final VersionBomPolicyRestService bomVersionPolicyService,
			final ComponentVersionRestService componentVersionService) {
		this.notificationService = notificationService;
		this.projectVersionService = projectVersionService;
		this.policyService = policyService;
		this.bomVersionPolicyService = bomVersionPolicyService;
		this.componentVersionService = componentVersionService;
	}

	public NotificationRestService getNotificationService() {
		return notificationService;
	}

	public ProjectVersionRestService getProjectVersionService() {
		return projectVersionService;
	}

	public PolicyRestService getPolicyService() {
		return policyService;
	}

	public VersionBomPolicyRestService getBomVersionPolicyService() {
		return bomVersionPolicyService;
	}

	public ComponentVersionRestService getComponentVersionService() {
		return componentVersionService;
	}

	public abstract List<NotificationContentItem> transform(NotificationItem item) throws HubItemTransformException;
}
