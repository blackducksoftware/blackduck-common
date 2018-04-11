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
package com.blackducksoftware.integration.hub.service;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.response.CurrentVersionView;
import com.blackducksoftware.integration.hub.service.model.PhoneHomeCallable;
import com.blackducksoftware.integration.hub.service.model.PhoneHomeResponse;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phonehome.PhoneHomeClient;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;
import com.blackducksoftware.integration.phonehome.enums.ProductIdEnum;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class PhoneHomeService extends DataService {
    private final HubRegistrationService hubRegistrationService;
    private final PhoneHomeClient phoneHomeClient;
    private final ExecutorService executorService;
    private final CIEnvironmentVariables ciEnvironmentVariables;

    public PhoneHomeService(final HubService hubService, final PhoneHomeClient phoneHomeClient, final HubRegistrationService hubRegistrationService, final CIEnvironmentVariables ciEnvironmentVariables) {
        super(hubService);
        this.hubRegistrationService = hubRegistrationService;
        this.phoneHomeClient = phoneHomeClient;
        this.ciEnvironmentVariables = ciEnvironmentVariables;
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
    }

    public PhoneHomeService(final HubService hubService, IntLogger logger, final PhoneHomeClient phoneHomeClient, final HubRegistrationService hubRegistrationService, final CIEnvironmentVariables ciEnvironmentVariables) {
        super(hubService, logger);
        this.hubRegistrationService = hubRegistrationService;
        this.phoneHomeClient = phoneHomeClient;
        this.ciEnvironmentVariables = ciEnvironmentVariables;
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
    }

    /**
     * @param artifactId The name of the jar without the version. For example: <i>hub-common</i>.
     * @param artifactVersion The version of the jar.
     */
    public void phoneHome(final String artifactId, final String artifactVersion) {
        final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder = createInitialPhoneHomeRequestBodyBuilder(artifactId, artifactVersion);
        phoneHome(phoneHomeRequestBodyBuilder);
    }

    public void phoneHome(final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder) {
        try {
            final PhoneHomeRequestBody phoneHomeRequestBody = phoneHomeRequestBodyBuilder.build();
            phoneHome(phoneHomeRequestBody);
        } catch (final Exception e) {
            logger.debug("Could not build phone home body" + e.getMessage());
        }
    }

    public void phoneHome(final PhoneHomeRequestBody phoneHomeRequestBody) {
        if (phoneHomeRequestBody == PhoneHomeRequestBody.DO_NOT_PHONE_HOME) {
            logger.debug("Skipping phone-home");
        } else {
            try {
                phoneHomeClient.postPhoneHomeRequest(phoneHomeRequestBody, ciEnvironmentVariables);
            } catch (final Exception e) {
                logger.debug("Problem with phone-home : " + e.getMessage(), e);
            }
        }
    }

    /**
     * @param artifactId The name of the jar without the version. For example: <i>hub-common</i>.
     * @param artifactVersion The version of the jar.
     */
    public PhoneHomeRequestBody.Builder createInitialPhoneHomeRequestBodyBuilder(final String artifactId, final String artifactVersion) {
        final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder = createInitialPhoneHomeRequestBodyBuilder();
        phoneHomeRequestBodyBuilder.setArtifactId(artifactId);
        phoneHomeRequestBodyBuilder.setArtifactVersion(artifactVersion);
        return phoneHomeRequestBodyBuilder;
    }

    public PhoneHomeRequestBody.Builder createInitialPhoneHomeRequestBodyBuilder() {
        final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder = new PhoneHomeRequestBody.Builder();
        try {
            final CurrentVersionView currentVersion = hubService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
            String registrationId = null;
            try {
                // We need to wrap this because this will most likely fail unless they are running as an admin
                registrationId = hubRegistrationService.getRegistrationId();
            } catch (final IntegrationException e) {
                registrationId = PhoneHomeRequestBody.Builder.UNKNOWN_ID;
            }
            final URL hubHostName = hubService.getHubBaseUrl();
            phoneHomeRequestBodyBuilder.setCustomerId(registrationId);
            phoneHomeRequestBodyBuilder.setHostName(hubHostName.toString());
            phoneHomeRequestBodyBuilder.setProductId(ProductIdEnum.HUB);
            phoneHomeRequestBodyBuilder.setProductVersion(currentVersion.version);
        } catch (final Exception e) {
            logger.debug("Couldn't detail phone home request builder: " + e.getMessage());
        }
        return phoneHomeRequestBodyBuilder;
    }

    /**
     * @param artifactId The name of the jar without the version. For example: <i>hub-common</i>.
     * @param artifactVersion The version of the jar.
     */
    public PhoneHomeResponse startPhoneHome(final String artifactId, final String artifactVersion) {
        final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder = createInitialPhoneHomeRequestBodyBuilder(artifactId, artifactVersion);
        return startPhoneHome(phoneHomeRequestBodyBuilder);
    }

    public PhoneHomeResponse startPhoneHome(final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder) {
        try {
            final PhoneHomeRequestBody phoneHomeRequestBody = phoneHomeRequestBodyBuilder.build();
            return startPhoneHome(phoneHomeRequestBody);
        } catch (final Exception e) {
            logger.debug("Could not build phone home body" + e.getMessage(), e);
        }
        return null;
    }

    public PhoneHomeResponse startPhoneHome(final PhoneHomeRequestBody phoneHomeRequestBody) {
        final PhoneHomeCallable task = new PhoneHomeCallable(logger, phoneHomeClient, phoneHomeRequestBody, ciEnvironmentVariables);
        final Future<Boolean> resultTask = executorService.submit(task);
        return new PhoneHomeResponse(resultTask);
    }

}
