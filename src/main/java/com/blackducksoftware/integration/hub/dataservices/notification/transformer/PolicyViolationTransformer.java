/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.dataservices.notification.transformer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.component.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRestService;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.project.ReleaseItemRestService;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public class PolicyViolationTransformer extends AbstractPolicyTransformer {
	public PolicyViolationTransformer(final NotificationRestService notificationService,
			final ReleaseItemRestService projectVersionService, final PolicyRestService policyService,
			final VersionBomPolicyRestService bomVersionPolicyService,
			final ComponentVersionRestService componentVersionService, final PolicyNotificationFilter policyFilter) {
		super(notificationService, projectVersionService, policyService, bomVersionPolicyService,
				componentVersionService, policyFilter);
	}

	@Override
	public List<NotificationContentItem> transform(final NotificationItem item) throws HubItemTransformException {
		final List<NotificationContentItem> templateData = new ArrayList<>();
		try {
			final RuleViolationNotificationItem policyViolation = (RuleViolationNotificationItem) item;
			final String projectName = policyViolation.getContent().getProjectName();
			final List<ComponentVersionStatus> componentVersionList = policyViolation.getContent()
					.getComponentVersionStatuses();
			final String projectVersionLink = policyViolation.getContent().getProjectVersionLink();
			final ReleaseItem releaseItem = getReleaseItem(projectVersionLink);
			final ProjectVersion projectVersion = new ProjectVersion();
			projectVersion.setProjectName(projectName);
			projectVersion.setProjectVersionName(releaseItem.getVersionName());
			projectVersion.setUrl(policyViolation.getContent().getProjectVersionLink());

			handleNotification(componentVersionList, projectVersion, item, templateData);
		} catch (final IOException | BDRestException e) {
			throw new HubItemTransformException(e);
		} catch (final Exception e) {
			throw new RuntimeException(e);
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

	private ReleaseItem getReleaseItem(final String projectVersionLink)
			throws IOException, BDRestException, URISyntaxException {
		ReleaseItem releaseItem;
		releaseItem = getProjectVersionService().getItem(projectVersionLink);
		return releaseItem;
	}

	@Override
	public void createContents(final ProjectVersion projectVersion, final String componentName,
			final String componentVersion, final String componentUrl, final String componentVersionUrl,
			final List<PolicyRule> policyRuleList, final NotificationItem item,
			final List<NotificationContentItem> templateData) throws URISyntaxException {
		templateData.add(new PolicyViolationContentItem(item.getCreatedAt(), projectVersion, componentName,
				componentVersion, componentUrl, componentVersionUrl, policyRuleList));
	}

}
