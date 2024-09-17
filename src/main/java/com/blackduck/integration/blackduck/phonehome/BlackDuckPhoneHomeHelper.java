/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.phonehome;

import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.blackduck.integration.blackduck.service.model.BlackDuckServerData;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.phonehome.PhoneHomeClient;
import com.blackduck.integration.phonehome.PhoneHomeResponse;
import com.blackduck.integration.phonehome.PhoneHomeService;
import com.blackduck.integration.phonehome.request.PhoneHomeRequestBody;
import com.blackduck.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import com.blackduck.integration.util.IntEnvironmentVariables;
import com.blackduck.integration.util.NoThreadExecutorService;
import com.google.gson.Gson;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class BlackDuckPhoneHomeHelper {
    private final IntLogger logger;
    private final PhoneHomeService phoneHomeService;
    private final BlackDuckRegistrationService blackDuckRegistrationService;
    private final IntEnvironmentVariables intEnvironmentVariables;

    public static BlackDuckPhoneHomeHelper createPhoneHomeHelper(BlackDuckServicesFactory blackDuckServicesFactory, String apiSecret, String measurementId) {
        return BlackDuckPhoneHomeHelper.createAsynchronousPhoneHomeHelper(blackDuckServicesFactory, apiSecret, measurementId, new NoThreadExecutorService());
    }

    public static BlackDuckPhoneHomeHelper createAsynchronousPhoneHomeHelper(BlackDuckServicesFactory blackDuckServicesFactory, String apiSecret, String measurementId, ExecutorService executorService) {
        BlackDuckRegistrationService blackDuckRegistrationService = blackDuckServicesFactory.createBlackDuckRegistrationService();

        IntLogger intLogger = blackDuckServicesFactory.getLogger();
        IntEnvironmentVariables intEnvironmentVariables = blackDuckServicesFactory.getEnvironmentVariables();
        BlackDuckHttpClient blackDuckHttpClient = blackDuckServicesFactory.getBlackDuckHttpClient();
        HttpClientBuilder httpClientBuilder = blackDuckHttpClient.getHttpClientBuilder();
        Gson gson = blackDuckServicesFactory.getGson();
        PhoneHomeClient phoneHomeClient = BlackDuckPhoneHomeHelper.createPhoneHomeClient(intLogger, httpClientBuilder, gson, apiSecret, measurementId);

        PhoneHomeService phoneHomeService = PhoneHomeService.createAsynchronousPhoneHomeService(intLogger, phoneHomeClient, executorService);

        return new BlackDuckPhoneHomeHelper(intLogger, phoneHomeService, blackDuckRegistrationService, intEnvironmentVariables);
    }

    public static PhoneHomeClient createPhoneHomeClient(IntLogger intLogger, HttpClientBuilder httpClientBuilder, Gson gson, String apiSecret, String measurementId) {
        return new PhoneHomeClient(intLogger, httpClientBuilder, gson, apiSecret, measurementId);
    }

    public BlackDuckPhoneHomeHelper(IntLogger logger, PhoneHomeService phoneHomeService, BlackDuckRegistrationService blackDuckRegistrationService,
        IntEnvironmentVariables intEnvironmentVariables) {
        this.logger = logger;
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
        String registrationKey = PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE;
        String blackDuckUrl = PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE;
        String blackDuckVersion = PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE;

        try {
            BlackDuckServerData blackDuckServerData = blackDuckRegistrationService.getBlackDuckServerData();
            registrationKey = blackDuckServerData.getRegistrationKey().orElse(PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE);
            blackDuckUrl = blackDuckServerData.getUrl().string();
            blackDuckVersion = blackDuckServerData.getVersion();
        } catch (IntegrationException e) {
            logger.warn("Could not gather all Black Duck data: " + e.getMessage());
        }

        PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = PhoneHomeRequestBodyBuilder.createForBlackDuck(integrationRepoName, registrationKey, blackDuckUrl, integrationVersion, blackDuckVersion);
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

}
