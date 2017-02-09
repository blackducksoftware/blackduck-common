/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.dataservice.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.UrlConstants;
import com.blackducksoftware.integration.hub.api.component.Component;
import com.blackducksoftware.integration.hub.api.component.ComponentRequestService;
import com.blackducksoftware.integration.hub.api.component.id.ComponentIdItem;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubParameterizedRequestService;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.log.IntLogger;

public class ComponentDataService {

    private final ComponentRequestService componentRequestService;

    private final HubParameterizedRequestService<ComponentVersion> hubParameterizedRequestService;

    private final HubRequestService hubRequestService;

    private final MetaService metaService;

    private final IntLogger logger;

    public ComponentDataService(final IntLogger logger, final RestConnection restConnection, final HubRequestService hubRequestService,
            final ComponentRequestService componentRequestService, final MetaService metaService) {
        this.logger = logger;
        this.componentRequestService = componentRequestService;
        this.metaService = metaService;
        this.hubRequestService = hubRequestService;
        this.hubParameterizedRequestService = new HubParameterizedRequestService<>(restConnection, ComponentVersion.class);
    }

    public ComponentVersion getExactComponentVersionFromComponent(final String namespace, final String groupId, final String artifactId, final String version)
            throws HubIntegrationException {
        for (ComponentVersion componentVersion : this.getAllComponentVersionsFromComponent(namespace, groupId, artifactId, version)) {
            if (componentVersion.getVersionName().equals(version)) {
                return componentVersion;
            }
        }
        String errMsg = "Could not find version " + version + " of component " + StringUtils.join(new String[] { groupId, artifactId, version }, ":");
        logger.error(errMsg);
        throw new HubIntegrationException(errMsg);
    }

    public List<ComponentVersion> getAllComponentVersionsFromComponent(final String namespace, final String groupId, final String artifactId,
            final String version)
            throws HubIntegrationException {
        final Component component = componentRequestService.getExactComponentMatch(namespace, groupId, artifactId, version);
        component.getComponent();
        ComponentIdItem componentItem = hubRequestService.getItem(component.getComponent(), ComponentIdItem.class);
        String versionsURL = metaService.getFirstLinkSafely(componentItem, UrlConstants.SEGMENT_VERSIONS);
        List<ComponentVersion> versions = new ArrayList<>();
        if (versionsURL != null) {
            versions = hubParameterizedRequestService.getAllItems(versionsURL);
        }
        return versions;
    }

}
