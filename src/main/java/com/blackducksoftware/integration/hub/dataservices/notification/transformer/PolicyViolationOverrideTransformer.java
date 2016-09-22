package com.blackducksoftware.integration.hub.dataservices.notification.transformer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.blackducksoftware.integration.hub.api.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.NotificationRestService;
import com.blackducksoftware.integration.hub.api.PolicyRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public class PolicyViolationOverrideTransformer extends AbstractPolicyTransformer {
	public PolicyViolationOverrideTransformer(final NotificationRestService notificationService,
			final ProjectVersionRestService projectVersionService, final PolicyRestService policyService,
			final VersionBomPolicyRestService bomVersionPolicyService,
			final ComponentVersionRestService componentVersionService, final PolicyNotificationFilter policyFilter) {
		super(notificationService, projectVersionService, policyService, bomVersionPolicyService,
				componentVersionService, policyFilter);
	}

	@Override
	public List<NotificationContentItem> transform(final NotificationItem item) throws HubItemTransformException {
		final List<NotificationContentItem> templateData = new ArrayList<>();
		try {
			final ReleaseItem releaseItem;
			final PolicyOverrideNotificationItem policyViolation = (PolicyOverrideNotificationItem) item;
			final String projectName = policyViolation.getContent().getProjectName();
			final List<ComponentVersionStatus> componentVersionList = new ArrayList<>();
			final ComponentVersionStatus componentStatus = new ComponentVersionStatus();
			componentStatus.setBomComponentVersionPolicyStatusLink(
					policyViolation.getContent().getBomComponentVersionPolicyStatusLink());
			componentStatus.setComponentName(policyViolation.getContent().getComponentName());
			componentStatus.setComponentVersionLink(policyViolation.getContent().getComponentVersionLink());

			componentVersionList.add(componentStatus);

			releaseItem = getProjectVersionService()
					.getProjectVersionReleaseItem(policyViolation.getContent().getProjectVersionLink());

			final ProjectVersion projectVersion = new ProjectVersion();
			projectVersion.setProjectName(projectName);
			projectVersion.setProjectVersionName(releaseItem.getVersionName());
			projectVersion.setProjectVersionLink(policyViolation.getContent().getProjectVersionLink());

			handleNotification(componentVersionList, projectVersion, item, templateData);
		} catch (IOException | BDRestException | URISyntaxException e) {
			throw new HubItemTransformException(e);
		}
		return templateData;
	}

	@Override
	public void handleNotification(final List<ComponentVersionStatus> componentVersionList,
			final ProjectVersion projectVersion, final NotificationItem item,
			final List<NotificationContentItem> templateData) throws HubItemTransformException {
		handleNotificationUsingBomComponentVersionPolicyStatusLink(componentVersionList, projectVersion, item,
				templateData);
	}

	@Override
	public void createContents(final ProjectVersion projectVersion, final String componentName,
			final String componentVersion, final UUID componentId, final UUID componentVersionId,
			final List<PolicyRule> policyRuleList, final NotificationItem item,
			final List<NotificationContentItem> templateData) {
		final PolicyOverrideNotificationItem policyOverride = (PolicyOverrideNotificationItem) item;

		templateData.add(new PolicyOverrideContentItem(item.getCreatedAt(), projectVersion, componentName,
				componentVersion, componentId,
				componentVersionId,
				policyRuleList, policyOverride.getContent().getFirstName(), policyOverride.getContent().getLastName()));
	}

}
