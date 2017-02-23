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
package com.blackducksoftware.integration.hub.api.aggregate.bom;

import java.util.List;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubParameterizedRequestService;

public class AggregateBomRequestService extends HubParameterizedRequestService<VersionBomComponentView> {

    private final MetaService metaService;

    public AggregateBomRequestService(final RestConnection restConnection, final MetaService metaService) {
        super(restConnection, VersionBomComponentView.class);
        this.metaService = metaService;
    }

    public List<VersionBomComponentView> getBomEntries(final ProjectVersionItem projectVersion) throws HubIntegrationException {
        final String componentURL = metaService.getFirstLink(projectVersion, MetaService.COMPONENTS_LINK);
        return getBomEntries(componentURL);
    }

    public List<VersionBomComponentView> getBomEntries(final String componentsUrl) throws HubIntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(componentsUrl);

        final List<VersionBomComponentView> allComponentItems = getAllItems(hubPagedRequest);
        return allComponentItems;
    }

}
