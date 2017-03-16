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
package com.blackducksoftware.integration.hub.phonehome;

import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;

public class IntegrationInfo {
    public static final IntegrationInfo DO_NOT_PHONE_HOME = new IntegrationInfo("", null, null);

    private final String thirdPartyName;

    private final String thirdPartyVersion;

    private final String pluginVersion;

    public IntegrationInfo(final String thirdPartyName, final String thirdPartyVersion, final String pluginVersion) {
        this.thirdPartyName = thirdPartyName;
        this.thirdPartyVersion = thirdPartyVersion;
        this.pluginVersion = pluginVersion;
    }

    public IntegrationInfo(final ThirdPartyName thirdPartyName, final String thirdPartyVersion, final String pluginVersion) {
        this.thirdPartyName = thirdPartyName.toString();
        this.thirdPartyVersion = thirdPartyVersion;
        this.pluginVersion = pluginVersion;
    }

    public String getThirdPartyName() {
        return thirdPartyName;
    }

    public String getThirdPartyVersion() {
        return thirdPartyVersion;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

}
