package com.blackducksoftware.integration.hub.dataservices.transforms;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.NotificationRestService;
import com.blackducksoftware.integration.hub.api.PolicyRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;

public abstract class AbstractPolicyTransform extends AbstractNotificationTransform {

	private final PolicyNotificationFilter policyFilter;

	public AbstractPolicyTransform(final NotificationRestService notificationService,
			final ProjectVersionRestService projectVersionService, final PolicyRestService policyService,
			final VersionBomPolicyRestService bomVersionPolicyService,
			final ComponentVersionRestService componentVersionService, final PolicyNotificationFilter policyFilter) {
		super(notificationService, projectVersionService, policyService, bomVersionPolicyService,
				componentVersionService);
		this.policyFilter = policyFilter;
	}

	public void handleNotification(final List<ComponentVersionStatus> componentVersionList,
			final ProjectVersion projectVersion, final NotificationItem item,
			final List<NotificationContentItem> templateData) throws HubItemTransformException {
		for (final ComponentVersionStatus componentVersion : componentVersionList) {
			try {
				String componentVersionName;
				if (StringUtils.isBlank(componentVersion.getComponentVersionLink())) {
					componentVersionName = "";
				} else {
					componentVersionName = getComponentVersionService()
							.getComponentVersion(componentVersion.getComponentVersionLink()).getVersionName();
				}

				final String policyStatusUrl = componentVersion.getBomComponentVersionPolicyStatusLink();

				if (StringUtils.isNotBlank(policyStatusUrl)) {
					final BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus = getBomVersionPolicyService()
							.getPolicyStatus(policyStatusUrl);
					List<String> ruleList = getRules(
							bomComponentVersionPolicyStatus.getLinks(BomComponentVersionPolicyStatus.POLICY_RULE_URL));

					ruleList = getMatchingRules(ruleList);
					if (ruleList != null && !ruleList.isEmpty()) {
						final List<PolicyRule> policyRuleList = new ArrayList<PolicyRule>();
						for (final String ruleUrl : ruleList) {
							final PolicyRule rule = getPolicyService().getPolicyRule(ruleUrl);
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

	private List<String> getRules(final List<String> rulesViolated) throws NotificationServiceException {
		if (rulesViolated == null || rulesViolated.isEmpty()) {
			return null;
		}
		final List<String> matchingRules = new ArrayList<String>();
		for (final String ruleViolated : rulesViolated) {
			final String fixedRuleUrl = fixRuleUrl(ruleViolated);
			matchingRules.add(fixedRuleUrl);
		}
		return matchingRules;
	}

	private List<String> getMatchingRules(final List<String> rulesViolated) {
		final List<String> filteredRules = new ArrayList<>();
		if (policyFilter != null && policyFilter.getRuleLinksToInclude() != null
				&& !policyFilter.getRuleLinksToInclude().isEmpty()) {
			for (final String ruleViolated : rulesViolated) {
				if (policyFilter.getRuleLinksToInclude().contains(ruleViolated)) {
					filteredRules.add(ruleViolated);
				}
			}
		} else {
			return rulesViolated;
		}
		return filteredRules;
	}

	/**
	 * In Hub versions prior to 3.2, the rule URLs contained in notifications
	 * are internal. To match the configured rule URLs, the "internal" segment
	 * of the URL from the notification must be removed. This is the workaround
	 * recommended by Rob P. In Hub 3.2 on, these URLs will exclude the
	 * "internal" segment.
	 *
	 * @param origRuleUrl
	 * @return
	 */
	private String fixRuleUrl(final String origRuleUrl) {
		String fixedRuleUrl = origRuleUrl;
		if (origRuleUrl.contains("/internal/")) {
			fixedRuleUrl = origRuleUrl.replace("/internal/", "/");
		}
		return fixedRuleUrl;
	}

	public abstract void createContents(final ProjectVersion projectVersion, final String componentName,
			final String componentVersion, final UUID componentId, final UUID componentVersionId,
			List<PolicyRule> policyRuleList, NotificationItem item,
			List<NotificationContentItem> templateData);
}
