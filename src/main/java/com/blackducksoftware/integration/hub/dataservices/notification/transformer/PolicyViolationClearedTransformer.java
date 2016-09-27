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
import com.blackducksoftware.integration.hub.api.notification.RuleViolationClearedNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public class PolicyViolationClearedTransformer extends AbstractPolicyTransformer {
	public PolicyViolationClearedTransformer(final NotificationRestService notificationService,
			final ProjectVersionRestService projectVersionService, final PolicyRestService policyService,
			final VersionBomPolicyRestService bomVersionPolicyService,
			final ComponentVersionRestService componentVersionService, final PolicyNotificationFilter policyFilter) {
		super(notificationService, projectVersionService, policyService, bomVersionPolicyService,
				componentVersionService, policyFilter);
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

	@Override
	public void handleNotification(final List<ComponentVersionStatus> componentVersionList,
			final ProjectVersion projectVersion, final NotificationItem item,
			final List<NotificationContentItem> templateData) throws HubItemTransformException {
		for (final ComponentVersionStatus componentVersion : componentVersionList) {
			try {
				final String componentVersionLink = componentVersion.getComponentVersionLink();
				final String componentVersionName = getComponentVersionName(componentVersionLink);
				final List<String> policyUrls = componentVersion.getPolicies();

				if (policyUrls != null) {
					List<PolicyRule> ruleList = getRulesFromUrls(policyUrls);

					ruleList = getMatchingRules(ruleList);
					if (ruleList != null && !ruleList.isEmpty()) {
						final List<PolicyRule> policyRuleList = new ArrayList<PolicyRule>();
						for (final PolicyRule rule : ruleList) {
							policyRuleList.add(rule);
						}
						createContents(projectVersion, componentVersion.getComponentName(), componentVersionName,
								componentVersion.getComponentId(), componentVersion.getComponentVersionId(),
								policyRuleList, item, templateData);
					}
				}
			} catch (final Exception e) {
				throw new HubItemTransformException(e);
			}
		}
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
		templateData.add(contentItem);
	}

}
