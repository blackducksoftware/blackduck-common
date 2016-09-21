package com.blackducksoftware.integration.hub.dataservices.notification.transforms;

import java.io.IOException;
import java.net.URISyntaxException;
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
import com.blackducksoftware.integration.hub.api.component.ComponentVersion;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.exception.BDRestException;
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

	public abstract void handleNotification(final List<ComponentVersionStatus> componentVersionList,
			final ProjectVersion projectVersion, final NotificationItem item,
			final List<NotificationContentItem> templateData) throws HubItemTransformException;

	protected String getComponentVersionName(final String componentVersionLink)
			throws NotificationServiceException, IOException, BDRestException, URISyntaxException {
		String componentVersionName;
		if (StringUtils.isBlank(componentVersionLink)) {
			componentVersionName = "";
		} else {
			ComponentVersion compVersion;
			compVersion = getComponentVersionService().getComponentVersion(componentVersionLink);
			componentVersionName = compVersion.getVersionName();
		}

		return componentVersionName;
	}

	protected List<PolicyRule> getRules(final List<String> ruleIdsViolated) throws NotificationServiceException,
	IOException, BDRestException, URISyntaxException {
		if (ruleIdsViolated == null || ruleIdsViolated.isEmpty()) {
			return null;
		}
		final List<PolicyRule> rules = new ArrayList<PolicyRule>();
		for (final String ruleIdViolated : ruleIdsViolated) {
			final PolicyRule ruleViolated = getPolicyService().getPolicyRuleById(ruleIdViolated);
			rules.add(ruleViolated);
		}
		return rules;
	}

	protected List<PolicyRule> getMatchingRules(final List<PolicyRule> rulesViolated) {
		final List<PolicyRule> filteredRules = new ArrayList<>();
		if (policyFilter != null && policyFilter.getRuleLinksToInclude() != null
				&& !policyFilter.getRuleLinksToInclude().isEmpty()) {
			for (final PolicyRule ruleViolated : rulesViolated) {
				if (policyFilter.getRuleLinksToInclude().contains(ruleViolated.getMeta().getHref())) {
					filteredRules.add(ruleViolated);
				}
			}
		} else {
			return rulesViolated;
		}
		return filteredRules;
	}

	public abstract void createContents(final ProjectVersion projectVersion, final String componentName,
			final String componentVersion, final UUID componentId, final UUID componentVersionId,
			List<PolicyRule> policyRuleList, NotificationItem item, List<NotificationContentItem> templateData);

	protected PolicyNotificationFilter getPolicyFilter() {
		return policyFilter;
	}

	protected PolicyRule getPolicyRule(final String ruleUrl) throws IOException, BDRestException, URISyntaxException {
		PolicyRule rule;
		rule = getPolicyService().getPolicyRule(ruleUrl);
		return rule;
	}

	protected List<String> getMatchingRuleUrls(final List<String> rulesViolated) {
		final List<String> filteredRules = new ArrayList<>();
		if (getPolicyFilter() != null && getPolicyFilter().getRuleLinksToInclude() != null
				&& !getPolicyFilter().getRuleLinksToInclude().isEmpty()) {
			for (final String ruleViolated : rulesViolated) {
				if (getPolicyFilter().getRuleLinksToInclude().contains(ruleViolated)) {
					filteredRules.add(ruleViolated);
				}
			}
		} else {
			return rulesViolated;
		}
		return filteredRules;
	}

	protected List<String> getRuleUrls(final List<String> rulesViolated) throws NotificationServiceException {
		if (rulesViolated == null || rulesViolated.isEmpty()) {
			return null;
		}
		final List<String> matchingRules = new ArrayList<>();
		for (final String ruleViolated : rulesViolated) {
			final String fixedRuleUrl = fixRuleUrl(ruleViolated);
			matchingRules.add(fixedRuleUrl);
		}
		return matchingRules;
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
	protected String fixRuleUrl(final String origRuleUrl) {
		String fixedRuleUrl = origRuleUrl;
		if (origRuleUrl.contains("/internal/")) {
			fixedRuleUrl = origRuleUrl.replace("/internal/", "/");
		}
		return fixedRuleUrl;
	}

	protected BomComponentVersionPolicyStatus getBomComponentVersionPolicyStatus(final String policyStatusUrl)
			throws IOException, BDRestException, URISyntaxException {
		BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus;
		bomComponentVersionPolicyStatus = getBomVersionPolicyService().getPolicyStatus(policyStatusUrl);

		return bomComponentVersionPolicyStatus;
	}

}
