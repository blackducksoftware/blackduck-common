/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service.dataservice;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.CurrentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.RegistrationView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.BlackDuckServerData;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckRegistrationService extends DataService {
    private HttpUrl blackDuckUrl;

    public BlackDuckRegistrationService(BlackDuckService blackDuckService, RequestFactory requestFactory, IntLogger logger, HttpUrl blackDuckUrl) {
        super(blackDuckService, requestFactory, logger);
        this.blackDuckUrl = blackDuckUrl;
    }

    public String getRegistrationId() throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = requestFactory.createCommonGetRequestBuilder();
        requestBuilder.acceptMimeType("application/vnd.blackducksoftware.status-4+json");

        RegistrationView registrationView = blackDuckService.getResponse(ApiDiscovery.REGISTRATION_LINK_RESPONSE, requestBuilder);
        return registrationView.getRegistrationId();
    }

    public BlackDuckServerData getBlackDuckServerData() throws IntegrationException {
        CurrentVersionView currentVersionView = blackDuckService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
        String registrationId = null;
        try {
            // We need to wrap this because this will most likely fail unless they are running as an admin
            registrationId = getRegistrationId();
        } catch (IntegrationException e) {
        }
        return new BlackDuckServerData(blackDuckUrl, currentVersionView.getVersion(), registrationId);
    }

}
