/**
 * blackduck-common
 * <p>
 * Copyright (c) 2020 Synopsys, Inc.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.phonehome;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.CurrentVersionView;
import com.synopsys.integration.blackduck.service.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.phonehome.PhoneHomeClient;
import com.synopsys.integration.phonehome.PhoneHomeResponse;
import com.synopsys.integration.phonehome.PhoneHomeService;
import com.synopsys.integration.phonehome.request.BlackDuckPhoneHomeRequestFactory;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.NoThreadExecutorService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @deprecated Please use the example code here to adapt your own phone home solution to the new api of the client - this class is being deleted with the next release of the library.
 */
@Deprecated
public class BlackDuckPhoneHomeHelper {
    private final IntLogger logger;
    private final BlackDuckService blackDuckService;
    private final PhoneHomeService phoneHomeService;
    private final BlackDuckRegistrationService blackDuckRegistrationService;
    private final IntEnvironmentVariables intEnvironmentVariables;

    /**
     * @deprecated Please provide an ExecutorService - for no change, you can provide an instance of NoThreadExecutorService
     */
    @Deprecated
    public static BlackDuckPhoneHomeHelper createPhoneHomeHelper(BlackDuckServicesFactory blackDuckServicesFactory) {
        return BlackDuckPhoneHomeHelper.createAsynchronousPhoneHomeHelper(blackDuckServicesFactory, new NoThreadExecutorService());
    }

    public static BlackDuckPhoneHomeHelper createAsynchronousPhoneHomeHelper(BlackDuckServicesFactory blackDuckServicesFactory, ExecutorService executorService) {
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        BlackDuckRegistrationService blackDuckRegistrationService = blackDuckServicesFactory.createBlackDuckRegistrationService();

        IntLogger intLogger = blackDuckServicesFactory.getLogger();
        IntEnvironmentVariables intEnvironmentVariables = blackDuckServicesFactory.getEnvironmentVariables();
        IntHttpClient intHttpClient = blackDuckServicesFactory.getBlackDuckHttpClient();
        Gson gson = blackDuckServicesFactory.getGson();
        PhoneHomeClient phoneHomeClient = BlackDuckPhoneHomeHelper.createPhoneHomeClient(intLogger, intHttpClient, gson);

        PhoneHomeService phoneHomeService = PhoneHomeService.createAsynchronousPhoneHomeService(intLogger, phoneHomeClient, executorService);

        return new BlackDuckPhoneHomeHelper(intLogger, blackDuckService, phoneHomeService, blackDuckRegistrationService, intEnvironmentVariables);
    }

    public static PhoneHomeClient createPhoneHomeClient(IntLogger intLogger, IntHttpClient intHttpClient, Gson gson) {
        HttpClientBuilder httpClientBuilder = intHttpClient.getClientBuilder();
        return new PhoneHomeClient(intLogger, httpClientBuilder, gson);
    }

    public BlackDuckPhoneHomeHelper(IntLogger logger, BlackDuckService blackDuckService, PhoneHomeService phoneHomeService, BlackDuckRegistrationService blackDuckRegistrationService,
                                    IntEnvironmentVariables intEnvironmentVariables) {
        this.logger = logger;
        this.blackDuckService = blackDuckService;
        this.phoneHomeService = phoneHomeService;
        this.blackDuckRegistrationService = blackDuckRegistrationService;
        this.intEnvironmentVariables = intEnvironmentVariables;
    }

    public PhoneHomeResponse handlePhoneHome(String integrationRepoName, String integrationVersion) {
        return handlePhoneHome(integrationRepoName, integrationVersion, Collections.emptyMap());
    }

    public PhoneHomeResponse handlePhoneHome(String integrationRepoName, String integrationVersion, Map<String, String> metaData, String... artifactModules) {
        try {
            PhoneHomeRequestBody phoneHomeRequestBody = createPhoneHomeRequestBody(integrationRepoName, integrationVersion, metaData, artifactModules);
            return phoneHomeService.phoneHome(phoneHomeRequestBody, getEnvironmentVariables());
        } catch (Exception e) {
            logger.debug("Problem phoning home: " + e.getMessage(), e);
        }
        return PhoneHomeResponse.createResponse(Boolean.FALSE);
    }

    private PhoneHomeRequestBody createPhoneHomeRequestBody(String integrationRepoName, String integrationVersion, Map<String, String> metaData, String... artifactModules) {
        String registrationKey = getRegistrationKey();
        String blackDuckUrl = getHostName();
        String blackDuckVersion = getProductVersion();

        BlackDuckPhoneHomeRequestFactory blackDuckPhoneHomeRequestFactory = new BlackDuckPhoneHomeRequestFactory(integrationRepoName);
        PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = blackDuckPhoneHomeRequestFactory.create(registrationKey, blackDuckUrl, integrationVersion, blackDuckVersion);
        phoneHomeRequestBodyBuilder.addArtifactModules(artifactModules);

        boolean metaDataSuccess = phoneHomeRequestBodyBuilder.addAllToMetaData(metaData);
        if (!metaDataSuccess) {
            logger.debug("The metadata provided to phone-home exceeded its size limit. At least some metadata will be missing.");
        }

        return phoneHomeRequestBodyBuilder.build();
    }

    private Map<String, String> getEnvironmentVariables() {
        if (intEnvironmentVariables != null) {
            return intEnvironmentVariables.getVariables();
        }
        return Collections.emptyMap();
    }

    private String getProductVersion() {
        CurrentVersionView currentVersion;
        try {
            currentVersion = blackDuckService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
            return currentVersion.getVersion();
        } catch (IntegrationException e) {
        }
        return PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE;
    }

    private String getHostName() {
        return blackDuckService.getBlackDuckBaseUrl().toString();
    }

    private String getRegistrationKey() {
        String registrationId = null;
        try {
            // We need to wrap this because this will most likely fail unless they are running as an admin
            registrationId = blackDuckRegistrationService.getRegistrationId();
        } catch (IntegrationException e) {
        }
        // We must check if the reg id is blank because of an edge case in which Black Duck can authenticate (while the webserver is coming up) without registration
        if (StringUtils.isBlank(registrationId)) {
            registrationId = PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE;
        }
        return registrationId;
    }

}
