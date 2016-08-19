package com.blackducksoftware.integration.hub.dataservices.transforms;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.blackducksoftware.integration.hub.api.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.NotificationRestService;
import com.blackducksoftware.integration.hub.api.PolicyRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public class PolicyViolationTransform extends AbstractPolicyTransform {

	private final Map<String, ReleaseItem> releaseItemMap = new ConcurrentHashMap<>();

	public PolicyViolationTransform(final NotificationRestService notificationService,
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
			final RuleViolationNotificationItem policyViolation = (RuleViolationNotificationItem) item;
			final String projectName = policyViolation.getContent().getProjectName();
			final List<ComponentVersionStatus> componentVersionList = policyViolation.getContent()
					.getComponentVersionStatuses();
			ReleaseItem releaseItem;
			final String projectVersionLink = policyViolation.getContent().getProjectVersionLink();
			if (releaseItemMap.containsKey(projectVersionLink)) {
				releaseItem = releaseItemMap.get(projectVersionLink);
			} else {
				releaseItem = getProjectVersionService().getProjectVersionReleaseItem(projectVersionLink);
				releaseItemMap.put(projectVersionLink, releaseItem);
			}
			handleNotification(projectName, componentVersionList, releaseItem, item, templateData);
		} catch (final IOException | BDRestException | URISyntaxException e) {
			throw new HubItemTransformException(e);
		}

		return templateData;
	}

	@Override
	public void createContents(final String projectName, final String projectVersion, final String componentName,
			final String componentVersion, final List<String> policyNameList, final NotificationItem item,
			final List<NotificationContentItem> templateData) {
		templateData.add(new PolicyViolationContentItem(projectName, projectVersion, componentName, componentVersion,
				policyNameList));
	}

	@Override
	public void reset() {
		super.reset();
		releaseItemMap.clear();
	}
}
