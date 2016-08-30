package com.blackducksoftware.integration.hub.dataservices.notification.transforms;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountBuilder;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public class PolicyViolationCounter extends AbstractNotificationCounter {

	public PolicyViolationCounter(final ProjectVersionRestService projectVersionService,
			final Map<String, NotificationCountBuilder> countBuilderMap) {
		super(projectVersionService, countBuilderMap);
	}

	@Override
	public void count(final NotificationItem item) throws HubItemTransformException {
		try {
			final RuleViolationNotificationItem policyViolation = (RuleViolationNotificationItem) item;
			final String projectName = policyViolation.getContent().getProjectName();
			final String projectVersionLink = policyViolation.getContent().getProjectVersionLink();
			final ReleaseItem releaseItem = getReleaseItem(projectVersionLink);
			final ProjectVersion projectVersion = new ProjectVersion();
			projectVersion.setProjectName(projectName);
			projectVersion.setProjectVersionName(releaseItem.getVersionName());
			projectVersion.setProjectVersionLink(policyViolation.getContent().getProjectVersionLink());
			final NotificationCountBuilder builder = getCountBuilder(projectVersion);
			builder.incrementPolicyCounts(policyViolation);
		} catch (final Exception e) {
			throw new HubItemTransformException(e);
		}
	}

	private ReleaseItem getReleaseItem(final String projectVersionLink)
			throws IOException, BDRestException, URISyntaxException {
		ReleaseItem releaseItem;
		releaseItem = getProjectVersionService().getProjectVersionReleaseItem(projectVersionLink);
		return releaseItem;
	}
}
