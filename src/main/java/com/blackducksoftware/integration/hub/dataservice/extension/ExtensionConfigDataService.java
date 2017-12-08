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
import com.blackducksoftware.integration.hub.api.extension.ExtensionConfigService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionUserOptionService;
import com.blackducksoftware.integration.hub.api.item.MetaUtility;
import com.blackducksoftware.integration.hub.api.user.UserService;
import com.blackducksoftware.integration.hub.dataservice.extension.item.UserConfigItem;
import com.blackducksoftware.integration.hub.dataservice.extension.transformer.UserConfigTransform;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessor;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessorResults;
import com.blackducksoftware.integration.hub.model.view.ExternalExtensionConfigValueView;
import com.blackducksoftware.integration.hub.model.view.ExternalExtensionUserView;
import com.blackducksoftware.integration.hub.model.view.ExternalExtensionView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;

public class ExtensionConfigDataService extends HubService {
    private final ExtensionConfigService extensionConfigRequestService;
    private final UserConfigTransform userConfigTransform;
    private final ExtensionUserOptionService extensionUserOptionRequestService;
    private final ParallelResourceProcessor<UserConfigItem, ExternalExtensionUserView> parallelProcessor;

    public ExtensionConfigDataService(final IntLogger logger, final RestConnection restConnection, final UserService userRequestService, final ExtensionConfigService extensionConfigRequestService,
            final ExtensionUserOptionService extensionUserOptionRequestService) {
        super(restConnection);
        this.extensionConfigRequestService = extensionConfigRequestService;
        this.extensionUserOptionRequestService = extensionUserOptionRequestService;
        userConfigTransform = new UserConfigTransform(userRequestService, extensionConfigRequestService);
        parallelProcessor = new ParallelResourceProcessor<>(logger);
        parallelProcessor.addTransform(ExternalExtensionUserView.class, userConfigTransform);

    }

    public Map<String, ExternalExtensionConfigValueView> getGlobalConfigMap(final String extensionUrl) throws IntegrationException {
        Map<String, ExternalExtensionConfigValueView> globalConfigMap = new HashMap<>();
        final ExternalExtensionView extension = getView(extensionUrl, ExternalExtensionView.class);
        final String globalOptionsLink = getFirstLink(extension, MetaUtility.GLOBAL_OPTIONS_LINK);
        globalConfigMap = createGlobalConfigMap(globalOptionsLink);
        return globalConfigMap;
    }

    public ParallelResourceProcessorResults<UserConfigItem> getUserConfigList(final String extensionUrl) throws IntegrationException {
        final ExternalExtensionView extension = getView(extensionUrl, ExternalExtensionView.class);
        final String userOptionsLink = getFirstLink(extension, MetaUtility.USER_OPTIONS_LINK);
        final List<ExternalExtensionUserView> userOptionList = extensionUserOptionRequestService.getUserOptions(userOptionsLink);
        final ParallelResourceProcessorResults<UserConfigItem> itemList = parallelProcessor.process(userOptionList);
        return itemList;
    }

    private Map<String, ExternalExtensionConfigValueView> createGlobalConfigMap(final String globalConfigUrl) throws IntegrationException {
        final List<ExternalExtensionConfigValueView> itemList = extensionConfigRequestService.getGlobalOptions(globalConfigUrl);
        final Map<String, ExternalExtensionConfigValueView> itemMap = createConfigMap(itemList);
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
