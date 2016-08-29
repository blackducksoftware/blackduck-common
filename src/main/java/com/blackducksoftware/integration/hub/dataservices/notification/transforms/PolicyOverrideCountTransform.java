package com.blackducksoftware.integration.hub.dataservices.notification.transforms;

import java.util.Map;

import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountDataBuilder;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public class PolicyOverrideCountTransform extends AbstractNotificationCounter {

	public PolicyOverrideCountTransform(final ProjectVersionRestService projectVersionService,
			final Map<String, NotificationCountDataBuilder> countBuilderMap) {
		super(projectVersionService, countBuilderMap);
	}

	@Override
	public void count(final NotificationItem item) throws HubItemTransformException {
		try {
			final ReleaseItem releaseItem;
			final PolicyOverrideNotificationItem policyViolation = (PolicyOverrideNotificationItem) item;
			final String projectName = policyViolation.getContent().getProjectName();
			releaseItem = getProjectVersionService()
					.getProjectVersionReleaseItem(policyViolation.getContent().getProjectVersionLink());

			final ProjectVersion projectVersion = new ProjectVersion();
			projectVersion.setProjectName(projectName);
			projectVersion.setProjectVersionName(releaseItem.getVersionName());
			projectVersion.setProjectVersionLink(policyViolation.getContent().getProjectVersionLink());
			final NotificationCountDataBuilder builder = getCountBuilder(projectVersion);
			builder.incrementPolicyOverrideCounts(policyViolation);
		} catch (final Exception e) {

		}
	}
}
