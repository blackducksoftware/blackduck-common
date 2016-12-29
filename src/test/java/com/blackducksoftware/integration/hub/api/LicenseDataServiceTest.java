/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.api;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.component.version.ComplexLicense;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.license.LicenseDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class LicenseDataServiceTest {
    @Test
    public void testComponentVersion() throws Exception {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setHubUrl("http://int-hub01.dc1.lan:8080");
        hubServerConfigBuilder.setUsername("sysadmin");
        hubServerConfigBuilder.setPassword("blackduck");

        final HubServerConfig hubServerConfig = hubServerConfigBuilder.build();
        final CredentialsRestConnection credentialsRestConnection = new CredentialsRestConnection(hubServerConfig);
        final HubServicesFactory hubServicesFactory = new HubServicesFactory(credentialsRestConnection);
        final LicenseDataService licenseDataService = hubServicesFactory.createLicenseDataService();

        final ComplexLicense complexLicense = licenseDataService.getComplexLicenseFromComponent("maven", "com.google.guava", "guava", "20.0");
        System.out.println(complexLicense);
    }

}
