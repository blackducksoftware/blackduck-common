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
package com.blackducksoftware.integration.hub;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.mockito.Mockito;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.blackducksoftware.integration.hub.api.HubVersionRestService;
import com.blackducksoftware.integration.hub.capabilities.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.test.TestLogger;

public class HubSupportHelperTest {
    private HubVersionRestService getMockedService(final String returnVersion) throws Exception {
        final HubVersionRestService service = Mockito.mock(HubVersionRestService.class);
        Mockito.when(service.getHubVersion()).thenReturn(returnVersion);
        return service;
    }

    @Test
    public void testHubVersionApiMissing() throws Exception {
        final HubVersionRestService service = getMockedService("2.0.1");
        final ResourceException cause = new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
        final BDRestException exception = new BDRestException("", cause, null);
        Mockito.when(service.getHubVersion()).thenThrow(exception);

        final HubSupportHelper supportHelper = new HubSupportHelper();
        final TestLogger logger = new TestLogger();
        supportHelper.checkHubSupport(service, logger);

        for (final HubCapabilitiesEnum value : HubCapabilitiesEnum.values()) {
            assertFalse(supportHelper.hasCapability(value));
        }
    }

}
