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
package com.blackducksoftware.integration.hub.dataservice.extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.extension.ConfigurationView;
import com.blackducksoftware.integration.hub.api.extension.ExtensionConfigRequestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionUserOptionRequestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionView;
import com.blackducksoftware.integration.hub.api.extension.UserOptionLinkView;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.dataservice.extension.item.UserConfigItem;
import com.blackducksoftware.integration.hub.dataservice.extension.transformer.UserConfigTransform;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessor;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessorResults;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.log.IntLogger;

public class ExtensionConfigDataService extends HubResponseService {

    private final ExtensionConfigRequestService extensionConfigRequestService;

    private final UserConfigTransform userConfigTransform;

    private final ExtensionUserOptionRequestService extensionUserOptionRequestService;

    private final ParallelResourceProcessor<UserConfigItem, UserOptionLinkView> parallelProcessor;

    private final MetaService metaService;

    public ExtensionConfigDataService(final IntLogger logger, final RestConnection restConnection, final UserRequestService userRequestService,
            final ExtensionConfigRequestService extensionConfigRequestService,
            final ExtensionUserOptionRequestService extensionUserOptionRequestService, final MetaService metaService) {
        super(restConnection);
        this.extensionConfigRequestService = extensionConfigRequestService;
        this.extensionUserOptionRequestService = extensionUserOptionRequestService;
        this.metaService = metaService;
        userConfigTransform = new UserConfigTransform(userRequestService, extensionConfigRequestService);
        parallelProcessor = new ParallelResourceProcessor<>(logger);
        parallelProcessor.addTransform(UserOptionLinkView.class, userConfigTransform);

    }

    public Map<String, ConfigurationView> getGlobalConfigMap(final String extensionUrl) throws IntegrationException {
        Map<String, ConfigurationView> globalConfigMap = new HashMap<>();
        final ExtensionView extension = getItem(extensionUrl, ExtensionView.class);
        final String globalOptionsLink = metaService.getFirstLink(extension, MetaService.GLOBAL_OPTIONS_LINK);
        globalConfigMap = createGlobalConfigMap(globalOptionsLink);
        return globalConfigMap;
    }

    public ParallelResourceProcessorResults<UserConfigItem> getUserConfigList(final String extensionUrl) throws IntegrationException {

        final ExtensionView extension = getItem(extensionUrl, ExtensionView.class);
        final String userOptionsLink = metaService.getFirstLink(extension, MetaService.USER_OPTIONS_LINK);
        final List<UserOptionLinkView> userOptionList = extensionUserOptionRequestService
                .getUserOptions(userOptionsLink);
        final ParallelResourceProcessorResults<UserConfigItem> itemList = parallelProcessor.process(userOptionList);
        return itemList;
    }

    private Map<String, ConfigurationView> createGlobalConfigMap(final String globalConfigUrl) throws IntegrationException {
        final List<ConfigurationView> itemList = extensionConfigRequestService.getGlobalOptions(globalConfigUrl);
        final Map<String, ConfigurationView> itemMap = createConfigMap(itemList);
        return itemMap;
    }

    private Map<String, ConfigurationView> createConfigMap(final List<ConfigurationView> itemList) {
        final Map<String, ConfigurationView> itemMap = new HashMap<>(itemList.size());
        for (final ConfigurationView item : itemList) {
            itemMap.put(item.getName(), item);
        }
        return itemMap;
    }
}
