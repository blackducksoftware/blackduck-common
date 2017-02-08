/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.dataservice.component;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.component.Component;
import com.blackducksoftware.integration.hub.api.component.ComponentRequestService;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubParameterizedRequestService;
import com.blackducksoftware.integration.log.IntLogger;

public class ComponentDataService {

    private final ComponentRequestService componentRequestService;

    private final HubParameterizedRequestService<ComponentVersion> hubParameterizedRequestService;

    private final IntLogger logger;

    public ComponentDataService(final IntLogger logger, final RestConnection restConnection, final ComponentRequestService componentRequestService) {
        this.logger = logger;
        this.componentRequestService = componentRequestService;
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
        final String componentURL = component.getComponent();
        return hubParameterizedRequestService.getAllItems(componentURL);
    }

}
