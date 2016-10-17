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

import java.util.List;

import com.blackducksoftware.integration.hub.api.component.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRestService;
import com.blackducksoftware.integration.hub.api.project.ReleaseItemRestService;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.dataservices.ItemTransform;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public abstract class AbstractNotificationTransformer
implements ItemTransform<List<NotificationContentItem>, NotificationItem> {
	private final NotificationRestService notificationService;
	private final ReleaseItemRestService projectVersionService;
	private final PolicyRestService policyService;
	private final VersionBomPolicyRestService bomVersionPolicyService;
	private final ComponentVersionRestService componentVersionService;

	public AbstractNotificationTransformer(final NotificationRestService notificationService,
			final ReleaseItemRestService projectVersionService, final PolicyRestService policyService,
			final VersionBomPolicyRestService bomVersionPolicyService,
			final ComponentVersionRestService componentVersionService) {
		this.notificationService = notificationService;
		this.projectVersionService = projectVersionService;
		this.policyService = policyService;
		this.bomVersionPolicyService = bomVersionPolicyService;
		this.componentVersionService = componentVersionService;
	}

	public NotificationRestService getNotificationService() {
		return notificationService;
	}

	public ReleaseItemRestService getProjectVersionService() {
		return projectVersionService;
	}

	public PolicyRestService getPolicyService() {
		return policyService;
	}

	public VersionBomPolicyRestService getBomVersionPolicyService() {
		return bomVersionPolicyService;
	}

	public ComponentVersionRestService getComponentVersionService() {
		return componentVersionService;
	}

	@Override
	public abstract List<NotificationContentItem> transform(NotificationItem item) throws HubItemTransformException;

}
