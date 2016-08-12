package com.blackducksoftware.integration.hub.dataservices.transforms;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.component.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.items.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.notification.NotificationService;

public class PolicyViolationOverrideTransform extends AbstractPolicyTransform {

	public PolicyViolationOverrideTransform(final NotificationService notificationService) {
		super(notificationService);
	}

	@Override
	public List<NotificationContentItem> transform(final NotificationItem item) throws HubItemTransformException {
		final List<NotificationContentItem> templateData = new ArrayList<NotificationContentItem>();

		ReleaseItem releaseItem;
		try {
			final PolicyOverrideNotificationItem policyViolation = (PolicyOverrideNotificationItem) item;

			final String projectName = policyViolation.getContent().getProjectName();
			final List<ComponentVersionStatus> componentVersionList = new ArrayList<ComponentVersionStatus>();
			final ComponentVersionStatus componentStatus = new ComponentVersionStatus();
			componentStatus.setBomComponentVersionPolicyStatusLink(
					policyViolation.getContent().getBomComponentVersionPolicyStatusLink());
			componentStatus.setComponentName(policyViolation.getContent().getComponentName());
			componentStatus.setComponentVersionLink(policyViolation.getContent().getComponentVersionLink());

			componentVersionList.add(componentStatus);

			releaseItem = getNotificationService()
					.getProjectReleaseItemFromProjectReleaseUrl(policyViolation.getContent().getProjectVersionLink());
			handleNotification(projectName, componentVersionList, releaseItem, item, templateData);
		} catch (final NotificationServiceException e) {
			throw new HubItemTransformException(e);
		} catch (final UnexpectedHubResponseException e) {
			throw new HubItemTransformException(e);
		} catch (final HubItemTransformException e) {
			throw e;
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

	@Override
	public String getNotificationType() {
		return PolicyOverrideNotificationItem.class.getName();
	}
}
