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
package com.blackducksoftware.integration.hub.dataservice.extension.transformer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.extension.ConfigurationItem;
import com.blackducksoftware.integration.hub.api.extension.ExtensionConfigRequestService;
import com.blackducksoftware.integration.hub.api.extension.UserOptionLinkItem;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.dataservice.ItemTransform;
import com.blackducksoftware.integration.hub.dataservice.extension.item.UserConfigItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;

public class UserConfigTransform implements ItemTransform<List<UserConfigItem>, UserOptionLinkItem> {
    private final UserRequestService userRequestService;

    private final ExtensionConfigRequestService extensionConfigRequestService;

    public UserConfigTransform(final UserRequestService userRequestService,
            final ExtensionConfigRequestService extensionConfigRequestService) {
        this.userRequestService = userRequestService;
        this.extensionConfigRequestService = extensionConfigRequestService;
    }

    @Override
    public List<UserConfigItem> transform(final UserOptionLinkItem item) throws HubItemTransformException {
        try {
            final UserItem user = userRequestService.getItem(item.getUser());
            if (!user.isActive()) {
                return Collections.emptyList();
            } else {
                final Map<String, ConfigurationItem> configItems = getUserConfigOptions(item.getExtensionOptions());
                final List<UserConfigItem> itemList = new ArrayList<>(configItems.size());
                itemList.add(new UserConfigItem(user, configItems));
                return itemList;
            }
        } catch (final IOException | URISyntaxException | BDRestException | MissingUUIDException e) {
            throw new HubItemTransformException("Error processing user config for extension", e);
        }
    }

    private Map<String, ConfigurationItem> getUserConfigOptions(final String userConfigUrl)
            throws IOException, URISyntaxException, BDRestException, MissingUUIDException {
        final List<ConfigurationItem> userItemList = extensionConfigRequestService.getUserConfiguration(userConfigUrl);
        final Map<String, ConfigurationItem> itemMap = createConfigMap(userItemList);
        return itemMap;
    }

    private Map<String, ConfigurationItem> createConfigMap(final List<ConfigurationItem> itemList) {
        final Map<String, ConfigurationItem> itemMap = new HashMap<>(itemList.size());
        for (final ConfigurationItem item : itemList) {
            itemMap.put(item.getName(), item);
        }
        return itemMap;
    }
}
