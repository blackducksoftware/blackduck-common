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
package com.blackducksoftware.integration.hub.service.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ExternalExtensionConfigValueView;
import com.blackducksoftware.integration.hub.api.generated.view.ExternalExtensionUserView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.dataservice.HubDataService;
import com.blackducksoftware.integration.parallel.processor.ItemTransformer;

public class UserConfigTransform implements ItemTransformer<UserConfigItem, ExternalExtensionUserView> {
    private final HubDataService hubService;

    public UserConfigTransform(final HubDataService hubService) {
        this.hubService = hubService;
    }

    @Override
    public List<UserConfigItem> transform(final ExternalExtensionUserView item) throws IntegrationException {
        final UserView user = hubService.getResponse(item.user, UserView.class);
        if (!user.active) {
            return Collections.emptyList();
        } else {
            final Map<String, ExternalExtensionConfigValueView> configItems = getUserConfigOptions(item.extensionOptions);
            final List<UserConfigItem> itemList = new ArrayList<>(configItems.size());
            itemList.add(new UserConfigItem(user, configItems));
            return itemList;
        }
    }

    private Map<String, ExternalExtensionConfigValueView> getUserConfigOptions(final String userConfigUrl) throws IntegrationException {
        final List<ExternalExtensionConfigValueView> userItemList = hubService.getResponses(userConfigUrl, ExternalExtensionConfigValueView.class, true);
        final Map<String, ExternalExtensionConfigValueView> itemMap = createConfigMap(userItemList);
        return itemMap;
    }

    private Map<String, ExternalExtensionConfigValueView> createConfigMap(final List<ExternalExtensionConfigValueView> itemList) {
        final Map<String, ExternalExtensionConfigValueView> itemMap = new HashMap<>(itemList.size());
        for (final ExternalExtensionConfigValueView item : itemList) {
            itemMap.put(item.name, item);
        }
        return itemMap;
    }

}
