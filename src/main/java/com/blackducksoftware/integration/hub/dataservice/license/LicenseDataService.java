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
package com.blackducksoftware.integration.hub.dataservice.license;

import com.blackducksoftware.integration.hub.api.component.Component;
import com.blackducksoftware.integration.hub.api.component.ComponentRequestService;
import com.blackducksoftware.integration.hub.api.component.version.ComplexLicenseItem;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;

public class LicenseDataService extends HubRequestService {

    private final HubRequestService hubRequestService;

    private final ComponentRequestService componentRequestService;

    public LicenseDataService(final RestConnection restConnection, final HubRequestService hubRequestService,
            final ComponentRequestService componentRequestService) {
        super(restConnection);
        this.hubRequestService = hubRequestService;
        this.componentRequestService = componentRequestService;
    }

    public ComplexLicenseItem getComplexLicenseItemFromComponent(final String namespace, final String groupId, final String artifactId, final String version)
            throws HubIntegrationException {
        final Component component = componentRequestService.getExactComponentMatch(namespace, groupId, artifactId, version);
        final String versionUrl = component.getVersion();
        final ComponentVersion componentVersion = hubRequestService.getItem(versionUrl, ComponentVersion.class);
        return componentVersion.getLicense();
    }

}
