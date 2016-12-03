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
package com.blackducksoftware.integration.hub.dataservice.extension;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.extension.ConfigurationItem;
import com.blackducksoftware.integration.hub.api.extension.ExtensionConfigRequestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionItem;
import com.blackducksoftware.integration.hub.api.extension.ExtensionUserOptionRequestService;
import com.blackducksoftware.integration.hub.api.extension.UserOptionLinkItem;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.dataservice.extension.item.UserConfigItem;
import com.blackducksoftware.integration.hub.dataservice.extension.transformer.UserConfigTransform;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessor;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.log.IntLogger;

public class ExtensionConfigDataService extends HubRequestService {
    private final IntLogger logger;

    private final HubRequestService hubRequestService;

    private final ExtensionConfigRequestService extensionConfigRequestService;

    private final UserConfigTransform userConfigTransform;

    private final ExtensionUserOptionRequestService extensionUserOptionRequestService;

    private final ParallelResourceProcessor<UserConfigItem, UserOptionLinkItem> parallelProcessor;

    public ExtensionConfigDataService(final IntLogger logger, final RestConnection restConnection, final UserRequestService userRequestService,
            final HubRequestService hubRequestService,
            final ExtensionConfigRequestService extensionConfigRequestService,
            final ExtensionUserOptionRequestService extensionUserOptionRequestService) {
        super(restConnection);
        this.logger = logger;
        this.hubRequestService = hubRequestService;
        this.extensionConfigRequestService = extensionConfigRequestService;
        this.extensionUserOptionRequestService = extensionUserOptionRequestService;
        userConfigTransform = new UserConfigTransform(userRequestService, extensionConfigRequestService);
        parallelProcessor = new ParallelResourceProcessor<>(logger);
        parallelProcessor.addTransform(UserOptionLinkItem.class, userConfigTransform);
    }

    public Map<String, ConfigurationItem> getGlobalConfigMap(final String extensionUrl) throws HubIntegrationException {
        Map<String, ConfigurationItem> globalConfigMap = new HashMap<>();
        final ExtensionItem extension = hubRequestService.getItem(extensionUrl, ExtensionItem.class);
        globalConfigMap = createGlobalConfigMap(extension.getLink("global-options"));
        return globalConfigMap;
    }

    public List<UserConfigItem> getUserConfigList(final String extensionUrl) throws HubIntegrationException {
        List<UserConfigItem> itemList = new LinkedList<>();
        final ExtensionItem extension = hubRequestService.getItem(extensionUrl, ExtensionItem.class);
        final List<UserOptionLinkItem> userOptionList = extensionUserOptionRequestService
                .getUserOptions(extension.getLink("user-options"));
        itemList = parallelProcessor.process(userOptionList);

        return itemList;
    }

    private Map<String, ConfigurationItem> createGlobalConfigMap(final String globalConfigUrl) throws HubIntegrationException {
        final List<ConfigurationItem> itemList = extensionConfigRequestService.getGlobalOptions(globalConfigUrl);
        final Map<String, ConfigurationItem> itemMap = createConfigMap(itemList);
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
