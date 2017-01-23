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

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.component.Component;
import com.blackducksoftware.integration.hub.api.component.ComponentRequestService;
import com.blackducksoftware.integration.hub.api.component.version.ComplexLicense;
import com.blackducksoftware.integration.hub.api.component.version.ComplexLicensePlusMeta;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.api.component.version.License;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;

public class LicenseDataService extends HubRequestService {
    private final HubRequestService hubRequestService;

    private final ComponentRequestService componentRequestService;
    
    private final MetaService metaService;

    public LicenseDataService(final RestConnection restConnection, final HubRequestService hubRequestService,
            final ComponentRequestService componentRequestService, final MetaService metaService) {
        super(restConnection);
        this.hubRequestService = hubRequestService;
        this.componentRequestService = componentRequestService;
        this.metaService = metaService;
    }

    public ComplexLicensePlusMeta getComplexLicensePlusMetaFromComponent(final String namespace, final String groupId, final String artifactId, final String version)
            throws HubIntegrationException {
        final Component component = componentRequestService.getExactComponentMatch(namespace, groupId, artifactId, version);
        final String versionUrl = component.getVersion();

        final ComponentVersion componentVersion = hubRequestService.getItem(versionUrl, ComponentVersion.class);
        final ComplexLicense parentComplexLicense = componentVersion.getLicense();
        
        final List<ComplexLicensePlusMeta> subLicensesPlusMeta = new ArrayList<ComplexLicensePlusMeta>();
        for(ComplexLicense subLicense : parentComplexLicense.getLicenses()) {
        	final License license = hubRequestService.getItem(subLicense.getLicense(), License.class);
        	//FIXME change to updated method once MetaService is updated
            final String textUrl = metaService.getLink(license, MetaService.TEXT_LINK);
            subLicensesPlusMeta.add(new ComplexLicensePlusMeta(subLicense, textUrl, new ArrayList<ComplexLicensePlusMeta>()));
        }
        
        
        
        return new ComplexLicensePlusMeta(parentComplexLicense, "", subLicensesPlusMeta);
    }

}
