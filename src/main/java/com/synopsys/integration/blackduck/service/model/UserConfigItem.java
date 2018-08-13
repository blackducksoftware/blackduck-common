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
package com.synopsys.integration.blackduck.service.model;

import java.util.Map;

import com.synopsys.integration.hub.api.generated.view.ExternalExtensionConfigValueView;
import com.synopsys.integration.hub.api.generated.view.UserView;

public class UserConfigItem {
    private final UserView user;

    private final Map<String, ExternalExtensionConfigValueView> configMap;

    public UserConfigItem(final UserView user, final Map<String, ExternalExtensionConfigValueView> configItems) {
        this.user = user;
        this.configMap = configItems;
    }

    public UserView getUser() {
        return user;
    }

    public Map<String, ExternalExtensionConfigValueView> getConfigMap() {
        return configMap;
    }
}
