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
package com.blackducksoftware.integration.hub.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.blackducksoftware.integration.hub.dataservice.license.LicenseDataService;
import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseCodeSharingEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseOwnershipEnum;
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class LicenseDataServiceTestIT {
    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testGettingLicenseFromComponentVersion() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final LicenseDataService licenseDataService = hubServicesFactory.createLicenseDataService();

        final ComplexLicenseView complexLicense = licenseDataService.getComplexLicenseItemFromComponent("maven", "com.google.guava", "guava", "20.0");
        assertNull(complexLicense.codeSharing);
        assertNull(complexLicense.license);
        assertNull(complexLicense.name);
        assertNull(complexLicense.ownership);
        assertEquals(ComplexLicenseEnum.CONJUNCTIVE, complexLicense.type);
        assertEquals(1, complexLicense.licenses.size());

        assertEquals(ComplexLicenseCodeSharingEnum.PERMISSIVE, complexLicense.licenses.get(0).codeSharing);
        assertTrue(StringUtils.isNotBlank(complexLicense.licenses.get(0).license));
        assertEquals("Apache License 2.0", complexLicense.licenses.get(0).name);
        assertEquals(ComplexLicenseOwnershipEnum.OPEN_SOURCE, complexLicense.licenses.get(0).ownership);
        assertNull(complexLicense.licenses.get(0).type);
        assertEquals(0, complexLicense.licenses.get(0).licenses.size());

        System.out.println(complexLicense);
    }

}
