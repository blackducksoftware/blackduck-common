package com.blackducksoftware.integration.hub.dataservices.notification.transforms;

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
import com.blackducksoftware.integration.hub.api.notification.RuleViolationClearedNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public class PolicyViolationClearedTransform extends AbstractPolicyTransform {
	private final IntLogger logger;

	public PolicyViolationClearedTransform(final IntLogger logger, final NotificationRestService notificationService,
			final ProjectVersionRestService projectVersionService, final PolicyRestService policyService,
			final VersionBomPolicyRestService bomVersionPolicyService,
			final ComponentVersionRestService componentVersionService, final PolicyNotificationFilter policyFilter) {
		super(notificationService, projectVersionService, policyService, bomVersionPolicyService,
				componentVersionService, policyFilter);
		this.logger = logger;
	}

	@Override
	public List<NotificationContentItem> transform(final NotificationItem item) throws HubItemTransformException {
		final List<NotificationContentItem> templateData = new ArrayList<>();

		final RuleViolationClearedNotificationItem policyViolation = (RuleViolationClearedNotificationItem) item;
		final String projectName = policyViolation.getContent().getProjectName();
		final List<ComponentVersionStatus> componentVersionList = policyViolation.getContent()
				.getComponentVersionStatuses();
		final String projectVersionLink = policyViolation.getContent().getProjectVersionLink();
		ReleaseItem releaseItem;
		try {
			releaseItem = getReleaseItem(projectVersionLink);
		} catch (IOException | BDRestException | URISyntaxException e1) {
			throw new HubItemTransformException("Error getting release item while transforming notification " + item
					+ "; projectVersionLink: " + projectVersionLink + ": " + e1.getMessage(), e1);
		}
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName(projectName);
		projectVersion.setProjectVersionName(releaseItem.getVersionName());
		projectVersion.setProjectVersionLink(policyViolation.getContent().getProjectVersionLink());

		try {
			handleNotification(componentVersionList, projectVersion, item, templateData);
		} catch (final HubItemTransformException e) {
			throw new HubItemTransformException("Error in handleNotification() while transforming notification " + item
					+ "; projectVersionLink: " + projectVersionLink + ": " + e.getMessage(), e);
		}

		return templateData;
	}

	private ReleaseItem getReleaseItem(final String projectVersionLink)
			throws IOException, BDRestException, URISyntaxException {
		ReleaseItem releaseItem;
		releaseItem = getProjectVersionService().getProjectVersionReleaseItem(projectVersionLink);
		return releaseItem;
	}

	@Override
	public void createContents(final ProjectVersion projectVersion, final String componentName,
			final String componentVersion, final UUID componentId, final UUID componentVersionId,
			final List<PolicyRule> policyRuleList, final NotificationItem item,
			final List<NotificationContentItem> templateData) {
		final PolicyViolationClearedContentItem contentItem = new PolicyViolationClearedContentItem(
				item.getCreatedAt(), projectVersion, componentName,
				componentVersion,
				componentId, componentVersionId,
				policyRuleList);
		logger.debug("createContents(): adding: " + contentItem);
		templateData.add(contentItem);
	}

}
