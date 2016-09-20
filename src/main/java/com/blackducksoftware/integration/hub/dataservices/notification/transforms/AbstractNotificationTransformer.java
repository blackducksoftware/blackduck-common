package com.blackducksoftware.integration.hub.dataservices.notification.transforms;

import java.util.List;

import com.blackducksoftware.integration.hub.api.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.NotificationRestService;
import com.blackducksoftware.integration.hub.api.PolicyRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.dataservices.ItemTransform;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public abstract class AbstractNotificationTransformer
		implements ItemTransform<List<NotificationContentItem>, NotificationItem> {
	private final NotificationRestService notificationService;
	private final ProjectVersionRestService projectVersionService;
	private final PolicyRestService policyService;
	private final VersionBomPolicyRestService bomVersionPolicyService;
	private final ComponentVersionRestService componentVersionService;

	public AbstractNotificationTransformer(final NotificationRestService notificationService,
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

	@Override
	public abstract List<NotificationContentItem> transform(NotificationItem item) throws HubItemTransformException;

}
