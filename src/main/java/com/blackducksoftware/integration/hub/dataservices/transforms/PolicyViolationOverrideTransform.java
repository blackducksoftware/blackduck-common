package com.blackducksoftware.integration.hub.dataservices.transforms;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.NotificationRestService;
import com.blackducksoftware.integration.hub.api.PolicyRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.items.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public class PolicyViolationOverrideTransform extends AbstractPolicyTransform {

	public PolicyViolationOverrideTransform(final NotificationRestService notificationService,
			final ProjectVersionRestService projectVersionService, final PolicyRestService policyService,
			final VersionBomPolicyRestService bomVersionPolicyService,
			final ComponentVersionRestService componentVersionService) {
		super(notificationService, projectVersionService, policyService, bomVersionPolicyService,
				componentVersionService);
	}

	@Override
	public List<NotificationContentItem> transform(final NotificationItem item) throws HubItemTransformException {
		final List<NotificationContentItem> templateData = new ArrayList<NotificationContentItem>();
		try {
			final ReleaseItem releaseItem;
			final PolicyOverrideNotificationItem policyViolation = (PolicyOverrideNotificationItem) item;
			final String projectName = policyViolation.getContent().getProjectName();
			final List<ComponentVersionStatus> componentVersionList = new ArrayList<ComponentVersionStatus>();
			final ComponentVersionStatus componentStatus = new ComponentVersionStatus();
			componentStatus.setBomComponentVersionPolicyStatusLink(
					policyViolation.getContent().getBomComponentVersionPolicyStatusLink());
			componentStatus.setComponentName(policyViolation.getContent().getComponentName());
			componentStatus.setComponentVersionLink(policyViolation.getContent().getComponentVersionLink());

			componentVersionList.add(componentStatus);

			releaseItem = getProjectVersionService()
					.getProjectVersionReleaseItem(policyViolation.getContent().getProjectVersionLink());
			handleNotification(projectName, componentVersionList, releaseItem, item, templateData);
		} catch (IOException | BDRestException | URISyntaxException e) {
			throw new HubItemTransformException(e);
		}
		return templateData;
	}

	@Override
	public void createContents(final String projectName, final String projectVersion, final String componentName,
			final String componentVersion, final List<String> policyNameList, final NotificationItem item,
			final List<NotificationContentItem> templateData) {
		final PolicyOverrideNotificationItem policyOverride = (PolicyOverrideNotificationItem) item;
		templateData.add(new PolicyOverrideContentItem(projectName, projectVersion, componentName, componentVersion,
				policyNameList, policyOverride.getContent().getFirstName(), policyOverride.getContent().getLastName()));
	}
}
