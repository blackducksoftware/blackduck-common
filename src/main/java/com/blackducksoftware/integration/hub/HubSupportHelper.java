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

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Set;

import org.restlet.resource.ResourceException;

import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.capability.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.log.IntLogger;

public class HubSupportHelper implements Serializable {
    private static final long serialVersionUID = 6440466357358359056L;

    private boolean hasBeenChecked = false;

    private final Set<HubCapabilitiesEnum> capabilities = EnumSet.noneOf(HubCapabilitiesEnum.class);

    public boolean isHasBeenChecked() {
        return hasBeenChecked;
    }

    public void setHasBeenChecked(final boolean hasBeenChecked) {
        this.hasBeenChecked = hasBeenChecked;
    }

    /**
     * This will check the Hub server to see which options this version of the
     * Hub supports. You can use the get methods in this class after this method
     * has run to get the supported options.
     */
    public void checkHubSupport(HubVersionRequestService hubVersionRequestService, final IntLogger logger)
            throws IOException, URISyntaxException {
        try {
            if (hubVersionRequestService.isConsumerVersionLessThanOrEqualToServerVersion("3.3.1")) {
                setHub3_3_1Support();
                setHub3_1Support();
                setHub3_0Support();
            } else if (hubVersionRequestService.isConsumerVersionLessThanOrEqualToServerVersion("3.1.0")) {
                setHub3_1Support();
                setHub3_0Support();
            } else {
                if (hubVersionRequestService.isConsumerVersionLessThanOrEqualToServerVersion("3.0.0")) {
                    setHub3_0Support();
                }
            }
            setHasBeenChecked(true);
        } catch (final BDRestException e) {
            ResourceException resEx = null;
            if (e.getCause() != null && e.getCause() instanceof ResourceException) {
                resEx = (ResourceException) e.getCause();
            }
            if (resEx != null) {
                if (logger != null) {
                    logger.error(resEx.getMessage());
                }
            }
            if (logger != null) {
                logger.error(e.getMessage());
            }
        }
    }

    public boolean hasCapability(final HubCapabilitiesEnum capability) {
        return capabilities.contains(capability);
    }

    private void setHub3_0Support() {
        capabilities.add(HubCapabilitiesEnum.JRE_PROVIDED);
        capabilities.add(HubCapabilitiesEnum.POLICY_API);
        capabilities.add(HubCapabilitiesEnum.CLI_STATUS_DIRECTORY_OPTION);
    }

    private void setHub3_1Support() {
        capabilities.add(HubCapabilitiesEnum.CLI_PASSWORD_ENVIRONMENT_VARIABLE);
    }

    private void setHub3_3_1Support() {
        capabilities.add(HubCapabilitiesEnum.BOM_FILE_UPLOAD);
    }

}
