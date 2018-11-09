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
package com.synopsys.integration.blackduck.phonehome;

import java.util.Map;

import com.synopsys.integration.phonehome.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.enums.ProductIdEnum;

public class BlackDuckPhoneHomeRequestBuilder {
    public static final String UNKNOWN_ID = PhoneHomeRequestBody.Builder.UNKNOWN_ID;

    private final PhoneHomeRequestBody.Builder builder;

    public BlackDuckPhoneHomeRequestBuilder() {
        this.builder = new PhoneHomeRequestBody.Builder();
    }

    public PhoneHomeRequestBody build() throws IllegalStateException {
        return builder.build();
    }

    public BlackDuckPhoneHomeRequestBuilder setProduct(final ProductIdEnum product) {
        builder.setProductId(product);
        return this;
    }

    public BlackDuckPhoneHomeRequestBuilder setProductVersion(final String versionName) {
        builder.setProductVersion(versionName);
        return this;
    }

    public BlackDuckPhoneHomeRequestBuilder setIntegrationRepoName(final String githubRepoName) {
        builder.setArtifactId(githubRepoName);
        return this;
    }

    public BlackDuckPhoneHomeRequestBuilder setIntegrationVersion(final String versionName) {
        builder.setArtifactVersion(versionName);
        return this;
    }

    public BlackDuckPhoneHomeRequestBuilder setRegistrationKey(final String registrationKey) {
        builder.setCustomerId(registrationKey);
        return this;
    }

    public BlackDuckPhoneHomeRequestBuilder setCustomerDomainName(final String hostName) {
        builder.setHostName(hostName);
        return this;
    }

    /**
     * metaData map cannot exceed {@value com.synopsys.integration.phonehome.PhoneHomeRequestBody#MAX_META_DATA_CHARACTERS}
     * @return true if the data was successfully added, false if the new data would make the map exceed it's size limit
     */
    public boolean addToMetaData(final String key, final String value) {
        return builder.addToMetaData(key, value);
    }

    /**
     * metaData map cannot exceed {@value com.synopsys.integration.phonehome.PhoneHomeRequestBody#MAX_META_DATA_CHARACTERS}
     * @return true if the all the data was successfully added,
     * false if one or more of the entries entries would make the map exceed it's size limit
     */
    public boolean addAllToMetaData(final Map<String, String> metadataMap) {
        return builder.addAllToMetaData(metadataMap);
    }
}
