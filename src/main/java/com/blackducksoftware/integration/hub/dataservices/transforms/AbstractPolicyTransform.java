package com.blackducksoftware.integration.hub.dataservices.transforms;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;

public abstract class AbstractPolicyTransform extends AbstractNotificationTransform {

	public AbstractPolicyTransform(final NotificationRestService notificationService,
			final ProjectVersionRestService projectVersionService, final PolicyRestService policyService,
			final VersionBomPolicyRestService bomVersionPolicyService,
			final ComponentVersionRestService componentVersionService) {
		super(notificationService, projectVersionService, policyService, bomVersionPolicyService,
				componentVersionService);
	}

	public void handleNotification(final String projectName, final List<ComponentVersionStatus> componentVersionList,
			final ReleaseItem releaseItem, final NotificationItem item,
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
					final List<String> ruleList = getRules(
							bomComponentVersionPolicyStatus.getLinks(BomComponentVersionPolicyStatus.POLICY_RULE_URL));
					if (ruleList != null && !ruleList.isEmpty()) {
						final List<String> ruleNameList = new ArrayList<String>();
						for (final String ruleUrl : ruleList) {
							final PolicyRule rule = getPolicyService().getPolicyRule(ruleUrl);
							ruleNameList.add(rule.getName());
						}
						createContents(projectName, releaseItem.getVersionName(), componentVersion.getComponentName(),
								componentVersionName, ruleNameList, item, templateData);
					}
				}
			} catch (final NotificationServiceException | IOException | BDRestException | URISyntaxException e) {
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

	public abstract void createContents(String projectName, String projectVersion, String componentName,
			String componentVersion, List<String> policyNameList, NotificationItem item,
			List<NotificationContentItem> templateData);
}
