/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UrlConstants;
import com.blackducksoftware.integration.hub.api.component.ComponentService;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentSearchResultView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.log.IntLogger;

public class ComponentDataService {
    private final IntLogger logger;
    private final ComponentService componentRequestService;

    public ComponentDataService(final IntLogger logger, final ComponentService componentRequestService) {
        this.logger = logger;
        this.componentRequestService = componentRequestService;
    }

    public ComponentVersionView getExactComponentVersionFromComponent(final ExternalId externalId) throws IntegrationException {
        for (final ComponentVersionView componentVersion : this.getAllComponentVersionsFromComponent(externalId)) {
            if (componentVersion.versionName.equals(externalId.version)) {
                return componentVersion;
            }
        }
        final String errMsg = "Could not find version " + externalId.version + " of component " + externalId.createHubOriginId();
        logger.error(errMsg);
        throw new HubIntegrationException(errMsg);
    }

    public List<ComponentVersionView> getAllComponentVersionsFromComponent(final ExternalId externalId) throws IntegrationException {
        final ComponentSearchResultView componentSearchView = componentRequestService.getExactComponentMatch(externalId);

        final ComponentView componentView = componentRequestService.getResponse(componentSearchView.component, ComponentView.class);
        final List<ComponentVersionView> componentVersionViews = componentRequestService.getAllResponsesFromLink(componentView, UrlConstants.SEGMENT_VERSIONS, ComponentVersionView.class);

        return componentVersionViews;
    }

}
